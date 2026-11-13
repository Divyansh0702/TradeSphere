package com.portfoliopro.api.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.portfoliopro.api.trading.HoldingRepository;
import com.portfoliopro.api.trading.HoldingResponse;
import com.portfoliopro.api.trading.TradeOrderRepository;
import com.portfoliopro.api.user.User;
import com.portfoliopro.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PortfolioService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);

    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;
    private final TradeOrderRepository orderRepository;

    public PortfolioService(UserRepository userRepository, HoldingRepository holdingRepository,
            TradeOrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.holdingRepository = holdingRepository;
        this.orderRepository = orderRepository;
    }

    public List<HoldingResponse> holdings(String email) {
        User user = requireUser(email);
        return holdingRepository.findAllByUser_IdOrderByStock_SymbolAsc(user.getId())
                .stream().map(HoldingResponse::from).toList();
    }

    public PortfolioSummaryResponse summary(String email) {
        User user = requireUser(email);
        var holdings = holdingRepository.findAllByUser_IdOrderByStock_SymbolAsc(user.getId());
        BigDecimal invested = ZERO;
        BigDecimal current = ZERO;
        for (var holding : holdings) {
            BigDecimal quantity = BigDecimal.valueOf(holding.getQuantity());
            invested = invested.add(holding.getAveragePurchasePrice().multiply(quantity));
            current = current.add(holding.getStock().getCurrentPrice().multiply(quantity));
        }
        invested = invested.setScale(2, RoundingMode.HALF_UP);
        current = current.setScale(2, RoundingMode.HALF_UP);
        BigDecimal realized = orderRepository.sumRealizedPnlByUserId(user.getId()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal unrealized = current.subtract(invested).setScale(2, RoundingMode.HALF_UP);
        return new PortfolioSummaryResponse(user.getWalletBalance(), invested, current, realized, unrealized,
                user.getWalletBalance().add(current).setScale(2, RoundingMode.HALF_UP), "INR", true);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
    }
}