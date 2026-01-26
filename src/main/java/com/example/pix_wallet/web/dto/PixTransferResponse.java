package com.example.pix_wallet.web.dto;

import com.example.pix_wallet.domain.model.PixTransfer;

import java.math.BigDecimal;
import java.time.Instant;

public record PixTransferResponse(
        String endToEndId,
        BigDecimal amount,
        Long toWalletId,
        Instant createdAt
) {
    public static PixTransferResponse from(PixTransfer transfer) {
        return new PixTransferResponse(
                transfer.getEndToEndId(),
                transfer.getAmount(),
                transfer.getToWallet().getId(),
                transfer.getCreatedAt()
        );
    }
}

