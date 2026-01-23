package com.example.pix_wallet.domain.exception;

import lombok.Getter;

@Getter
public class PixKeyAlreadyExistsException extends RuntimeException {

    private final String value;

    public PixKeyAlreadyExistsException(String value) {
        super("Pix key already exists: " + value);
        this.value = value;
    }

}
