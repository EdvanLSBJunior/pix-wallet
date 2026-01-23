package com.example.pix_wallet.domain.service;

import com.example.pix_wallet.domain.exception.PixKeyAlreadyExistsException;
import com.example.pix_wallet.domain.exception.WalletNotFoundException;
import com.example.pix_wallet.domain.factory.WalletTestFactory;
import com.example.pix_wallet.domain.model.PixKey;
import com.example.pix_wallet.domain.model.PixKeyType;
import com.example.pix_wallet.domain.model.Wallet;
import com.example.pix_wallet.domain.repository.PixKeyRepository;
import com.example.pix_wallet.domain.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterPixKeyServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PixKeyRepository pixKeyRepository;

    @InjectMocks
    private RegisterPixKeyService service;

    @Test
    void shouldRegisterEVPKey() {
        Wallet wallet = WalletTestFactory.validWallet();

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(wallet));

        when(pixKeyRepository.save(any(PixKey.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PixKey pixKey = service.registerEVP(1L);

        assertEquals(PixKeyType.EVP, pixKey.getType());
        assertNotNull(pixKey.getValue());
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        when(walletRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> service.registerEVP(99L)
        );
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(pixKeyRepository.existsByTypeAndValue(
                PixKeyType.EMAIL,
                "user@email.com"
        )).thenReturn(true);

        assertThrows(
                PixKeyAlreadyExistsException.class,
                () -> service.registerEmail(1L, "user@email.com")
        );
    }
}
