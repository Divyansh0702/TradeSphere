package com.portfoliopro.api.wallet;

import java.math.BigDecimal;

public record WalletResponse(BigDecimal balance, String currency, boolean simulated) {
}