package com.example.pix_wallet.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "pix_key",
        uniqueConstraints = @UniqueConstraint(columnNames = "value")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PixKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PixKeyType type;

    @Column(nullable = false, unique = true)
    private String value;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private PixKey(PixKeyType type, String value, Wallet wallet) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Pix key value cannot be null or blank");
        }
        this.type = type;
        this.value = value;
        this.wallet = wallet;
        this.createdAt = Instant.now();
    }

    public static PixKey create(PixKeyType type, String value, Wallet wallet) {
        return switch (type) {
            case EMAIL -> createEmail(value, wallet);
            case PHONE -> createPhone(value, wallet);
            case EVP -> createEVP(wallet);
        };
    }

    public static PixKey createEmail(String email, Wallet wallet) {
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Invalid email Pix key");
        }
        return new PixKey(PixKeyType.EMAIL, email, wallet);
    }

    public static PixKey createPhone(String phone, Wallet wallet) {
        if (!phone.matches("^\\+?[1-9]\\d{10,14}$")) {
            throw new IllegalArgumentException("Invalid phone Pix key");
        }
        return new PixKey(PixKeyType.PHONE, phone, wallet);
    }

    public static PixKey createEVP(Wallet wallet) {
        return new PixKey(
                PixKeyType.EVP,
                UUID.randomUUID().toString(),
                wallet
        );
    }
}
