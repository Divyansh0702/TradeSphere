package com.portfoliopro.api.watchlist;

import java.util.List;

import com.portfoliopro.api.common.BusinessException;
import com.portfoliopro.api.market.Stock;
import com.portfoliopro.api.market.StockNotFoundException;
import com.portfoliopro.api.market.StockRepository;
import com.portfoliopro.api.market.StockResponse;
import com.portfoliopro.api.user.User;
import com.portfoliopro.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WatchlistService {
    private final UserRepository userRepository;
    private final WatchlistRepository watchlistRepository;
    private final WatchlistItemRepository itemRepository;
    private final StockRepository stockRepository;

    public WatchlistService(UserRepository userRepository, WatchlistRepository watchlistRepository,
            WatchlistItemRepository itemRepository, StockRepository stockRepository) {
        this.userRepository = userRepository;
        this.watchlistRepository = watchlistRepository;
        this.itemRepository = itemRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public WatchlistResponse create(String email) {
        User user = requireLockedUser(email);
        if (watchlistRepository.findByUser_Id(user.getId()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Watchlist already exists");
        }
        return response(watchlistRepository.save(new Watchlist(user)));
    }

    @Transactional(readOnly = true)
    public WatchlistResponse get(String email) {
        Watchlist watchlist = requireWatchlist(email);
        return response(watchlist);
    }

    @Transactional
    public WatchlistResponse addStock(String email, String symbol) {
        User user = requireLockedUser(email);
        Watchlist watchlist = watchlistRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Create a watchlist first"));
        Stock stock = stockRepository.findBySymbolIgnoreCase(symbol.trim())
                .orElseThrow(() -> new StockNotFoundException(symbol));
        if (itemRepository.findByWatchlist_IdAndStock_Id(watchlist.getId(), stock.getId()).isEmpty()) {
            itemRepository.save(new WatchlistItem(watchlist, stock));
        }
        return response(watchlist);
    }

    @Transactional
    public void removeStock(String email, String symbol) {
        User user = requireLockedUser(email);
        Watchlist watchlist = watchlistRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Watchlist not found"));
        Stock stock = stockRepository.findBySymbolIgnoreCase(symbol.trim())
                .orElseThrow(() -> new StockNotFoundException(symbol));
        if (itemRepository.findByWatchlist_IdAndStock_Id(watchlist.getId(), stock.getId()).isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Stock is not in this watchlist");
        }
        itemRepository.deleteByWatchlist_IdAndStock_Id(watchlist.getId(), stock.getId());
    }

    private User requireLockedUser(String email) {
        return userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
    }

    private Watchlist requireWatchlist(String email) {
        return userRepository.findByEmail(email)
                .flatMap(user -> watchlistRepository.findByUser_Id(user.getId()))
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Watchlist not found"));
    }

    private WatchlistResponse response(Watchlist watchlist) {
        List<StockResponse> stocks = itemRepository.findAllByWatchlist_IdOrderByStock_SymbolAsc(watchlist.getId())
                .stream().map(item -> StockResponse.from(item.getStock())).toList();
        return new WatchlistResponse(watchlist.getId(), watchlist.getCreatedAt(), stocks);
    }
}