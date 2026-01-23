package com.example.pix_wallet.domain.service;

import com.example.pix_wallet.domain.exception.PixKeyAlreadyExistsException;
import com.example.pix_wallet.domain.exception.WalletNotFoundException;
import com.example.pix_wallet.domain.model.PixKey;
import com.example.pix_wallet.domain.model.PixKeyType;
import com.example.pix_wallet.domain.model.Wallet;
import com.example.pix_wallet.domain.repository.PixKeyRepository;
import com.example.pix_wallet.domain.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PixKeyRegistrationService {

    private final WalletRepository walletRepository;
    private final PixKeyRepository pixKeyRepository;

    public PixKeyRegistrationService(
            WalletRepository walletRepository,
            PixKeyRepository pixKeyRepository
    ) {
        this.walletRepository = walletRepository;
        this.pixKeyRepository = pixKeyRepository;
    }

    @Transactional
    public PixKey register(PixKeyType type, String value, Long walletId) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        if (type != PixKeyType.EVP && pixKeyRepository.existsByValue(value)) {
            throw new PixKeyAlreadyExistsException(value);
        }

        PixKey pixKey = switch (type) {
            case EVP -> PixKey.createEVP(wallet);
            default -> PixKey.create(type, value, wallet);
        };

        return pixKeyRepository.save(pixKey);
    }
}
