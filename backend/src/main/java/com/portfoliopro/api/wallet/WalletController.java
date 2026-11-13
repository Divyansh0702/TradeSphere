package com.portfoliopro.api.wallet;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public WalletResponse get(Authentication authentication) {
        return walletService.get(authentication.getName());
    }

    @PostMapping("/top-ups")
    public WalletResponse topUp(Authentication authentication, @Valid @RequestBody TopUpRequest request) {
        return walletService.topUp(authentication.getName(), request);
    }
}