package com.portfoliopro.api.market;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySymbolIgnoreCase(String symbol);

    boolean existsBySymbol(String symbol);

    List<Stock> findAllByOrderBySymbolAsc();

    @Query("select s from Stock s where "
            + "lower(s.symbol) like lower(concat('%', :query, '%')) or "
            + "lower(s.companyName) like lower(concat('%', :query, '%')) or "
            + "lower(s.sector) like lower(concat('%', :query, '%')) "
            + "order by s.symbol")
    List<Stock> search(@Param("query") String query);
}