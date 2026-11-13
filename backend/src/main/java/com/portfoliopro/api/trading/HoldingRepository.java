package com.portfoliopro.api.trading;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    Optional<Holding> findByUser_IdAndStock_Id(Long userId, Long stockId);

    List<Holding> findAllByUser_IdOrderByStock_SymbolAsc(Long userId);
}