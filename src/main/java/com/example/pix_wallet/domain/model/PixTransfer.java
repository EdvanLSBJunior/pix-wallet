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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PixTransferStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant lastStatusUpdate;

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
        this.status = PixTransferStatus.PENDING;
        this.createdAt = Instant.now();
        this.lastStatusUpdate = Instant.now();
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

    public boolean updateStatus(PixTransferStatus newStatus, Instant eventTimestamp) {
        // Ignora eventos duplicados ou mais antigos
        if (this.lastStatusUpdate != null &&
            eventTimestamp.isBefore(this.lastStatusUpdate)) {
            return false;
        }

        // Ignora se o status já é o mesmo
        if (this.status == newStatus) {
            return false;
        }

        // Não permite mudar de CONFIRMED ou REJECTED para outro status
        if (this.status == PixTransferStatus.CONFIRMED ||
            this.status == PixTransferStatus.REJECTED) {
            return false;
        }

        this.status = newStatus;
        this.lastStatusUpdate = eventTimestamp;
        return true;
    }

    public boolean isPending() {
        return this.status == PixTransferStatus.PENDING;
    }

    public boolean isConfirmed() {
        return this.status == PixTransferStatus.CONFIRMED;
    }

    public boolean isRejected() {
        return this.status == PixTransferStatus.REJECTED;
    }
}


