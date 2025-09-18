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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
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

    public UserResponse registerUser(UserRegistrationRequest request) {
        try {
            // Validate Kraken credentials by making a test API call
            KrakenAccountResponse accountInfo = krakenClient.getAccountInfo(
                request.getKrakenApiKey(), 
                request.getKrakenPrivateKey()
            );

            // Extract initial balance if available
            BigDecimal initialBalance = null;
            if (accountInfo.getAccounts() != null && !accountInfo.getAccounts().isEmpty()) {
                // Get the first account's balance
                KrakenAccountResponse.Account account = accountInfo.getAccounts().get(0);
                initialBalance = account.getBalance();
            }

            // Generate unique client ID
            String clientId = generateUniqueClientId();

            // Create new user
            User user = new User(clientId, request.getKrakenApiKey(), request.getKrakenPrivateKey());
            user.setDailyRiskAbsolute(request.getDailyRiskAbsolute());
            user.setDailyRiskPercentage(request.getDailyRiskPercentage());
            user.setInitialBalance(initialBalance);

            user = userRepository.save(user);

            logger.info("New user registered with client_id: {}", clientId);
            return convertToUserResponse(user);

        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage());
            throw new RuntimeException("Failed to register user. Please check your Kraken API credentials. Error: " + e.getMessage());
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
                user.getCreatedAt()
        );
    }
}
