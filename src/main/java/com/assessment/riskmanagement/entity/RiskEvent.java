package com.assessment.riskmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "risk_events")
@EntityListeners(AuditingEntityListener.class)
public class RiskEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    @NotNull
    private RiskEventType eventType;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String description;

    @Column(name = "current_balance", precision = 19, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "initial_balance", precision = 19, scale = 2)
    private BigDecimal initialBalance;

    @Column(name = "risk_threshold", precision = 19, scale = 2)
    private BigDecimal riskThreshold;

    @Column(name = "loss_amount", precision = 19, scale = 2)
    private BigDecimal lossAmount;

    @Column(name = "loss_percentage", precision = 5, scale = 2)
    private BigDecimal lossPercentage;

    @Column(name = "orders_closed", columnDefinition = "TEXT")
    private String ordersClosed;

    @Column(name = "trading_disabled_until")
    private LocalDateTime tradingDisabledUntil;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    public RiskEvent() {}

    public RiskEvent(User user, RiskEventType eventType, String description) {
        this.user = user;
        this.eventType = eventType;
        this.description = description;
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public RiskEventType getEventType() { return eventType; }
    public void setEventType(RiskEventType eventType) { this.eventType = eventType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    public BigDecimal getRiskThreshold() { return riskThreshold; }
    public void setRiskThreshold(BigDecimal riskThreshold) { this.riskThreshold = riskThreshold; }

    public BigDecimal getLossAmount() { return lossAmount; }
    public void setLossAmount(BigDecimal lossAmount) { this.lossAmount = lossAmount; }

    public BigDecimal getLossPercentage() { return lossPercentage; }
    public void setLossPercentage(BigDecimal lossPercentage) { this.lossPercentage = lossPercentage; }

    public String getOrdersClosed() { return ordersClosed; }
    public void setOrdersClosed(String ordersClosed) { this.ordersClosed = ordersClosed; }

    public LocalDateTime getTradingDisabledUntil() { return tradingDisabledUntil; }
    public void setTradingDisabledUntil(LocalDateTime tradingDisabledUntil) { this.tradingDisabledUntil = tradingDisabledUntil; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "RiskEvent{" +
                "id=" + id +
                ", eventType=" + eventType +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
