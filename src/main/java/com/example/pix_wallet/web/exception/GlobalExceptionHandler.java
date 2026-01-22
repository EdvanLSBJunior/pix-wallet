package com.example.pix_wallet.web.exception;

import com.example.pix_wallet.domain.exception.InsufficientBalanceException;
import com.example.pix_wallet.domain.exception.WalletNotFoundException;
import org.springframework.http.HttpStatus;
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
}
