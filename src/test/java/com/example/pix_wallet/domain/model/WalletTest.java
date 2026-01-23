package com.example.pix_wallet.domain.model;

import com.example.pix_wallet.domain.exception.InsufficientBalanceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Test
    void shouldCreateWalletWithZeroBalance() {
        Wallet wallet = Wallet.create();

        assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }

    @Test
    void shouldCreditAmount() {
        Wallet wallet = Wallet.create();

        wallet.credit(new BigDecimal("100.00"));

        assertEquals(new BigDecimal("100.00"), wallet.getBalance());
    }

    @Test
    void shouldDebitAmount() {
        Wallet wallet = Wallet.create();
        wallet.credit(new BigDecimal("100.00"));

        wallet.debit(new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), wallet.getBalance());
    }

    @Test
    void shouldThrowExceptionWhenDebitGreaterThanBalance() {
        Wallet wallet = Wallet.create();
        wallet.credit(new BigDecimal("50.00"));

        assertThrows(
                InsufficientBalanceException.class,
                () -> wallet.debit(new BigDecimal("100.00"))
        );
    }

    @Test
    void shouldThrowExceptionWhenCreditWithNegativeAmount() {
        Wallet wallet = Wallet.create();

        assertThrows(
                IllegalArgumentException.class,
                () -> wallet.credit(new BigDecimal("-10.00"))
        );
    }
}

