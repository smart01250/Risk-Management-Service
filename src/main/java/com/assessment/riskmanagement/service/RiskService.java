package com.assessment.riskmanagement.service;

import com.assessment.riskmanagement.client.KrakenClient;
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

    public Map<String, Object> checkUserRisk(User user) {
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


            Map<String, Object> riskResult = new HashMap<>();
            riskResult.put("user_id", user.getId().toString());
            riskResult.put("client_id", user.getClientId());
            riskResult.put("current_balance", currentBalance);
            riskResult.put("initial_balance", user.getInitialBalance());
            riskResult.put("risk_exceeded", false);
            riskResult.put("actions_taken", new ArrayList<String>());
            riskResult.put("risk_events", new ArrayList<>());

            if (user.getInitialBalance() == null) {
                user.setInitialBalance(currentBalance);
                userService.updateLastRiskCheck(user);
                logger.info("Set initial balance for user {}: {}", user.getClientId(), currentBalance);
                return riskResult;
            }


            BigDecimal lossAmount = user.getInitialBalance().subtract(currentBalance);
            BigDecimal lossPercentage = BigDecimal.ZERO;
            if (user.getInitialBalance().compareTo(BigDecimal.ZERO) > 0) {
                lossPercentage = lossAmount.divide(user.getInitialBalance(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            riskResult.put("loss_amount", lossAmount);
            riskResult.put("loss_percentage", lossPercentage);


            boolean riskExceeded = false;
            BigDecimal riskThreshold = null;
            String riskType = null;

            if (user.getDailyRiskAbsolute() != null && 
                lossAmount.compareTo(user.getDailyRiskAbsolute()) >= 0) {
                riskExceeded = true;
                riskThreshold = user.getDailyRiskAbsolute();
                riskType = "absolute";
            } else if (user.getDailyRiskPercentage() != null && 
                       lossPercentage.compareTo(user.getDailyRiskPercentage()) >= 0) {
                riskExceeded = true;
                riskThreshold = user.getDailyRiskPercentage();
                riskType = "percentage";
            }

            if (riskExceeded) {
                logger.warn("Risk limit exceeded for user {}: {} threshold {}", 
                        user.getClientId(), riskType, riskThreshold);


                List<String> actionsTaken = handleRiskExceeded(
                        user, currentBalance, lossAmount, lossPercentage, riskThreshold, riskType
                );

                riskResult.put("risk_exceeded", true);
                riskResult.put("risk_threshold", riskThreshold);
                riskResult.put("risk_type", riskType);
                riskResult.put("actions_taken", actionsTaken);
            }


            userService.updateLastRiskCheck(user);

            return riskResult;

        } catch (Exception e) {
            logger.error("Error checking risk for user {}: {}", user.getClientId(), e.getMessage());
            throw new RuntimeException("Error checking risk for user", e);
        }
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

    public List<Map<String, Object>> checkAllUsersRisk() {
        List<Map<String, Object>> results = new ArrayList<>();

        try {

            List<User> users = userService.getAllActiveUsers();

            logger.info("Checking risk for {} active users", users.size());

            for (User user : users) {
                try {
                    Map<String, Object> riskResult = checkUserRisk(user);
                    results.add(riskResult);

                } catch (Exception e) {
                    logger.error("Error checking risk for user {}: {}", user.getClientId(), e.getMessage());
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("user_id", user.getId().toString());
                    errorResult.put("client_id", user.getClientId());
                    errorResult.put("error", e.getMessage());
                    results.add(errorResult);
                }
            }

            return results;

        } catch (Exception e) {
            logger.error("Error in checkAllUsersRisk: {}", e.getMessage());
            throw new RuntimeException("Error checking all users risk", e);
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
