package com.example.pix_wallet.domain.service;

import com.example.pix_wallet.domain.exception.InsufficientBalanceException;
import com.example.pix_wallet.domain.exception.InvalidTransferException;
import com.example.pix_wallet.domain.exception.WalletNotFoundException;
import com.example.pix_wallet.domain.model.Wallet;
import com.example.pix_wallet.domain.model.WalletTransaction;
import com.example.pix_wallet.domain.repository.WalletRepository;
import com.example.pix_wallet.domain.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletTransferServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private WalletTransferService walletTransferService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    void shouldTransferAmountBetweenWallets() {
//        Wallet from = Wallet.create();
//        from.credit(new BigDecimal("100.00"));
//
//        Wallet to = Wallet.create();
//
//        when(walletRepository.findById(1L)).thenReturn(Optional.of(from));
//        when(walletRepository.findById(2L)).thenReturn(Optional.of(to));
//
//        walletTransferService.transfer(1L, 2L, new BigDecimal("40.00"));
//
//        assertEquals(new BigDecimal("60.00"), from.getBalance());
//        assertEquals(new BigDecimal("40.00"), to.getBalance());
//
//        verify(walletRepository).findById(1L);
//        verify(walletRepository).findById(2L);
//    }

    @Test
    void shouldTransferAmountBetweenWallets() {
        Wallet from = Wallet.create();
        from.credit(new BigDecimal("200.00"));

        Wallet to = Wallet.create();

        when(walletRepository.findById(1L)).thenReturn(Optional.of(from));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(to));

        walletTransferService.transfer(1L, 2L, new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), from.getBalance());
        assertEquals(new BigDecimal("50.00"), to.getBalance());

        verify(walletTransactionRepository, times(2))
                .save(any(WalletTransaction.class));
    }

    @Test
    void shouldThrowExceptionWhenTransferToSameWallet() {
        assertThrows(
                InvalidTransferException.class,
                () -> walletTransferService.transfer(1L, 1L, new BigDecimal("10.00"))
        );

        verifyNoInteractions(walletRepository);
    }

    @Test
    void shouldThrowExceptionWhenSourceWalletNotFound() {
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> walletTransferService.transfer(1L, 2L, new BigDecimal("10.00"))
        );
    }

    @Test
    void shouldThrowExceptionWhenTargetWalletNotFound() {
        Wallet from = Wallet.create();
        from.credit(new BigDecimal("50.00"));

        when(walletRepository.findById(1L)).thenReturn(Optional.of(from));
        when(walletRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> walletTransferService.transfer(1L, 2L, new BigDecimal("10.00"))
        );
    }

    @Test
    void shouldThrowExceptionWhenInsufficientBalance() {
        Wallet from = Wallet.create();
        from.credit(new BigDecimal("20.00"));

        Wallet to = Wallet.create();

        when(walletRepository.findById(1L)).thenReturn(Optional.of(from));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(to));

        assertThrows(
                InsufficientBalanceException.class,
                () -> walletTransferService.transfer(1L, 2L, new BigDecimal("100.00"))
        );

        assertEquals(new BigDecimal("20.00"), from.getBalance());
        assertEquals(BigDecimal.ZERO, to.getBalance());
    }
}
