package com.assessment.riskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Risk check response")
public class RiskCheckResponse {

    @Schema(description = "Response status", example = "success")
    private String status;

    @Schema(description = "Response message", example = "Risk check completed")
    private String message;

    @Schema(description = "Risk status", example = "SAFE", allowableValues = {"SAFE", "AT_LIMIT", "EXCEEDED"})
    @JsonProperty("risk_status")
    private String riskStatus;

    @Schema(description = "Current account balance", example = "48500.00")
    @JsonProperty("current_balance")
    private BigDecimal currentBalance;

    @Schema(description = "Initial account balance", example = "50000.00")
    @JsonProperty("initial_balance")
    private BigDecimal initialBalance;

    @Schema(description = "Daily loss amount", example = "1500.00")
    @JsonProperty("daily_loss")
    private BigDecimal dailyLoss;

    @Schema(description = "Daily loss percentage", example = "3.0")
    @JsonProperty("daily_loss_percentage")
    private BigDecimal dailyLossPercentage;

    @Schema(description = "Risk percentage limit", example = "2.5")
    @JsonProperty("risk_percentage_limit")
    private BigDecimal riskPercentageLimit;

    @Schema(description = "Risk absolute limit", example = "1000.00")
    @JsonProperty("risk_absolute_limit")
    private BigDecimal riskAbsoluteLimit;

    @Schema(description = "Action taken", example = "none")
    @JsonProperty("action_taken")
    private String actionTaken;

    @Schema(description = "Number of positions closed", example = "0")
    @JsonProperty("positions_closed")
    private Integer positionsClosed;

    @Schema(description = "Response timestamp", example = "2025-09-18T14:30:00Z")
    private LocalDateTime timestamp;

    @Schema(description = "Client ID", example = "CLIENT_ABC123XYZ")
    @JsonProperty("client_id")
    private String clientId;

    @Schema(description = "User ID")
    @JsonProperty("user_id")
    private String userId;

    @Schema(description = "Risk events")
    @JsonProperty("risk_events")
    private List<Object> riskEvents;

    // Constructors
    public RiskCheckResponse() {}

    public RiskCheckResponse(String status, String message, String riskStatus) {
        this.status = status;
        this.message = message;
        this.riskStatus = riskStatus;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRiskStatus() { return riskStatus; }
    public void setRiskStatus(String riskStatus) { this.riskStatus = riskStatus; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    public BigDecimal getDailyLoss() { return dailyLoss; }
    public void setDailyLoss(BigDecimal dailyLoss) { this.dailyLoss = dailyLoss; }

    public BigDecimal getDailyLossPercentage() { return dailyLossPercentage; }
    public void setDailyLossPercentage(BigDecimal dailyLossPercentage) { this.dailyLossPercentage = dailyLossPercentage; }

    public BigDecimal getRiskPercentageLimit() { return riskPercentageLimit; }
    public void setRiskPercentageLimit(BigDecimal riskPercentageLimit) { this.riskPercentageLimit = riskPercentageLimit; }

    public BigDecimal getRiskAbsoluteLimit() { return riskAbsoluteLimit; }
    public void setRiskAbsoluteLimit(BigDecimal riskAbsoluteLimit) { this.riskAbsoluteLimit = riskAbsoluteLimit; }

    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }

    public Integer getPositionsClosed() { return positionsClosed; }
    public void setPositionsClosed(Integer positionsClosed) { this.positionsClosed = positionsClosed; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<Object> getRiskEvents() { return riskEvents; }
    public void setRiskEvents(List<Object> riskEvents) { this.riskEvents = riskEvents; }
}
