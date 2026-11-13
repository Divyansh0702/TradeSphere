package com.portfoliopro.api.watchlist;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {
    List<WatchlistItem> findAllByWatchlist_IdOrderByStock_SymbolAsc(Long watchlistId);

    Optional<WatchlistItem> findByWatchlist_IdAndStock_Id(Long watchlistId, Long stockId);

    void deleteByWatchlist_IdAndStock_Id(Long watchlistId, Long stockId);
}