package com.example.pix_wallet.domain.service;

import com.example.pix_wallet.domain.model.Wallet;
import com.example.pix_wallet.domain.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateWalletService {

    private final WalletRepository walletRepository;

    public CreateWalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public Wallet create() {
        Wallet wallet = Wallet.create();
        return walletRepository.save(wallet);
    }
}
