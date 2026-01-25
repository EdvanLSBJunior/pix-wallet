package com.example.pix_wallet.domain.repository;

import com.example.pix_wallet.domain.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransaction, Long> {

    Optional<WalletTransaction> findFirstByWalletIdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(
            Long walletId,
            Instant createdAt
    );
}
