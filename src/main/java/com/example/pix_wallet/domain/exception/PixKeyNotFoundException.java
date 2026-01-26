package com.example.pix_wallet.domain.exception;

import com.example.pix_wallet.domain.model.PixKeyType;

public class PixKeyNotFoundException extends RuntimeException {

    public PixKeyNotFoundException(PixKeyType type, String value) {
        super("Pix key not found. Type: " + type + ", Value: " + value);
    }
}
