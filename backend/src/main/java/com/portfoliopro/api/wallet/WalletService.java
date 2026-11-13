package com.portfoliopro.api.wallet;

import com.portfoliopro.api.user.User;
import com.portfoliopro.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {
    private final UserRepository userRepository;

    public WalletService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse get(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        return response(user);
    }

    @Transactional
    public WalletResponse topUp(String email, TopUpRequest request) {
        User user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        user.creditWallet(request.amount());
        return response(user);
    }

    private WalletResponse response(User user) {
        return new WalletResponse(user.getWalletBalance(), "INR", true);
    }
}