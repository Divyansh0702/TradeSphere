package com.portfoliopro.api.watchlist;

import java.time.LocalDateTime;
import java.util.List;

import com.portfoliopro.api.market.StockResponse;

public record WatchlistResponse(Long id, LocalDateTime createdAt, List<StockResponse> stocks) {
}