package com.portfoliopro.api.trading;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeOrderResponse(
        Long id,
        String symbol,
        TradeSide side,
        long quantity,
        BigDecimal executionPrice,
        BigDecimal totalAmount,
        String currency,
        BigDecimal realizedPnl,
        OrderStatus status,
        LocalDateTime createdAt) {
    public static TradeOrderResponse from(TradeOrder order) {
        return new TradeOrderResponse(order.getId(), order.getStock().getSymbol(), order.getSide(),
            order.getQuantity(), order.getExecutionPrice(), order.getTotalAmount(), "INR", order.getRealizedPnl(),
                order.getStatus(), order.getCreatedAt());
    }
}