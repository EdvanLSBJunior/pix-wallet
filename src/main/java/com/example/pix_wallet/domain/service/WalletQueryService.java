package com.example.pix_wallet.domain.service;

import com.example.pix_wallet.domain.exception.WalletNotFoundException;
import com.example.pix_wallet.domain.model.Wallet;
import com.example.pix_wallet.domain.repository.WalletRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class WalletQueryService {

    private final WalletRepository walletRepository;

    public WalletQueryService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional(readOnly = true)
    public Wallet getById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
    }
}
