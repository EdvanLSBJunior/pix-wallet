package com.example.pix_wallet.domain.model;

import com.example.pix_wallet.domain.factory.WalletTestFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PixKeyTest {

    @Test
    void shouldCreateEmailPixKey() {
        Wallet wallet = WalletTestFactory.validWallet();

        PixKey pixKey = PixKey.createEmail("user@email.com", wallet);

        assertEquals(PixKeyType.EMAIL, pixKey.getType());
        assertEquals("user@email.com", pixKey.getValue());
    }
}
