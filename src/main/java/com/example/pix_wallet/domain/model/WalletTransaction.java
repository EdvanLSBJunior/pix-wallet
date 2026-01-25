package com.example.pix_wallet.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallet_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private WalletTransaction(
            Wallet wallet,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter
    ) {
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = Instant.now();
    }

    public static WalletTransaction credit(
            Wallet wallet,
            BigDecimal amount,
            BigDecimal balanceAfter
    ) {
        return new WalletTransaction(
                wallet,
                TransactionType.CREDIT,
                amount,
                balanceAfter
        );
    }

    public static WalletTransaction debit(
            Wallet wallet,
            BigDecimal amount,
            BigDecimal balanceAfter
    ) {
        return new WalletTransaction(
                wallet,
                TransactionType.DEBIT,
                amount,
                balanceAfter
        );
    }
}

