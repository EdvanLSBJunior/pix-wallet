package com.example.pix_wallet.domain.service;

import com.example.pix_wallet.domain.exception.TransferNotFoundException;
import com.example.pix_wallet.domain.exception.WebhookEventIgnoredException;
import com.example.pix_wallet.domain.model.PixTransfer;
import com.example.pix_wallet.domain.model.PixTransferStatus;
import com.example.pix_wallet.domain.repository.PixTransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class PixWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(PixWebhookService.class);

    private final PixTransferRepository pixTransferRepository;
    private final WalletOperationService walletOperationService;

    public PixWebhookService(
            PixTransferRepository pixTransferRepository,
            WalletOperationService walletOperationService
    ) {
        this.pixTransferRepository = pixTransferRepository;
        this.walletOperationService = walletOperationService;
    }

    @Transactional
    public void processWebhookEvent(String endToEndId, PixTransferStatus status, Instant timestamp) {
        logger.info("Processing webhook event for endToEndId: {}, status: {}, timestamp: {}",
                   endToEndId, status, timestamp);

        Optional<PixTransfer> transferOpt = pixTransferRepository.findByEndToEndIdWithLock(endToEndId);

        if (transferOpt.isEmpty()) {
            logger.warn("Transfer not found for endToEndId: {}", endToEndId);
            logger.error("🚨 THROWING TransferNotFoundException for endToEndId: {}", endToEndId);
            throw new TransferNotFoundException(endToEndId);
        }

        PixTransfer transfer = transferOpt.get();
        boolean statusUpdated = transfer.updateStatus(status, timestamp);

        if (!statusUpdated) {
            String reason = "Duplicate or outdated event for transfer " + transfer.getId();
            logger.info(reason);
            logger.error("🚨 THROWING WebhookEventIgnoredException: {}", reason);
            throw new WebhookEventIgnoredException(reason);
        }

        // Processa as ações baseadas no novo status
        if (status == PixTransferStatus.CONFIRMED && transfer.isConfirmed()) {
            processConfirmedTransfer(transfer);
        } else if (status == PixTransferStatus.REJECTED && transfer.isRejected()) {
            processRejectedTransfer(transfer);
        }

        pixTransferRepository.save(transfer);

        logger.info("Successfully processed webhook event for transfer {}, new status: {}",
                   transfer.getId(), status);
    }

    private void processConfirmedTransfer(PixTransfer transfer) {
        logger.info("Processing confirmed transfer {} - executing payment", transfer.getId());

        try {
            // Verifica saldo antes de debitar
            if (transfer.getFromWallet().getBalance().compareTo(transfer.getAmount()) < 0) {
                throw new IllegalStateException("Insufficient balance for confirmed transfer");
            }

            // Executa a transferência
            walletOperationService.debit(transfer.getFromWallet().getId(), transfer.getAmount());
            walletOperationService.credit(transfer.getToWallet().getId(), transfer.getAmount());

            logger.info("Successfully executed confirmed transfer {}", transfer.getId());
        } catch (Exception e) {
            logger.error("Error executing confirmed transfer {}: {}", transfer.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to execute confirmed transfer", e);
        }
    }

    private void processRejectedTransfer(PixTransfer transfer) {
        logger.info("Processing rejected transfer {} - no action needed (transfer was never executed)", transfer.getId());

        // Como a transferência nunca foi executada (status era PENDING),
        // não há necessidade de reverter nada
        logger.info("Transfer {} rejected - no reversal needed as it was never executed", transfer.getId());
    }
}
