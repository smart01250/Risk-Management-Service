package com.assessment.riskmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Column(name = "symbol", nullable = false, length = 20)
    @NotBlank
    private String symbol;

    @Column(name = "strategy", nullable = false, length = 100)
    @NotBlank
    private String strategy;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    @NotNull
    private OrderSide side;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 8)
    @NotNull
    @Positive
    private BigDecimal quantity;

    @Column(name = "price", precision = 19, scale = 8)
    private BigDecimal price;

    @Column(name = "stop_loss_percentage", precision = 5, scale = 2)
    private BigDecimal stopLossPercentage;

    @Column(name = "max_risk_per_day_percentage", precision = 5, scale = 2)
    private BigDecimal maxRiskPerDayPercentage;

    @Column(name = "inverse", nullable = false)
    @NotNull
    private Boolean inverse = false;

    @Column(name = "pyramid", nullable = false)
    @NotNull
    private Boolean pyramid = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "kraken_order_id", length = 100)
    private String krakenOrderId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    // Constructors
    public Order() {}

    public Order(User user, String symbol, String strategy, OrderSide side, BigDecimal quantity) {
        this.user = user;
        this.symbol = symbol;
        this.strategy = strategy;
        this.side = side;
        this.quantity = quantity;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getStopLossPercentage() { return stopLossPercentage; }
    public void setStopLossPercentage(BigDecimal stopLossPercentage) { this.stopLossPercentage = stopLossPercentage; }

    public BigDecimal getMaxRiskPerDayPercentage() { return maxRiskPerDayPercentage; }
    public void setMaxRiskPerDayPercentage(BigDecimal maxRiskPerDayPercentage) { this.maxRiskPerDayPercentage = maxRiskPerDayPercentage; }

    public Boolean getInverse() { return inverse; }
    public void setInverse(Boolean inverse) { this.inverse = inverse; }

    public Boolean getPyramid() { return pyramid; }
    public void setPyramid(Boolean pyramid) { this.pyramid = pyramid; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getKrakenOrderId() { return krakenOrderId; }
    public void setKrakenOrderId(String krakenOrderId) { this.krakenOrderId = krakenOrderId; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", side=" + side +
                ", quantity=" + quantity +
                ", status=" + status +
                '}';
    }
}
