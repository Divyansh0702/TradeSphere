package com.portfoliopro.api.market;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketSimulationService {
    private static final BigDecimal BASIS_POINT_FACTOR = new BigDecimal("10000");
    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");

    private final StockRepository stockRepository;
    private final StockPriceHistoryRepository historyRepository;

    public MarketSimulationService(StockRepository stockRepository, StockPriceHistoryRepository historyRepository) {
        this.stockRepository = stockRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public void updatePrices() {
        for (Stock stock : stockRepository.findAll()) {
            int movementBasisPoints = ThreadLocalRandom.current().nextInt(-150, 151);
            BigDecimal factor = BASIS_POINT_FACTOR.add(BigDecimal.valueOf(movementBasisPoints))
                    .divide(BASIS_POINT_FACTOR, 6, RoundingMode.HALF_UP);
            BigDecimal updatedPrice = stock.getCurrentPrice().multiply(factor)
                    .max(MIN_PRICE).setScale(2, RoundingMode.HALF_UP);
            stock.updatePrice(updatedPrice);
            historyRepository.save(new StockPriceHistory(stock, updatedPrice));
        }
    }
}