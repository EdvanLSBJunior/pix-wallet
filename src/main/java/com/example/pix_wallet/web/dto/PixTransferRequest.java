package com.example.pix_wallet.web.dto;

import com.example.pix_wallet.domain.model.PixKeyType;

import java.math.BigDecimal;

public record PixTransferRequest(
        Long fromWalletId,
        PixKeyType pixKeyType,
        String pixKeyValue,
        BigDecimal amount
) {}

