package com.portfoliopro.api.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        BigDecimal walletBalance,
        String currency,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(),
            user.getWalletBalance(), "INR", user.getCreatedAt(), user.getUpdatedAt());
    }
}