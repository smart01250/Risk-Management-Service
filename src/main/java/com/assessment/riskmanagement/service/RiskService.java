package com.assessment.riskmanagement.service;

import com.assessment.riskmanagement.client.KrakenClient;
import com.assessment.riskmanagement.dto.RiskCheckResponse;
import com.assessment.riskmanagement.dto.kraken.KrakenBalanceResponse;
import com.assessment.riskmanagement.entity.RiskEvent;
import com.assessment.riskmanagement.entity.RiskEventType;
import com.assessment.riskmanagement.entity.User;
import com.assessment.riskmanagement.repository.RiskEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RiskService {

    private static final Logger logger = LoggerFactory.getLogger(RiskService.class);

    @Autowired
    private RiskEventRepository riskEventRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private KrakenClient krakenClient;

    @Autowired
    private ObjectMapper objectMapper;

    public RiskCheckResponse checkUserRisk(User user) {
        try {
            // Use the currentBalance from database if available, otherwise try Kraken API
            BigDecimal currentBalance = user.getCurrentBalance();

            // If no currentBalance in database, try to get from Kraken API
            if (currentBalance == null) {
                try {
                    KrakenBalanceResponse balanceInfo = krakenClient.getBalances(
                            user.getKrakenApiKey(),
                            user.getKrakenPrivateKey()
                    );

                    currentBalance = BigDecimal.ZERO;
                    if (balanceInfo.getAccounts() != null && !balanceInfo.getAccounts().isEmpty()) {
                        for (KrakenBalanceResponse.Account account : balanceInfo.getAccounts()) {
                            if (account.getBalance() != null) {
                                currentBalance = currentBalance.add(account.getBalance());
                            }
                        }
                    }

                    // Update the user's currentBalance in database
                    user.setCurrentBalance(currentBalance);
                    userService.updateLastRiskCheck(user);

                } catch (Exception krakenException) {
                    logger.warn("Failed to get balance from Kraken API for user {}: {}. Using database value.",
                            user.getClientId(), krakenException.getMessage());
                    currentBalance = user.getCurrentBalance() != null ? user.getCurrentBalance() : BigDecimal.ZERO;
                }
            }


            // Initialize response
            RiskCheckResponse response = new RiskCheckResponse("success", "Risk check completed", "SAFE");
            response.setUserId(user.getId().toString());
            response.setClientId(user.getClientId());
            response.setCurrentBalance(currentBalance);
            response.setInitialBalance(user.getInitialBalance());
            response.setRiskAbsoluteLimit(user.getDailyRiskAbsolute());
            response.setRiskPercentageLimit(user.getDailyRiskPercentage());
            response.setRiskEvents(new ArrayList<>());
            response.setActionTaken("none");
            response.setPositionsClosed(0);

            if (user.getInitialBalance() == null) {
                user.setInitialBalance(currentBalance);
                userService.updateLastRiskCheck(user);
                logger.info("Set initial balance for user {}: {}", user.getClientId(), currentBalance);
                response.setInitialBalance(currentBalance);
                response.setDailyLoss(BigDecimal.ZERO);
                response.setDailyLossPercentage(BigDecimal.ZERO);
                return response;
            }


            // Calculate loss amounts
            BigDecimal lossAmount = user.getInitialBalance().subtract(currentBalance);
            BigDecimal lossPercentage = BigDecimal.ZERO;
            if (user.getInitialBalance().compareTo(BigDecimal.ZERO) > 0) {
                lossPercentage = lossAmount.divide(user.getInitialBalance(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            response.setDailyLoss(lossAmount);
            response.setDailyLossPercentage(lossPercentage);

            // Determine risk status
            boolean riskExceeded = false;
            boolean atLimit = false;
            String riskType = null;

            // Check absolute risk limit first
            if (user.getDailyRiskAbsolute() != null &&
                lossAmount.compareTo(user.getDailyRiskAbsolute()) > 0) {
                riskExceeded = true;
                riskType = "absolute";
            }
            // Check percentage risk limit
            else if (user.getDailyRiskPercentage() != null &&
                     lossPercentage.compareTo(user.getDailyRiskPercentage()) > 0) {
                riskExceeded = true;
                riskType = "percentage";
            }
            // Check if at limit (exactly at threshold)
            else if ((user.getDailyRiskAbsolute() != null &&
                      lossAmount.compareTo(user.getDailyRiskAbsolute()) == 0) ||
                     (user.getDailyRiskPercentage() != null &&
                      lossPercentage.compareTo(user.getDailyRiskPercentage()) == 0)) {
                atLimit = true;
            }

            // Set risk status and handle actions
            if (riskExceeded) {
                response.setRiskStatus("EXCEEDED");
                response.setMessage("Risk threshold exceeded - Trading disabled");

                BigDecimal riskThreshold = riskType.equals("absolute") ?
                    user.getDailyRiskAbsolute() : user.getDailyRiskPercentage();

                logger.warn("Risk limit exceeded for user {}: {} threshold {}",
                        user.getClientId(), riskType, riskThreshold);

                List<String> actionsTaken = handleRiskExceeded(
                        user, currentBalance, lossAmount, lossPercentage, riskThreshold, riskType
                );

                response.setActionTaken("trading_disabled_positions_closed");
                response.setPositionsClosed(actionsTaken.size() > 0 ?
                    extractPositionsClosedCount(actionsTaken) : 0);
            } else if (atLimit) {
                response.setRiskStatus("AT_LIMIT");
                response.setMessage("Risk check completed - At risk limit");
            } else {
                response.setRiskStatus("SAFE");
                response.setMessage("Risk check completed");
            }

            userService.updateLastRiskCheck(user);

            return response;

        } catch (Exception e) {
            logger.error("Error checking risk for user {}: {}", user.getClientId(), e.getMessage());
            throw new RuntimeException("Error checking risk for user", e);
        }
    }

    private int extractPositionsClosedCount(List<String> actionsTaken) {
        for (String action : actionsTaken) {
            if (action.contains("Closed") && action.contains("open orders")) {
                try {
                    String[] parts = action.split(" ");
                    for (String part : parts) {
                        if (part.matches("\\d+")) {
                            return Integer.parseInt(part);
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Failed to parse positions closed count from: {}", action);
                }
            }
        }
        return 0;
    }

    private List<String> handleRiskExceeded(User user, BigDecimal currentBalance,
                                          BigDecimal lossAmount, BigDecimal lossPercentage,
                                          BigDecimal riskThreshold, String riskType) {
        List<String> actionsTaken = new ArrayList<>();

        try {

            List<String> closedOrders = orderService.closeAllOrdersForUser(user);

            if (!closedOrders.isEmpty()) {
                actionsTaken.add("Closed " + closedOrders.size() + " open orders");
                logger.info("Closed {} orders for user {}", closedOrders.size(), user.getClientId());
            }


            LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime nextDay = nowUtc.plusDays(1)
                    .withHour(0)
                    .withMinute(1)
                    .withSecond(0)
                    .withNano(0);

            userService.updateTradingStatus(user, false);
            actionsTaken.add("Trading disabled until " + nextDay.toString());


            RiskEvent riskEvent = new RiskEvent(user, RiskEventType.DAILY_RISK_EXCEEDED,
                    "Daily risk limit exceeded: " + riskType + " threshold " + riskThreshold);
            riskEvent.setCurrentBalance(currentBalance);
            riskEvent.setInitialBalance(user.getInitialBalance());
            riskEvent.setRiskThreshold(riskThreshold);
            riskEvent.setLossAmount(lossAmount);
            riskEvent.setLossPercentage(lossPercentage);
            riskEvent.setTradingDisabledUntil(nextDay);

            if (!closedOrders.isEmpty()) {
                try {
                    riskEvent.setOrdersClosed(objectMapper.writeValueAsString(closedOrders));
                } catch (JsonProcessingException e) {
                    logger.warn("Failed to serialize closed orders: {}", e.getMessage());
                }
            }

            riskEventRepository.save(riskEvent);
            actionsTaken.add("Risk event logged");


            logger.error("RISK ALERT: User {} exceeded daily risk limit. " +
                            "Loss: {} ({}%). Actions: {}",
                    user.getClientId(), lossAmount, lossPercentage, String.join(", ", actionsTaken));

        } catch (Exception e) {
            logger.error("Error handling risk exceeded for user {}: {}", user.getClientId(), e.getMessage());
            throw new RuntimeException("Error handling risk exceeded", e);
        }

        return actionsTaken;
    }

    public List<RiskCheckResponse> checkAllUsersRisk() {
        List<RiskCheckResponse> results = new ArrayList<>();

        try {

            List<User> users = userService.getAllActiveUsers();

            logger.info("Checking risk for {} active users", users.size());

            for (User user : users) {
                try {
                    RiskCheckResponse riskResult = checkUserRisk(user);
                    results.add(riskResult);

                } catch (Exception e) {
                    logger.error("Error checking risk for user {}: {}", user.getClientId(), e.getMessage());
                    RiskCheckResponse errorResult = new RiskCheckResponse("error", e.getMessage(), "ERROR");
                    errorResult.setUserId(user.getId().toString());
                    errorResult.setClientId(user.getClientId());
                    results.add(errorResult);
                }
            }

            return results;

        } catch (Exception e) {
            logger.error("Error in checkAllUsersRisk: {}", e.getMessage());
            throw new RuntimeException("Error checking all users risk", e);
        }
    }

    public boolean resetUserTradingStatus(String clientId) {
        try {
            var userOpt = userService.getUserEntityByClientId(clientId);
            if (userOpt.isEmpty()) {
                logger.warn("User not found for reset: {}", clientId);
                return false;
            }

            User user = userOpt.get();
            userService.updateTradingStatus(user, true);
            logger.info("Trading status reset for user: {}", clientId);
            return true;

        } catch (Exception e) {
            logger.error("Error resetting trading status for user {}: {}", clientId, e.getMessage());
            return false;
        }
    }

    public int resetDailyTrading() {
        try {
            LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);


            List<User> users = userService.getAllActiveUsers().stream()
                    .filter(user -> !user.getTradingEnabled())
                    .toList();

            int resetCount = 0;
            for (User user : users) {

                var recentEvent = riskEventRepository.findLatestTradingDisabledEvent(user, nowUtc);

                if (recentEvent.isPresent() && 
                    recentEvent.get().getTradingDisabledUntil().isBefore(nowUtc)) {
                    userService.updateTradingStatus(user, true);
                    resetCount++;
                    logger.info("Re-enabled trading for user {}", user.getClientId());
                }
            }

            if (resetCount > 0) {
                logger.info("Reset trading for {} users", resetCount);
            }

            return resetCount;

        } catch (Exception e) {
            logger.error("Error resetting daily trading: {}", e.getMessage());
            throw new RuntimeException("Error resetting daily trading", e);
        }
    }

    @Transactional(readOnly = true)
    public List<RiskEvent> getAllRiskEvents() {
        return riskEventRepository.findAllOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<RiskEvent> getUserRiskEvents(String clientId) {
        User user = userService.getUserEntityByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return riskEventRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
