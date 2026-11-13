package com.portfoliopro.api.market;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "stocks")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(nullable = false, length = 120)
    private String companyName;

    @Column(nullable = false, length = 20)
    private String exchange;

    @Column(nullable = false, length = 60)
    private String sector;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentPrice;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Stock() {
    }

    public Stock(String symbol, String companyName, String exchange, String sector, BigDecimal currentPrice) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.exchange = exchange;
        this.sector = sector;
        this.currentPrice = currentPrice;
    }

    public void updatePrice(BigDecimal price) {
        this.currentPrice = price;
    }

    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getSymbol() { return symbol; }
    public String getCompanyName() { return companyName; }
    public String getExchange() { return exchange; }
    public String getSector() { return sector; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}