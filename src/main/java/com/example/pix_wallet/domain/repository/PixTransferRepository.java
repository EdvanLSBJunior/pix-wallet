package com.example.pix_wallet.domain.repository;

import com.example.pix_wallet.domain.model.PixTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PixTransferRepository
        extends JpaRepository<PixTransfer, Long> {

    Optional<PixTransfer> findByEndToEndId(String endToEndId);
}
