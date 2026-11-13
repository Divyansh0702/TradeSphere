package com.portfoliopro.api.market;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_price_history", indexes = @Index(name = "idx_price_history_stock_time", columnList = "stock_id, recorded_at"))
public class StockPriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    protected StockPriceHistory() {
    }

    public StockPriceHistory(Stock stock, BigDecimal price) {
        this.stock = stock;
        this.price = price;
    }

    @PrePersist
    void onCreate() {
        recordedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Stock getStock() { return stock; }
    public BigDecimal getPrice() { return price; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}