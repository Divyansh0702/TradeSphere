package com.portfoliopro.api.market;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.market.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class MarketSimulationScheduler {
    private final MarketSimulationService marketSimulationService;

    public MarketSimulationScheduler(MarketSimulationService marketSimulationService) {
        this.marketSimulationService = marketSimulationService;
    }

    @Scheduled(fixedDelayString = "${app.market.simulation.interval-ms:7000}")
    public void updatePrices() {
        marketSimulationService.updatePrices();
    }
}