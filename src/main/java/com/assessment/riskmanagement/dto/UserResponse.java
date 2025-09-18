package com.assessment.riskmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserResponse {
    
    private String clientId;
    private Boolean isActive;
    private Boolean tradingEnabled;
    private BigDecimal dailyRiskAbsolute;
    private BigDecimal dailyRiskPercentage;
    private BigDecimal initialBalance;
    private LocalDateTime createdAt;


    public UserResponse() {}

    public UserResponse(String clientId, Boolean isActive, Boolean tradingEnabled, 
                       BigDecimal dailyRiskAbsolute, BigDecimal dailyRiskPercentage, 
                       BigDecimal initialBalance, LocalDateTime createdAt) {
        this.clientId = clientId;
        this.isActive = isActive;
        this.tradingEnabled = tradingEnabled;
        this.dailyRiskAbsolute = dailyRiskAbsolute;
        this.dailyRiskPercentage = dailyRiskPercentage;
        this.initialBalance = initialBalance;
        this.createdAt = createdAt;
    }


    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getTradingEnabled() { return tradingEnabled; }
    public void setTradingEnabled(Boolean tradingEnabled) { this.tradingEnabled = tradingEnabled; }

    public BigDecimal getDailyRiskAbsolute() { return dailyRiskAbsolute; }
    public void setDailyRiskAbsolute(BigDecimal dailyRiskAbsolute) { this.dailyRiskAbsolute = dailyRiskAbsolute; }

    public BigDecimal getDailyRiskPercentage() { return dailyRiskPercentage; }
    public void setDailyRiskPercentage(BigDecimal dailyRiskPercentage) { this.dailyRiskPercentage = dailyRiskPercentage; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
