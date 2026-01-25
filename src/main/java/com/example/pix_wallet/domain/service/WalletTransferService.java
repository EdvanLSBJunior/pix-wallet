package com.example.pix_wallet.domain.service;

import com.example.pix_wallet.domain.exception.InvalidTransferException;
import com.example.pix_wallet.domain.exception.WalletNotFoundException;
import com.example.pix_wallet.domain.model.Wallet;
import com.example.pix_wallet.domain.model.WalletTransaction;
import com.example.pix_wallet.domain.repository.WalletRepository;
import com.example.pix_wallet.domain.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletTransferService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletTransferService(WalletRepository walletRepository, WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {

        if (fromId.equals(toId)) {
            throw new InvalidTransferException();
        }

        Wallet from = walletRepository.findById(fromId)
                .orElseThrow(() -> new WalletNotFoundException(fromId));

        Wallet to = walletRepository.findById(toId)
                .orElseThrow(() -> new WalletNotFoundException(toId));

        from.debit(amount);
        to.credit(amount);

        transactionRepository.save(WalletTransaction.debit(from, amount, from.getBalance()));
        transactionRepository.save(WalletTransaction.credit(to, amount, to.getBalance()));
    }
}
