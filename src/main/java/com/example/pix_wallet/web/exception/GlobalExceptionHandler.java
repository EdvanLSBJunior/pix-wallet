package com.example.pix_wallet.web.exception;

import com.example.pix_wallet.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleWalletNotFound(WalletNotFoundException ex) {
        return Map.of(
                "timestamp", Instant.now(),
                "status", 404,
                "error", ex.getMessage()
        );
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, Object> handleInsufficientBalance(InsufficientBalanceException ex) {
        return Map.of(
                "timestamp", Instant.now(),
                "status", 422,
                "error", ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidTransferException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, Object> handleInvalidTransfer(InvalidTransferException ex) {

        return Map.of(
                "timestamp", Instant.now(),
                "status", HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "error", ex.getMessage()
        );
    }

    @ExceptionHandler(PixKeyAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, Object> handlePixKeyAlreadyExists(PixKeyAlreadyExistsException ex) {
        return Map.of(
                "error", "PIX_KEY_ALREADY_EXISTS",
                "message", ex.getMessage()
        );
    }

    @ExceptionHandler(PixKeyNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePixKeyNotFound(
            PixKeyNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "timestamp", Instant.now(),
                        "status", 404,
                        "error", "PIX_KEY_NOT_FOUND",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(TransferNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTransferNotFound(
            TransferNotFoundException ex,
            HttpServletRequest request
    ) {
        System.out.println("🔴 GlobalExceptionHandler.handleTransferNotFound() CALLED!");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "timestamp", Instant.now(),
                        "status", 404,
                        "error", "Transfer Not Found",
                        "message", ex.getMessage(),
                        "path", request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(WebhookEventIgnoredException.class)
    public ResponseEntity<Map<String, Object>> handleWebhookEventIgnored(
            WebhookEventIgnoredException ex,
            HttpServletRequest request
    ) {
        System.out.println("🟡 GlobalExceptionHandler.handleWebhookEventIgnored() CALLED!");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "timestamp", Instant.now(),
                        "status", 409,
                        "error", "Webhook Event Ignored",
                        "message", ex.getMessage(),
                        "path", request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("Validation failed");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "timestamp", Instant.now(),
                        "status", 400,
                        "error", "Validation Error",
                        "message", message,
                        "path", request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                        "timestamp", Instant.now(),
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", "An unexpected error occurred while processing the request",
                        "path", request.getRequestURI()
                )
        );
    }
}
