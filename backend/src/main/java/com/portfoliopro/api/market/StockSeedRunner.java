package com.portfoliopro.api.market;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StockSeedRunner implements CommandLineRunner {
    private record Seed(String symbol, String company, String sector, String price) { }

    private static final List<Seed> STOCKS = List.of(
            new Seed("RELIANCE", "Reliance Industries", "Energy", "2940.00"),
            new Seed("TCS", "Tata Consultancy Services", "Information Technology", "4120.00"),
            new Seed("INFY", "Infosys", "Information Technology", "1850.00"),
            new Seed("HDFCBANK", "HDFC Bank", "Banking", "1690.00"),
            new Seed("ICICIBANK", "ICICI Bank", "Banking", "1280.00"),
            new Seed("ITC", "ITC Limited", "Consumer Goods", "435.00"),
            new Seed("SBIN", "State Bank of India", "Banking", "820.00"),
            new Seed("BHARTIARTL", "Bharti Airtel", "Telecommunications", "1720.00"),
            new Seed("LT", "Larsen & Toubro", "Infrastructure", "3580.00"),
            new Seed("HINDUNILVR", "Hindustan Unilever", "Consumer Goods", "2450.00"),
            new Seed("AXISBANK", "Axis Bank", "Banking", "1190.00"),
            new Seed("KOTAKBANK", "Kotak Mahindra Bank", "Banking", "1840.00"),
            new Seed("WIPRO", "Wipro", "Information Technology", "540.00"),
            new Seed("TATAMOTORS", "Tata Motors", "Automobile", "980.00"),
            new Seed("MARUTI", "Maruti Suzuki India", "Automobile", "12600.00")
    );

    private final StockRepository stockRepository;
    private final StockPriceHistoryRepository historyRepository;

    public StockSeedRunner(StockRepository stockRepository, StockPriceHistoryRepository historyRepository) {
        this.stockRepository = stockRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (Seed seed : STOCKS) {
            if (stockRepository.existsBySymbol(seed.symbol())) continue;
            Stock stock = stockRepository.save(new Stock(seed.symbol(), seed.company(), "NSE", seed.sector(),
                    new BigDecimal(seed.price())));
            historyRepository.save(new StockPriceHistory(stock, stock.getCurrentPrice()));
        }
    }
}