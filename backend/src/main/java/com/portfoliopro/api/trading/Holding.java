package com.portfoliopro.api.trading;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.portfoliopro.api.market.Stock;
import com.portfoliopro.api.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "holdings", uniqueConstraints =
        @UniqueConstraint(name = "uk_holding_user_stock", columnNames = {"user_id", "stock_id"}))
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private long quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal averagePurchasePrice;

    protected Holding() {
    }

    public Holding(User user, Stock stock, long quantity, BigDecimal averagePurchasePrice) {
        this.user = user;
        this.stock = stock;
        this.quantity = quantity;
        this.averagePurchasePrice = averagePurchasePrice.setScale(4, RoundingMode.HALF_UP);
    }

    public void add(long addedQuantity, BigDecimal purchasePrice) {
        long newQuantity = Math.addExact(quantity, addedQuantity);
        BigDecimal oldCost = averagePurchasePrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal addedCost = purchasePrice.multiply(BigDecimal.valueOf(addedQuantity));
        averagePurchasePrice = oldCost.add(addedCost)
                .divide(BigDecimal.valueOf(newQuantity), 4, RoundingMode.HALF_UP);
        quantity = newQuantity;
    }

    public void reduce(long soldQuantity) {
        quantity -= soldQuantity;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Stock getStock() { return stock; }
    public long getQuantity() { return quantity; }
    public BigDecimal getAveragePurchasePrice() { return averagePurchasePrice; }
}