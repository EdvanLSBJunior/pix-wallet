package com.example.pix_wallet.domain.exception;

public class WebhookEventIgnoredException extends RuntimeException {
    public WebhookEventIgnoredException(String reason) {
        super("Webhook event ignored: " + reason);
    }
}
