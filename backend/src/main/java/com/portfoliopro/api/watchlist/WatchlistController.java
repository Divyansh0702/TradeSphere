package com.portfoliopro.api.watchlist;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/watchlists")
public class WatchlistController {
    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @PostMapping
    public ResponseEntity<WatchlistResponse> create(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(watchlistService.create(authentication.getName()));
    }

    @GetMapping("/me")
    public WatchlistResponse get(Authentication authentication) {
        return watchlistService.get(authentication.getName());
    }

    @PostMapping("/me/stocks/{symbol}")
    public WatchlistResponse addStock(Authentication authentication, @PathVariable String symbol) {
        return watchlistService.addStock(authentication.getName(), symbol);
    }

    @DeleteMapping("/me/stocks/{symbol}")
    public ResponseEntity<Void> removeStock(Authentication authentication, @PathVariable String symbol) {
        watchlistService.removeStock(authentication.getName(), symbol);
        return ResponseEntity.noContent().build();
    }
}