package com.portfoliopro.api.auth;

import java.time.Duration;

import com.portfoliopro.api.auth.dto.AuthResponse;
import com.portfoliopro.api.auth.dto.LoginRequest;
import com.portfoliopro.api.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final long expirationMs;
    private final boolean secureCookie;
    private final String sameSite;

    public AuthController(
            AuthService authService,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.auth.cookie-secure}") boolean secureCookie,
            @Value("${app.auth.cookie-same-site}") String sameSite) {
        this.authService = authService;
        this.expirationMs = expirationMs;
        this.secureCookie = secureCookie;
        this.sameSite = sameSite;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthSession session = authService.register(request);
        return ResponseEntity.status(201)
                .header(HttpHeaders.SET_COOKIE, createCookie(session.token(), Duration.ofMillis(expirationMs)))
                .body(new AuthResponse(session.user()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthSession session = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createCookie(session.token(), Duration.ofMillis(expirationMs)))
                .body(new AuthResponse(session.user()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, createCookie("", Duration.ZERO)).build();
    }

    private String createCookie(String token, Duration maxAge) {
        return ResponseCookie.from(JwtAuthenticationFilter.COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/api")
                .maxAge(maxAge)
                .build()
                .toString();
    }
}