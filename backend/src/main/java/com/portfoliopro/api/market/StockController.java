package com.portfoliopro.api.market;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public List<StockResponse> search(@RequestParam(required = false) String query) {
        return stockService.search(query);
    }

    @GetMapping("/{symbol}")
    public StockResponse details(@PathVariable String symbol) {
        return stockService.details(symbol);
    }

    @GetMapping("/{symbol}/history")
    public List<PriceHistoryResponse> history(@PathVariable String symbol) {
        return stockService.history(symbol);
    }
}