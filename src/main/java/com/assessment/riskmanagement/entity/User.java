package com.assessment.riskmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id", unique = true, nullable = false, length = 50)
    @NotBlank
    private String clientId;

    @Column(name = "kraken_api_key", nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String krakenApiKey;

    @Column(name = "kraken_private_key", nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String krakenPrivateKey;

    @Column(name = "daily_risk_absolute", precision = 19, scale = 2)
    private BigDecimal dailyRiskAbsolute;

    @Column(name = "daily_risk_percentage", precision = 5, scale = 2)
    private BigDecimal dailyRiskPercentage;

    @Column(name = "initial_balance", precision = 19, scale = 2)
    private BigDecimal initialBalance;

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;

    @Column(name = "trading_enabled", nullable = false)
    @NotNull
    private Boolean tradingEnabled = true;

    @Column(name = "last_risk_check")
    private LocalDateTime lastRiskCheck;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RiskEvent> riskEvents = new ArrayList<>();

    // Constructors
    public User() {}

    public User(String clientId, String krakenApiKey, String krakenPrivateKey) {
        this.clientId = clientId;
        this.krakenApiKey = krakenApiKey;
        this.krakenPrivateKey = krakenPrivateKey;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getKrakenApiKey() { return krakenApiKey; }
    public void setKrakenApiKey(String krakenApiKey) { this.krakenApiKey = krakenApiKey; }

    public String getKrakenPrivateKey() { return krakenPrivateKey; }
    public void setKrakenPrivateKey(String krakenPrivateKey) { this.krakenPrivateKey = krakenPrivateKey; }

    public BigDecimal getDailyRiskAbsolute() { return dailyRiskAbsolute; }
    public void setDailyRiskAbsolute(BigDecimal dailyRiskAbsolute) { this.dailyRiskAbsolute = dailyRiskAbsolute; }

    public BigDecimal getDailyRiskPercentage() { return dailyRiskPercentage; }
    public void setDailyRiskPercentage(BigDecimal dailyRiskPercentage) { this.dailyRiskPercentage = dailyRiskPercentage; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getTradingEnabled() { return tradingEnabled; }
    public void setTradingEnabled(Boolean tradingEnabled) { this.tradingEnabled = tradingEnabled; }

    public LocalDateTime getLastRiskCheck() { return lastRiskCheck; }
    public void setLastRiskCheck(LocalDateTime lastRiskCheck) { this.lastRiskCheck = lastRiskCheck; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }

    public List<RiskEvent> getRiskEvents() { return riskEvents; }
    public void setRiskEvents(List<RiskEvent> riskEvents) { this.riskEvents = riskEvents; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", clientId='" + clientId + '\'' +
                ", isActive=" + isActive +
                ", tradingEnabled=" + tradingEnabled +
                '}';
    }
}
