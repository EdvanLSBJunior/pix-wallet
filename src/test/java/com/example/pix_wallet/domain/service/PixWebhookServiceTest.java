package com.example.pix_wallet.domain.service;

import com.example.pix_wallet.domain.exception.TransferNotFoundException;
import com.example.pix_wallet.domain.exception.WebhookEventIgnoredException;
import com.example.pix_wallet.domain.model.*;
import com.example.pix_wallet.domain.repository.PixTransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PixWebhookServiceTest {

    @Mock
    private PixTransferRepository pixTransferRepository;

    @Mock
    private WalletOperationService walletOperationService;

    @InjectMocks
    private PixWebhookService pixWebhookService;

    private PixTransfer pixTransfer;
    private Wallet fromWallet;
    private Wallet toWallet;

    @BeforeEach
    void setUp() {
        fromWallet = Wallet.create();
        toWallet = Wallet.create();
        PixKey pixKey = PixKey.createEmail("test@test.com", toWallet);

        pixTransfer = PixTransfer.create(fromWallet, toWallet, pixKey, new BigDecimal("100.00"));
    }

    @Test
    void shouldProcessConfirmedWebhookEvent() {
        String endToEndId = "E2E-123456";
        Instant timestamp = Instant.now();

        // Simula carteira com saldo suficiente
        fromWallet.credit(new BigDecimal("200.00"));

        when(pixTransferRepository.findByEndToEndId(endToEndId))
                .thenReturn(Optional.of(pixTransfer));

        pixWebhookService.processWebhookEvent(endToEndId, PixTransferStatus.CONFIRMED, timestamp);

        verify(pixTransferRepository).save(pixTransfer);
        // Agora deve executar a transferência
        verify(walletOperationService).debit(fromWallet.getId(), new BigDecimal("100.00"));
        verify(walletOperationService).credit(toWallet.getId(), new BigDecimal("100.00"));
    }

    @Test
    void shouldProcessRejectedWebhookEventWithoutReversal() {
        String endToEndId = "E2E-123456";
        Instant timestamp = Instant.now();

        when(pixTransferRepository.findByEndToEndId(endToEndId))
                .thenReturn(Optional.of(pixTransfer));

        pixWebhookService.processWebhookEvent(endToEndId, PixTransferStatus.REJECTED, timestamp);

        verify(pixTransferRepository).save(pixTransfer);
        // Não deve haver movimentação pois a transferência nunca foi executada
        verify(walletOperationService, never()).credit(any(), any());
        verify(walletOperationService, never()).debit(any(), any());
    }

    @Test
    void shouldThrowExceptionForDuplicateWebhookEvent() {
        String endToEndId = "E2E-123456";
        Instant oldTimestamp = Instant.now().minusSeconds(10);
        Instant newTimestamp = Instant.now();

        // Simula que já foi processado um evento mais recente
        pixTransfer.updateStatus(PixTransferStatus.CONFIRMED, newTimestamp);

        when(pixTransferRepository.findByEndToEndId(endToEndId))
                .thenReturn(Optional.of(pixTransfer));

        // Tenta processar evento mais antigo - deve lançar exceção
        assertThrows(WebhookEventIgnoredException.class, () -> {
            pixWebhookService.processWebhookEvent(endToEndId, PixTransferStatus.REJECTED, oldTimestamp);
        });

        verify(pixTransferRepository, never()).save(pixTransfer);
        verify(walletOperationService, never()).credit(any(), any());
        verify(walletOperationService, never()).debit(any(), any());
    }

    @Test
    void shouldThrowExceptionForNonExistentTransfer() {
        String endToEndId = "E2E-NOTFOUND";
        Instant timestamp = Instant.now();

        when(pixTransferRepository.findByEndToEndId(endToEndId))
                .thenReturn(Optional.empty());

        assertThrows(TransferNotFoundException.class, () -> {
            pixWebhookService.processWebhookEvent(endToEndId, PixTransferStatus.CONFIRMED, timestamp);
        });

        verify(pixTransferRepository, never()).save(any());
        verify(walletOperationService, never()).credit(any(), any());
        verify(walletOperationService, never()).debit(any(), any());
    }
}
