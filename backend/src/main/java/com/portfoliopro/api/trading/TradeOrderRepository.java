package com.portfoliopro.api.trading;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {
    Optional<TradeOrder> findByUser_IdAndIdempotencyKey(Long userId, String idempotencyKey);

    List<TradeOrder> findTop100ByUser_IdOrderByCreatedAtDesc(Long userId);

    @Query("select coalesce(sum(o.realizedPnl), 0) from TradeOrder o where o.user.id = :userId")
    BigDecimal sumRealizedPnlByUserId(@Param("userId") Long userId);
}