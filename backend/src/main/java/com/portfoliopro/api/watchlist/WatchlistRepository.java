package com.portfoliopro.api.watchlist;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    Optional<Watchlist> findByUser_Id(Long userId);
}