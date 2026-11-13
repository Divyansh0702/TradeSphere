package com.portfoliopro.api.market;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {
    List<StockPriceHistory> findTop100ByStock_SymbolIgnoreCaseOrderByRecordedAtDesc(String symbol);
}