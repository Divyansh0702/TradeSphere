package com.portfoliopro.api.wallet;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record TopUpRequest(
        @NotNull @DecimalMin("1.00") @DecimalMax("10000000.00") @Digits(integer = 8, fraction = 2)
        BigDecimal amount) {
}