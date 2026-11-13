package com.portfoliopro.api.trading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

import com.portfoliopro.api.common.BusinessException;
import com.portfoliopro.api.market.Stock;
import com.portfoliopro.api.market.StockNotFoundException;
import com.portfoliopro.api.market.StockRepository;
import com.portfoliopro.api.user.User;
import com.portfoliopro.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeService {
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final TradeOrderRepository orderRepository;
    private final HoldingRepository holdingRepository;

    public TradeService(UserRepository userRepository, StockRepository stockRepository,
            TradeOrderRepository orderRepository, HoldingRepository holdingRepository) {
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
        this.orderRepository = orderRepository;
        this.holdingRepository = holdingRepository;
    }

    @Transactional
    public TradeOrderResponse buy(String email, TradeRequest request, String idempotencyKey) {
        return execute(email, request, idempotencyKey, TradeSide.BUY);
    }

    @Transactional
    public TradeOrderResponse sell(String email, TradeRequest request, String idempotencyKey) {
        return execute(email, request, idempotencyKey, TradeSide.SELL);
    }

    @Transactional(readOnly = true)
    public List<TradeOrderResponse> history(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        return orderRepository.findTop100ByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream().map(TradeOrderResponse::from).toList();
    }

    private TradeOrderResponse execute(String email, TradeRequest request, String key, TradeSide side) {
        String idempotencyKey = normalizeKey(key);
        User user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        String symbol = request.symbol().trim().toUpperCase(Locale.ROOT);
        var existing = orderRepository.findByUser_IdAndIdempotencyKey(user.getId(), idempotencyKey);
        if (existing.isPresent()) {
            TradeOrder priorOrder = existing.get();
            if (priorOrder.getSide() != side || priorOrder.getQuantity() != request.quantity()
                    || !priorOrder.getStock().getSymbol().equals(symbol)) {
                throw new BusinessException(HttpStatus.CONFLICT,
                        "Idempotency-Key was already used for a different order");
            }
            return TradeOrderResponse.from(priorOrder);
        }

        Stock stock = stockRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new StockNotFoundException(symbol));
        long quantity = request.quantity();
        BigDecimal price = stock.getCurrentPrice();
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal realizedPnl = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);

        if (side == TradeSide.BUY) {
            if (user.getWalletBalance().compareTo(total) < 0) {
                throw new BusinessException(HttpStatus.CONFLICT, "Insufficient virtual wallet balance");
            }
            user.debitWallet(total);
            Holding holding = holdingRepository.findByUser_IdAndStock_Id(user.getId(), stock.getId())
                    .orElse(null);
            if (holding == null) {
                holdingRepository.save(new Holding(user, stock, quantity, price));
            } else {
                holding.add(quantity, price);
            }
        } else {
            Holding holding = holdingRepository.findByUser_IdAndStock_Id(user.getId(), stock.getId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT, "No shares available to sell"));
            if (holding.getQuantity() < quantity) {
                throw new BusinessException(HttpStatus.CONFLICT, "Cannot sell more shares than you own");
            }
            realizedPnl = price.subtract(holding.getAveragePurchasePrice())
                    .multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
            user.creditWallet(total);
            if (holding.getQuantity() == quantity) {
                holdingRepository.delete(holding);
            } else {
                holding.reduce(quantity);
            }
        }

        TradeOrder order = orderRepository.save(new TradeOrder(user, stock, side, quantity, price, total,
                realizedPnl, idempotencyKey));
        return TradeOrderResponse.from(order);
    }

    private String normalizeKey(String key) {
        if (key == null || key.isBlank() || key.trim().length() > 120) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Idempotency-Key header is required and must be at most 120 characters");
        }
        return key.trim();
    }
}