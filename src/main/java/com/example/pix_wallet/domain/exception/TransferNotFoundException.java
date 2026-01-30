package com.example.pix_wallet.domain.exception;

public class TransferNotFoundException extends RuntimeException {
    public TransferNotFoundException(String endToEndId) {
        super("Transfer not found for endToEndId: " + endToEndId);
    }
}
