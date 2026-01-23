package com.example.pix_wallet.web.exception;

import com.example.pix_wallet.domain.exception.InsufficientBalanceException;
import com.example.pix_wallet.domain.exception.InvalidTransferException;
import com.example.pix_wallet.domain.exception.PixKeyAlreadyExistsException;
import com.example.pix_wallet.domain.exception.WalletNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
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
}
