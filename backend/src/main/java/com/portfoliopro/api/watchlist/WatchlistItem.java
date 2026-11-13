package com.portfoliopro.api.watchlist;

import com.portfoliopro.api.market.Stock;
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
@Table(name = "watchlist_items", uniqueConstraints =
        @UniqueConstraint(name = "uk_watchlist_stock", columnNames = {"watchlist_id", "stock_id"}))
public class WatchlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "watchlist_id", nullable = false)
    private Watchlist watchlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    protected WatchlistItem() {
    }

    public WatchlistItem(Watchlist watchlist, Stock stock) {
        this.watchlist = watchlist;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public Watchlist getWatchlist() { return watchlist; }
    public Stock getStock() { return stock; }
}