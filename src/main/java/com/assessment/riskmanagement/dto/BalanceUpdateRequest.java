package com.assessment.riskmanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class BalanceUpdateRequest {
    
    @NotNull(message = "Current balance is required")
    @DecimalMin(value = "0.0", message = "Current balance must be non-negative")
    private BigDecimal currentBalance;

    public BalanceUpdateRequest() {}

    public BalanceUpdateRequest(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public BigDecimal getCurrentBalance() { 
        return currentBalance; 
    }
    
    public void setCurrentBalance(BigDecimal currentBalance) { 
        this.currentBalance = currentBalance; 
    }

    @Override
    public String toString() {
        return "BalanceUpdateRequest{" +
                "currentBalance=" + currentBalance +
                '}';
    }
}
