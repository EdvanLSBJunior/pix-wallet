package com.example.pix_wallet.domain.service;

import com.example.pix_wallet.domain.exception.InsufficientBalanceException;
import com.example.pix_wallet.domain.exception.WalletNotFoundException;
import com.example.pix_wallet.domain.model.Wallet;
import com.example.pix_wallet.domain.model.WalletTransaction;
import com.example.pix_wallet.domain.repository.WalletRepository;
import com.example.pix_wallet.domain.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletOperationService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletOperationService(
            WalletRepository walletRepository,
            WalletTransactionRepository transactionRepository
            ) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public BigDecimal credit(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        BigDecimal newBalance = wallet.credit(amount);

        WalletTransaction transaction = WalletTransaction.credit(
                wallet,
                amount,
                wallet.getBalance()
        );

        transactionRepository.save(
                WalletTransaction.credit(wallet, amount, newBalance)
        );
        return newBalance;
    }

    @Transactional
    public BigDecimal debit(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    wallet.getBalance(),
                    amount
            );
        }

        BigDecimal newBalance = wallet.debit(amount);

        WalletTransaction transaction = WalletTransaction.debit(
                wallet,
                amount,
                wallet.getBalance()
        );

        transactionRepository.save(
                WalletTransaction.debit(wallet, amount, newBalance)
        );
        return newBalance;
    }
}
