package com.example.pix_wallet.domain.repository;

import com.example.pix_wallet.domain.model.PixKey;
import com.example.pix_wallet.domain.model.PixKeyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PixKeyRepository extends JpaRepository<PixKey, Long> {

    boolean existsByTypeAndValue(PixKeyType type, String value);

    boolean existsByValue(String value);

    Optional<PixKey> findByTypeAndValue(PixKeyType type, String value);
}
