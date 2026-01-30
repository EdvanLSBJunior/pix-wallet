package com.example.pix_wallet.web.dto;

import com.example.pix_wallet.domain.model.PixTransferStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record PixWebhookEventRequest(
        @NotBlank
        String endToEndId,

        @NotNull
        PixTransferStatus status,

        @NotNull
        Instant timestamp
) {
}
