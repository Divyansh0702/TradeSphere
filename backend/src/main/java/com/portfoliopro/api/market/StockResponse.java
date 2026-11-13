package com.portfoliopro.api.market;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockResponse(
        Long id,
        String symbol,
        String companyName,
        String exchange,
        String sector,
        BigDecimal currentPrice,
        String currency,
        LocalDateTime updatedAt) {
    public static StockResponse from(Stock stock) {
        return new StockResponse(stock.getId(), stock.getSymbol(), stock.getCompanyName(), stock.getExchange(),
            stock.getSector(), stock.getCurrentPrice(), "INR", stock.getUpdatedAt());
    }
}