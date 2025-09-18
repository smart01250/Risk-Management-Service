package com.assessment.riskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class TradingSignalRequest {
    
    @NotBlank(message = "Client ID is required")
    private String clientId;
    
    @NotBlank(message = "Symbol is required")
    private String symbol;
    
    @NotBlank(message = "Strategy is required")
    private String strategy;
    
    @JsonProperty("maxriskperday%")
    private BigDecimal maxRiskPerDayPercent;
    
    @NotBlank(message = "Action is required")
    private String action;
    
    @NotNull(message = "Order quantity is required")
    @Positive(message = "Order quantity must be positive")
    private BigDecimal orderQty;
    
    private Boolean inverse = false;
    
    private Boolean pyramid = false;
    
    @JsonProperty("stopLoss%")
    private BigDecimal stopLossPercent;


    public TradingSignalRequest() {}


    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public BigDecimal getMaxRiskPerDayPercent() { return maxRiskPerDayPercent; }
    public void setMaxRiskPerDayPercent(BigDecimal maxRiskPerDayPercent) { this.maxRiskPerDayPercent = maxRiskPerDayPercent; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public BigDecimal getOrderQty() { return orderQty; }
    public void setOrderQty(BigDecimal orderQty) { this.orderQty = orderQty; }

    public Boolean getInverse() { return inverse; }
    public void setInverse(Boolean inverse) { this.inverse = inverse; }

    public Boolean getPyramid() { return pyramid; }
    public void setPyramid(Boolean pyramid) { this.pyramid = pyramid; }

    public BigDecimal getStopLossPercent() { return stopLossPercent; }
    public void setStopLossPercent(BigDecimal stopLossPercent) { this.stopLossPercent = stopLossPercent; }

    @Override
    public String toString() {
        return "TradingSignalRequest{" +
                "clientId='" + clientId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", strategy='" + strategy + '\'' +
                ", action='" + action + '\'' +
                ", orderQty=" + orderQty +
                ", inverse=" + inverse +
                ", pyramid=" + pyramid +
                '}';
    }
}
