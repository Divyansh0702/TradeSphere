package com.portfoliopro.api.auth;

import com.portfoliopro.api.user.UserResponse;

public record AuthSession(String token, UserResponse user) {
}