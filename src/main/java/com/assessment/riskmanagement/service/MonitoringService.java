package com.assessment.riskmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
public class MonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringService.class);

    @Autowired
    private RiskService riskService;

    private boolean monitoringEnabled = true;
    private LocalDateTime lastMonitoringRun;
    private int totalUsersChecked = 0;
    private int totalRiskEventsTriggered = 0;

    @Scheduled(fixedRateString = "${risk-management.monitoring.check-interval-seconds:30}000")
    public void performRiskMonitoring() {
        if (!monitoringEnabled) {
            return;
        }

        try {
            logger.debug("Starting scheduled risk monitoring...");
            lastMonitoringRun = LocalDateTime.now(ZoneOffset.UTC);


            List<Map<String, Object>> results = riskService.checkAllUsersRisk();
            
            int usersChecked = results.size();
            int riskEventsTriggered = (int) results.stream()
                    .mapToLong(result -> {
                        Boolean riskExceeded = (Boolean) result.get("risk_exceeded");
                        return (riskExceeded != null && riskExceeded) ? 1 : 0;
                    })
                    .sum();

            totalUsersChecked += usersChecked;
            totalRiskEventsTriggered += riskEventsTriggered;

            if (riskEventsTriggered > 0) {
                logger.warn("Risk monitoring completed: {} users checked, {} risk events triggered", 
                        usersChecked, riskEventsTriggered);
            } else {
                logger.debug("Risk monitoring completed: {} users checked, no risk events", usersChecked);
            }

        } catch (Exception e) {
            logger.error("Error during scheduled risk monitoring: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 1 0 * * *", zone = "UTC")
    public void performDailyReset() {
        try {
            logger.info("Starting daily trading reset...");
            
            int resetCount = riskService.resetDailyTrading();
            
            logger.info("Daily trading reset completed: {} users re-enabled", resetCount);
            
        } catch (Exception e) {
            logger.error("Error during daily trading reset: {}", e.getMessage(), e);
        }
    }

    public Map<String, Object> getMonitoringStatus() {
        return Map.of(
                "monitoring_enabled", monitoringEnabled,
                "last_monitoring_run", lastMonitoringRun,
                "total_users_checked", totalUsersChecked,
                "total_risk_events_triggered", totalRiskEventsTriggered,
                "current_time_utc", LocalDateTime.now(ZoneOffset.UTC)
        );
    }

    public void enableMonitoring() {
        monitoringEnabled = true;
        logger.info("Risk monitoring enabled");
    }

    public void disableMonitoring() {
        monitoringEnabled = false;
        logger.info("Risk monitoring disabled");
    }

    public boolean isMonitoringEnabled() {
        return monitoringEnabled;
    }
}
