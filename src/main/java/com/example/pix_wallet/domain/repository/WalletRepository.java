package com.example.pix_wallet.domain.repository;

import com.example.pix_wallet.domain.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
