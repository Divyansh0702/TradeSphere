package com.portfoliopro.api.auth;

import java.math.BigDecimal;
import java.util.Locale;

import com.portfoliopro.api.auth.dto.LoginRequest;
import com.portfoliopro.api.auth.dto.RegisterRequest;
import com.portfoliopro.api.user.User;
import com.portfoliopro.api.user.UserDetailsServiceImpl;
import com.portfoliopro.api.user.UserRepository;
import com.portfoliopro.api.user.UserResponse;
import com.portfoliopro.api.user.UserRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UserDetailsServiceImpl userDetailsService,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthSession register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) throw new EmailAlreadyRegisteredException();

        User user = userRepository.save(new User(request.name().trim(), email,
                passwordEncoder.encode(request.password()), UserRole.USER, BigDecimal.ZERO));
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        return new AuthSession(jwtService.generateToken(userDetails), UserResponse.from(user));
    }

    public AuthSession login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated account was not found"));
        return new AuthSession(jwtService.generateToken(userDetails), UserResponse.from(user));
    }

    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Account not found"));
        return UserResponse.from(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}