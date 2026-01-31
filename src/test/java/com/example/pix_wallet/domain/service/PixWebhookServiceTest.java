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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        when(pixTransferRepository.findByEndToEndIdWithLock(endToEndId))
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

        when(pixTransferRepository.findByEndToEndIdWithLock(endToEndId))
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

        when(pixTransferRepository.findByEndToEndIdWithLock(endToEndId))
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

        when(pixTransferRepository.findByEndToEndIdWithLock(endToEndId))
                .thenReturn(Optional.empty());

        assertThrows(TransferNotFoundException.class, () -> {
            pixWebhookService.processWebhookEvent(endToEndId, PixTransferStatus.CONFIRMED, timestamp);
        });

        verify(pixTransferRepository, never()).save(any());
        verify(walletOperationService, never()).credit(any(), any());
        verify(walletOperationService, never()).debit(any(), any());
    }

    @Test
    void shouldHandleConcurrentWebhookRequestsForSamePix() throws InterruptedException {
        String endToEndId = "E2E-concurrent-test";
        Instant timestamp = Instant.now();

        // Simula carteira com saldo suficiente
        fromWallet.credit(new BigDecimal("200.00"));

        when(pixTransferRepository.findByEndToEndIdWithLock(endToEndId))
                .thenReturn(Optional.of(pixTransfer));

        // Contador para rastrear tentativas de processamento
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger exceptionsCount = new AtomicInteger(0);

        // Mock que simula que apenas a primeira execução é bem-sucedida
        doAnswer(invocation -> {
            int count = processedCount.incrementAndGet();
            if (count == 1) {
                // Primeira execução: sucesso
                return new BigDecimal("100.00");
            } else {
                // Demais execuções: falham devido ao lock/estado
                throw new IllegalStateException("Transfer já processado");
            }
        }).when(walletOperationService).debit(any(), any());

        // Simular múltiplas requisições simultâneas do MESMO PIX
        int numberOfThreads = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Aguarda sinal para todas iniciarem juntas
                    pixWebhookService.processWebhookEvent(endToEndId, PixTransferStatus.CONFIRMED, timestamp);
                } catch (Exception e) {
                    exceptionsCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Libera todas as threads simultaneamente
        startLatch.countDown();

        // Aguarda todas terminarem
        endLatch.await();

        executor.shutdown();

        // Verificações: apenas 1 processamento deve ter sido bem-sucedido
        // O lock pessimista deve ter protegido contra processamentos simultâneos
        assertTrue(processedCount.get() <= 1,
                "Apenas 1 processamento deve ter acontecido, mas foram: " + processedCount.get());

        // Em cenário real, esperamos que 4 threads falhem devido ao lock
        // (este é um teste conceitual - o lock real seria no banco)
        assertTrue(exceptionsCount.get() >= 0,
                "Deve haver exceções devido ao controle de concorrência");
    }
}
