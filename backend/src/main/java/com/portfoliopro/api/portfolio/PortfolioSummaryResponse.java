package com.portfoliopro.api.portfolio;

import java.math.BigDecimal;

public record PortfolioSummaryResponse(
        BigDecimal walletBalance,
        BigDecimal investedValue,
        BigDecimal currentValue,
        BigDecimal realizedPnl,
        BigDecimal unrealizedPnl,
        BigDecimal totalAccountValue,
        String currency,
        boolean simulated) {
}