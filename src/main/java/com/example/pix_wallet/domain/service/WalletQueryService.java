package com.example.pix_wallet.domain.service;

import com.example.pix_wallet.domain.exception.WalletNotFoundException;
import com.example.pix_wallet.domain.model.Wallet;
import com.example.pix_wallet.domain.repository.WalletRepository;
import com.example.pix_wallet.domain.repository.WalletTransactionRepository;
import com.example.pix_wallet.domain.model.WalletTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class WalletQueryService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletQueryService(WalletRepository walletRepository, WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public Wallet getById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId))
                .getBalance();
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        return wallet.getBalance();
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalanceAt(Long walletId, Instant at) {
        return transactionRepository
                .findFirstByWalletIdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(walletId, at)
                .map(WalletTransaction::getBalanceAfter)
                .orElse(BigDecimal.ZERO);
    }
}
