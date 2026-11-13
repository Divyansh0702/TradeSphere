package com.portfoliopro.api.portfolio;

import java.util.List;

import com.portfoliopro.api.trading.HoldingResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/holdings")
    public List<HoldingResponse> holdings(Authentication authentication) {
        return portfolioService.holdings(authentication.getName());
    }

    @GetMapping("/summary")
    public PortfolioSummaryResponse summary(Authentication authentication) {
        return portfolioService.summary(authentication.getName());
    }
}