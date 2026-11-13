package com.portfoliopro.api.market;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StockService {
    private final StockRepository stockRepository;
    private final StockPriceHistoryRepository historyRepository;

    public StockService(StockRepository stockRepository, StockPriceHistoryRepository historyRepository) {
        this.stockRepository = stockRepository;
        this.historyRepository = historyRepository;
    }

    public List<StockResponse> search(String query) {
        List<Stock> stocks = query == null || query.isBlank()
                ? stockRepository.findAllByOrderBySymbolAsc()
                : stockRepository.search(query.trim());
        return stocks.stream().map(StockResponse::from).toList();
    }

    public StockResponse details(String symbol) {
        return StockResponse.from(requireStock(symbol));
    }

    public List<PriceHistoryResponse> history(String symbol) {
        requireStock(symbol);
        return historyRepository.findTop100ByStock_SymbolIgnoreCaseOrderByRecordedAtDesc(symbol.trim())
                .stream().map(PriceHistoryResponse::from).toList();
    }

    Stock requireStock(String symbol) {
        return stockRepository.findBySymbolIgnoreCase(symbol.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new StockNotFoundException(symbol));
    }
}