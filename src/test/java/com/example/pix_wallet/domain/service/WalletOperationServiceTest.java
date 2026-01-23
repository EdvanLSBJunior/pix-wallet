package com.example.pix_wallet.domain.service;

import com.example.pix_wallet.domain.exception.InsufficientBalanceException;
import com.example.pix_wallet.domain.exception.WalletNotFoundException;
import com.example.pix_wallet.domain.model.Wallet;
import com.example.pix_wallet.domain.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletOperationServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletOperationService walletOperationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreditWallet() {
        Wallet wallet = Wallet.create();

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        Wallet result = walletOperationService.credit(1L, new BigDecimal("50.00"));

        assertEquals(new BigDecimal("50.00"), result.getBalance());
        verify(walletRepository).findById(1L);
    }

    @Test
    void shouldDebitWallet() {
        Wallet wallet = Wallet.create();
        wallet.credit(new BigDecimal("100.00"));

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        Wallet result = walletOperationService.debit(1L, new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), result.getBalance());
        verify(walletRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenCreditWalletNotFound() {
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> walletOperationService.credit(1L, new BigDecimal("10.00"))
        );
    }

    @Test
    void shouldThrowExceptionWhenDebitWalletNotFound() {
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> walletOperationService.debit(1L, new BigDecimal("10.00"))
        );
    }

    @Test
    void shouldThrowExceptionWhenDebitWithInsufficientBalance() {
        Wallet wallet = Wallet.create();
        wallet.credit(new BigDecimal("20.00"));

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        assertThrows(
                InsufficientBalanceException.class,
                () -> walletOperationService.debit(1L, new BigDecimal("100.00"))
        );

        assertEquals(new BigDecimal("20.00"), wallet.getBalance());
    }
}
