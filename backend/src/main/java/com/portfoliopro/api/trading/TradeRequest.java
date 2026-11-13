package com.portfoliopro.api.trading;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TradeRequest(
        @NotBlank @Size(max = 20) String symbol,
        @NotNull @Positive @Max(1_000_000) Long quantity) {
}