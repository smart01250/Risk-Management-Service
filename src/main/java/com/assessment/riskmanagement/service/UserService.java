package com.assessment.riskmanagement.service;

import com.assessment.riskmanagement.client.KrakenClient;
import com.assessment.riskmanagement.dto.UserRegistrationRequest;
import com.assessment.riskmanagement.dto.UserResponse;
import com.assessment.riskmanagement.dto.kraken.KrakenAccountResponse;
import com.assessment.riskmanagement.entity.User;
import com.assessment.riskmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KrakenClient krakenClient;

    @Value("${risk-management.kraken.demo-mode:false}")
    private boolean demoMode;

    public UserResponse registerUser(UserRegistrationRequest request) {
        try {
            BigDecimal initialBalance = null;

            if (demoMode) {
                // Demo mode - skip Kraken API validation and use mock data
                logger.info("DEMO MODE: Skipping Kraken API validation for user registration");
                initialBalance = new BigDecimal("10000.00"); // Mock initial balance
            } else {
                // Production mode - validate Kraken credentials by making a test API call
                KrakenAccountResponse accountInfo = krakenClient.getAccountInfo(
                    request.getKrakenApiKey(),
                    request.getKrakenPrivateKey()
                );

                // Extract initial balance if available
                if (accountInfo.getAccounts() != null && !accountInfo.getAccounts().isEmpty()) {
                    // Get the first account's balance
                    KrakenAccountResponse.Account account = accountInfo.getAccounts().get(0);
                    initialBalance = account.getBalance();
                }
            }

            // Generate unique client ID
            String clientId = generateUniqueClientId();

            // Create new user
            User user = new User(clientId, request.getKrakenApiKey(), request.getKrakenPrivateKey());
            user.setDailyRiskAbsolute(request.getDailyRiskAbsolute());
            user.setDailyRiskPercentage(request.getDailyRiskPercentage());
            user.setInitialBalance(initialBalance);

            user = userRepository.save(user);

            logger.info("New user registered with client_id: {} (demo mode: {})", clientId, demoMode);
            return convertToUserResponse(user);

        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage());
            String errorMessage = demoMode ?
                "Failed to register user in demo mode. Error: " + e.getMessage() :
                "Failed to register user. Please check your Kraken API credentials. Error: " + e.getMessage();
            throw new RuntimeException(errorMessage);
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserByClientId(String clientId) {
        return userRepository.findByClientId(clientId)
                .map(this::convertToUserResponse);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserEntityByClientId(String clientId) {
        return userRepository.findByClientId(clientId);
    }

    public UserResponse updateUser(String clientId, UserRegistrationRequest updateRequest) {
        User user = userRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateRequest.getDailyRiskAbsolute() != null) {
            user.setDailyRiskAbsolute(updateRequest.getDailyRiskAbsolute());
        }
        if (updateRequest.getDailyRiskPercentage() != null) {
            user.setDailyRiskPercentage(updateRequest.getDailyRiskPercentage());
        }

        user = userRepository.save(user);
        logger.info("User {} updated successfully", clientId);
        return convertToUserResponse(user);
    }

    public UserResponse setTradingEnabled(String clientId, boolean enabled) {
        User user = userRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTradingEnabled(enabled);
        user = userRepository.save(user);

        logger.info("Trading {} for user {}", enabled ? "enabled" : "disabled", clientId);
        return convertToUserResponse(user);
    }

    public Map<String, Object> updateUserBalanceWithRiskCheck(String clientId, double newBalance) {
        User user = userRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update balance
        user.setCurrentBalance(BigDecimal.valueOf(newBalance));
        user = userRepository.save(user);

        // Calculate risk info
        BigDecimal currentBalance = BigDecimal.valueOf(newBalance);
        BigDecimal initialBalance = user.getInitialBalance();
        BigDecimal lossAmount = initialBalance.subtract(currentBalance);
        BigDecimal lossPercentage = BigDecimal.ZERO;

        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            lossPercentage = lossAmount.divide(initialBalance, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Check if risk limits exceeded
        boolean riskExceeded = false;
        String riskStatus = "SAFE";
        List<String> actionsTaken = new ArrayList<>();

        if (user.getDailyRiskAbsolute() != null &&
            lossAmount.compareTo(user.getDailyRiskAbsolute()) >= 0) {
            riskExceeded = true;
            riskStatus = "EXCEEDED";
        } else if (user.getDailyRiskPercentage() != null &&
                   lossPercentage.compareTo(user.getDailyRiskPercentage()) >= 0) {
            riskExceeded = true;
            riskStatus = "EXCEEDED";
        } else if (user.getDailyRiskAbsolute() != null &&
                   lossAmount.compareTo(user.getDailyRiskAbsolute().multiply(BigDecimal.valueOf(0.9))) >= 0) {
            riskStatus = "AT_LIMIT";
        }

        // If risk exceeded, disable trading
        if (riskExceeded && user.getTradingEnabled()) {
            user.setTradingEnabled(false);
            user = userRepository.save(user);
            actionsTaken.add("Trading disabled");
            actionsTaken.add("Risk event logged");
        }

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", riskExceeded ?
            "Balance updated for user " + clientId + " - RISK LIMIT EXCEEDED" :
            "Balance updated for user " + clientId);
        response.put("user", convertToUserResponse(user));

        Map<String, Object> riskInfo = new HashMap<>();
        riskInfo.put("loss_amount", lossAmount);
        riskInfo.put("loss_percentage", lossPercentage);
        riskInfo.put("risk_status", riskStatus);
        if (!actionsTaken.isEmpty()) {
            riskInfo.put("actions_taken", actionsTaken);
        }
        response.put("risk_info", riskInfo);

        logger.info("Balance updated to {} for user {} - Risk Status: {}", newBalance, clientId, riskStatus);
        return response;
    }

    public void deleteUser(String clientId) {
        User user = userRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
        logger.info("User {} deleted successfully", clientId);
    }

    @Transactional(readOnly = true)
    public List<User> getAllActiveUsers() {
        return userRepository.findAllActiveUsers();
    }

    public void updateTradingStatus(User user, boolean enabled) {
        user.setTradingEnabled(enabled);
        userRepository.save(user);
        logger.info("Trading status updated for user {}: {}", user.getClientId(), enabled);
    }

    public void updateLastRiskCheck(User user) {
        user.setLastRiskCheck(java.time.LocalDateTime.now());
        userRepository.save(user);
    }

    public UserResponse updateUserBalance(String clientId, BigDecimal newBalance) {
        User user = userRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setCurrentBalance(newBalance);
        user = userRepository.save(user);

        logger.info("Balance updated to {} for user {}", newBalance, clientId);
        return convertToUserResponse(user);
    }

    public BigDecimal getUserCurrentBalance(String clientId) {
        User user = userRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getCurrentBalance();
    }

    private String generateUniqueClientId() {
        String clientId;
        do {
            clientId = String.format("%010d", random.nextInt(1000000000));
        } while (userRepository.existsByClientId(clientId));
        return clientId;
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getClientId(),
                user.getIsActive(),
                user.getTradingEnabled(),
                user.getDailyRiskAbsolute(),
                user.getDailyRiskPercentage(),
                user.getInitialBalance(),
                user.getCurrentBalance(),
                user.getCreatedAt()
        );
    }
}
