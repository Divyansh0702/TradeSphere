package com.portfoliopro.api.trading;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record HoldingResponse(
        String symbol,
        String companyName,
        long quantity,
        BigDecimal averagePurchasePrice,
        BigDecimal currentPrice,
        String currency,
        BigDecimal investedValue,
        BigDecimal currentValue,
        BigDecimal unrealizedPnl) {
    public static HoldingResponse from(Holding holding) {
        BigDecimal quantity = BigDecimal.valueOf(holding.getQuantity());
        BigDecimal invested = holding.getAveragePurchasePrice().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        BigDecimal current = holding.getStock().getCurrentPrice().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        return new HoldingResponse(holding.getStock().getSymbol(), holding.getStock().getCompanyName(),
                holding.getQuantity(), holding.getAveragePurchasePrice(), holding.getStock().getCurrentPrice(),
            "INR", invested, current, current.subtract(invested));
    }
}