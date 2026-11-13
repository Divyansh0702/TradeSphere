package com.portfoliopro.api.trading;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.portfoliopro.api.market.Stock;
import com.portfoliopro.api.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "trade_orders", uniqueConstraints =
        @UniqueConstraint(name = "uk_trade_order_user_idempotency", columnNames = {"user_id", "idempotency_key"}))
public class TradeOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private TradeSide side;

    @Column(nullable = false)
    private long quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal executionPrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal realizedPnl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private OrderStatus status;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected TradeOrder() {
    }

    public TradeOrder(User user, Stock stock, TradeSide side, long quantity, BigDecimal executionPrice,
            BigDecimal totalAmount, BigDecimal realizedPnl, String idempotencyKey) {
        this.user = user;
        this.stock = stock;
        this.side = side;
        this.quantity = quantity;
        this.executionPrice = executionPrice;
        this.totalAmount = totalAmount;
        this.realizedPnl = realizedPnl;
        this.idempotencyKey = idempotencyKey;
        this.status = OrderStatus.EXECUTED;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Stock getStock() { return stock; }
    public TradeSide getSide() { return side; }
    public long getQuantity() { return quantity; }
    public BigDecimal getExecutionPrice() { return executionPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getRealizedPnl() { return realizedPnl; }
    public OrderStatus getStatus() { return status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}