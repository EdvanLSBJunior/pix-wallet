package com.example.pix_wallet.domain.exception;

public class InvalidTransferException extends RuntimeException {
    public InvalidTransferException() {
        super("Source and destination wallets must be different");
    }
}
