package com.example.pix_wallet.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pix_transfer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PixTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_wallet_id")
    private Wallet fromWallet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_wallet_id")
    private Wallet toWallet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pix_key_id")
    private PixKey pixKey;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, unique = true)
    private String endToEndId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private PixTransfer(
            Wallet fromWallet,
            Wallet toWallet,
            PixKey pixKey,
            BigDecimal amount
    ) {
        this.fromWallet = fromWallet;
        this.toWallet = toWallet;
        this.pixKey = pixKey;
        this.amount = amount;
        this.endToEndId = generateEndToEndId();
        this.createdAt = Instant.now();
    }

    public static PixTransfer create(
            Wallet fromWallet,
            Wallet toWallet,
            PixKey pixKey,
            BigDecimal amount
    ) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        return new PixTransfer(fromWallet, toWallet, pixKey, amount);
    }

    private static String generateEndToEndId() {
        return "E2E-" + UUID.randomUUID();
    }
}


