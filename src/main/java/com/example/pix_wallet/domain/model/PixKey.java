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
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"type", "value"})
        }
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

    @Column(nullable = false, length = 255)
    private String value;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private PixKey(PixKeyType type, String value, Wallet wallet) {
        this.type = type;
        this.value = value;
        this.wallet = wallet;
        this.createdAt = Instant.now();
    }

    public static PixKey createEVP(Wallet wallet) {
        return new PixKey(
                PixKeyType.EVP,
                UUID.randomUUID().toString(),
                wallet
        );
    }

    public static PixKey createEmail(String email, Wallet wallet) {
        return new PixKey(
                PixKeyType.EMAIL,
                email,
                wallet
        );
    }

    public static PixKey createPhone(String phone, Wallet wallet) {
        return new PixKey(
                PixKeyType.PHONE,
                phone,
                wallet
        );
    }
}
