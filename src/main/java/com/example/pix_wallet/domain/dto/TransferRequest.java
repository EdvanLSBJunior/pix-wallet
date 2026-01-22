package com.example.pix_wallet.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull
        Long fromWalletId,

        @NotNull
        Long toWalletId,

        @NotNull
        @Positive
        BigDecimal amount
) {}
