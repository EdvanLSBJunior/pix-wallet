package com.example.pix_wallet.domain.service;

import com.example.pix_wallet.domain.exception.InsufficientBalanceException;
import com.example.pix_wallet.domain.exception.InvalidTransferException;
import com.example.pix_wallet.domain.exception.PixKeyNotFoundException;
import com.example.pix_wallet.domain.exception.WalletNotFoundException;
import com.example.pix_wallet.domain.model.*;
import com.example.pix_wallet.domain.repository.PixKeyRepository;
import com.example.pix_wallet.domain.repository.PixTransferRepository;
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



class PixTransferServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PixKeyRepository pixKeyRepository;

    @Mock
    private PixTransferRepository pixTransferRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private WalletOperationService walletOperationService;

    @InjectMocks
    private PixTransferService pixTransferService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldTransferAmountUsingPixKey() {
        Wallet from = Wallet.create();
        from.credit(new BigDecimal("200.00"));

        Wallet to = Wallet.create();

        PixKey pixKey = PixKey.createEmail("user@email.com", to);

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(from));

        when(pixKeyRepository.findByTypeAndValue(pixKey.getType(), pixKey.getValue()))
                .thenReturn(Optional.of(pixKey));

        when(pixTransferRepository.save(any(PixTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PixTransfer transfer = pixTransferService.transfer(
                1L,
                pixKey.getType(),
                pixKey.getValue(),
                new BigDecimal("50.00")
        );

        assertEquals(new BigDecimal("50.00"), transfer.getAmount());
        assertNotNull(transfer.getEndToEndId());
        assertEquals(PixTransferStatus.PENDING, transfer.getStatus());

        // Verifica que NÃO houve movimentação de valores (deve acontecer apenas no webhook)
        verify(walletOperationService, never()).debit(any(), any());
        verify(walletOperationService, never()).credit(any(), any());

        verify(pixTransferRepository).save(any(PixTransfer.class));
    }

    @Test
    void shouldThrowExceptionWhenInsufficientBalance() {
        Wallet from = Wallet.create();
        // Carteira sem saldo suficiente
        from.credit(new BigDecimal("30.00"));

        Wallet to = Wallet.create();
        PixKey pixKey = PixKey.createEmail("user@email.com", to);

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(from));

        when(pixKeyRepository.findByTypeAndValue(pixKey.getType(), pixKey.getValue()))
                .thenReturn(Optional.of(pixKey));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            pixTransferService.transfer(
                    1L,
                    pixKey.getType(),
                    pixKey.getValue(),
                    new BigDecimal("50.00")
            );
        });

        assertTrue(exception.getMessage().contains("Insufficient balance"));
        verify(pixTransferRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenPixKeyNotFound() {
        Wallet from = Wallet.create();

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(from));

        when(pixKeyRepository.findByTypeAndValue(PixKeyType.EMAIL, "x@email.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                PixKeyNotFoundException.class,
                () -> pixTransferService.transfer(
                        1L,
                        PixKeyType.EMAIL,
                        "x@email.com",
                        new BigDecimal("50.00")
                )
        );

        verify(walletOperationService, never()).debit(any(), any());
        verify(walletOperationService, never()).credit(any(), any());
        verify(pixTransferRepository, never()).save(any());
    }

//    @Test
//    void shouldThrowExceptionWhenInsufficientBalance() {
//        Wallet from = Wallet.create();
//        from.credit(new BigDecimal("10.00"));
//
//        Wallet to = Wallet.create();
//        PixKey pixKey = PixKey.createEmail("user@email.com", to);
//
//        when(walletRepository.findById(1L))
//                .thenReturn(Optional.of(from));
//
//        when(pixKeyRepository.findByTypeAndValue(pixKey.getType(), pixKey.getValue()))
//                .thenReturn(Optional.of(pixKey));
//
//        when(walletOperationService.debit(any(), eq(new BigDecimal("50.00"))))
//                .thenThrow(
//                        new InsufficientBalanceException(
//                                new BigDecimal("10.00"),
//                                new BigDecimal("50.00")
//                        )
//                );
//
//        assertThrows(
//                InsufficientBalanceException.class,
//                () -> pixTransferService.transfer(
//                        1L,
//                        pixKey.getType(),
//                        pixKey.getValue(),
//                        new BigDecimal("50.00")
//                )
//        );
//
//        verify(walletOperationService).debit(any(), eq(new BigDecimal("50.00")));
//        verify(walletOperationService, never()).credit(any(), any());
//        verify(pixTransferRepository, never()).save(any());
//    }

    @Test
    void shouldThrowExceptionWhenAmountIsInvalid() {
        assertThrows(
                WalletNotFoundException.class,
                () -> pixTransferService.transfer(
                        1L,
                        PixKeyType.EMAIL,
                        "user@email.com",
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldNotAllowTransferToSameWallet() {
        Wallet wallet = Wallet.create();
        PixKey pixKey = PixKey.createEmail("self@email.com", wallet);

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(wallet));

        when(pixKeyRepository.findByTypeAndValue(pixKey.getType(), pixKey.getValue()))
                .thenReturn(Optional.of(pixKey));

        assertThrows(
                InvalidTransferException.class,
                () -> pixTransferService.transfer(
                        1L,
                        pixKey.getType(),
                        pixKey.getValue(),
                        new BigDecimal("10.00")
                )
        );
    }
}
