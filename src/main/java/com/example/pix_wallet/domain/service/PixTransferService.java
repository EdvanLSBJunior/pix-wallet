package com.example.pix_wallet.domain.service;

import com.example.pix_wallet.domain.exception.InvalidTransferException;
import com.example.pix_wallet.domain.exception.PixKeyNotFoundException;
import com.example.pix_wallet.domain.exception.WalletNotFoundException;
import com.example.pix_wallet.domain.model.PixKey;
import com.example.pix_wallet.domain.model.PixKeyType;
import com.example.pix_wallet.domain.model.PixTransfer;
import com.example.pix_wallet.domain.model.Wallet;
import com.example.pix_wallet.domain.repository.PixKeyRepository;
import com.example.pix_wallet.domain.repository.PixTransferRepository;
import com.example.pix_wallet.domain.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PixTransferService {

    private final WalletRepository walletRepository;
    private final PixKeyRepository pixKeyRepository;
    private final PixTransferRepository pixTransferRepository;
    private final WalletOperationService walletOperationService;

    public PixTransferService(
            WalletRepository walletRepository,
            PixKeyRepository pixKeyRepository,
            PixTransferRepository pixTransferRepository,
            WalletOperationService walletOperationService
    ) {
        this.walletRepository = walletRepository;
        this.pixKeyRepository = pixKeyRepository;
        this.pixTransferRepository = pixTransferRepository;
        this.walletOperationService = walletOperationService;
    }

    @Transactional
    public PixTransfer transfer(
            Long fromWalletId,
            PixKeyType type,
            String value,
            BigDecimal amount
    ) {
        Wallet fromWallet = walletRepository.findById(fromWalletId)
                .orElseThrow(() -> new WalletNotFoundException(fromWalletId));

        PixKey pixKey = pixKeyRepository
                .findByTypeAndValue(type, value)
                .orElseThrow(() -> new PixKeyNotFoundException(type, value));

        Wallet toWallet = pixKey.getWallet();

        if (fromWallet.getId() != null
                && fromWallet.getId().equals(toWallet.getId())) {
            throw new IllegalStateException("Cannot transfer Pix to the same wallet");
        }

        if (fromWallet == toWallet) {
            throw new InvalidTransferException();
        }

        // Validar se há saldo suficiente (sem debitar ainda)
        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance. Balance: " +
                fromWallet.getBalance() + ", amount: " + amount);
        }

        PixTransfer pixTransfer = PixTransfer.create(
                fromWallet,
                toWallet,
                pixKey,
                amount
        );

        return pixTransferRepository.save(pixTransfer);
    }
}
