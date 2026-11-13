package com.portfoliopro.api.trading;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class TradeController {
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/buy")
    public ResponseEntity<TradeOrderResponse> buy(Authentication authentication,
            @Valid @RequestBody TradeRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tradeService.buy(authentication.getName(), request, idempotencyKey));
    }

    @PostMapping("/sell")
    public ResponseEntity<TradeOrderResponse> sell(Authentication authentication,
            @Valid @RequestBody TradeRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tradeService.sell(authentication.getName(), request, idempotencyKey));
    }

    @GetMapping
    public List<TradeOrderResponse> history(Authentication authentication) {
        return tradeService.history(authentication.getName());
    }
}