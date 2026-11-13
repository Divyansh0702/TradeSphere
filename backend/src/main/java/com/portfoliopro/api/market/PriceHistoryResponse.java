package com.portfoliopro.api.market;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceHistoryResponse(BigDecimal price, String currency, LocalDateTime recordedAt) {
    public static PriceHistoryResponse from(StockPriceHistory history) {
        return new PriceHistoryResponse(history.getPrice(), "INR", history.getRecordedAt());
    }
}