package com.portfoliopro.api.auth.dto;

import com.portfoliopro.api.user.UserResponse;

public record AuthResponse(UserResponse user) {
}