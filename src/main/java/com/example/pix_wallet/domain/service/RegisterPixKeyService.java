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
public class RegisterPixKeyService {

    private final WalletRepository walletRepository;
    private final PixKeyRepository pixKeyRepository;

    public RegisterPixKeyService(
            WalletRepository walletRepository,
            PixKeyRepository pixKeyRepository
    ) {
        this.walletRepository = walletRepository;
        this.pixKeyRepository = pixKeyRepository;
    }

    @Transactional
    public PixKey registerEVP(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        PixKey pixKey = PixKey.createEVP(wallet);
        return pixKeyRepository.save(pixKey);
    }

    @Transactional
    public PixKey registerEmail(Long walletId, String email) {
        validateUnique(PixKeyType.EMAIL, email);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        PixKey pixKey = PixKey.createEmail(email, wallet);
        return pixKeyRepository.save(pixKey);
    }

    @Transactional
    public PixKey registerPhone(Long walletId, String phone) {
        validateUnique(PixKeyType.PHONE, phone);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        PixKey pixKey = PixKey.createPhone(phone, wallet);
        return pixKeyRepository.save(pixKey);
    }

    private void validateUnique(PixKeyType type, String value) {
        if (pixKeyRepository.existsByTypeAndValue(type, value)) {
            throw new PixKeyAlreadyExistsException(value);
        }
    }
}
