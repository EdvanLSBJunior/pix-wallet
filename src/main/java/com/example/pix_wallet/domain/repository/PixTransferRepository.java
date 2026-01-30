package com.example.pix_wallet.domain.repository;

import com.example.pix_wallet.domain.model.PixTransfer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PixTransferRepository
        extends JpaRepository<PixTransfer, Long> {

    Optional<PixTransfer> findByEndToEndId(String endToEndId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pt FROM PixTransfer pt WHERE pt.endToEndId = :endToEndId")
    Optional<PixTransfer> findByEndToEndIdWithLock(@Param("endToEndId") String endToEndId);
}
