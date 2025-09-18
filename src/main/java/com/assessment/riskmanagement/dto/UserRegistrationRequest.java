package com.assessment.riskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public class UserRegistrationRequest {
    
    @NotBlank(message = "Kraken API key is required")
    private String krakenApiKey;
    
    @NotBlank(message = "Kraken private key is required")
    private String krakenPrivateKey;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Daily risk absolute must be positive")
    private BigDecimal dailyRiskAbsolute;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Daily risk percentage must be positive")
    private BigDecimal dailyRiskPercentage;


    public UserRegistrationRequest() {}

    public UserRegistrationRequest(String krakenApiKey, String krakenPrivateKey) {
        this.krakenApiKey = krakenApiKey;
        this.krakenPrivateKey = krakenPrivateKey;
    }


    public String getKrakenApiKey() { return krakenApiKey; }
    public void setKrakenApiKey(String krakenApiKey) { this.krakenApiKey = krakenApiKey; }

    public String getKrakenPrivateKey() { return krakenPrivateKey; }
    public void setKrakenPrivateKey(String krakenPrivateKey) { this.krakenPrivateKey = krakenPrivateKey; }

    public BigDecimal getDailyRiskAbsolute() { return dailyRiskAbsolute; }
    public void setDailyRiskAbsolute(BigDecimal dailyRiskAbsolute) { this.dailyRiskAbsolute = dailyRiskAbsolute; }

    public BigDecimal getDailyRiskPercentage() { return dailyRiskPercentage; }
    public void setDailyRiskPercentage(BigDecimal dailyRiskPercentage) { this.dailyRiskPercentage = dailyRiskPercentage; }
}
