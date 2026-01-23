package com.example.pix_wallet.domain.factory;

import com.example.pix_wallet.domain.model.Wallet;

import java.math.BigDecimal;

public class WalletTestFactory {

    public static Wallet validWallet() {
        return Wallet.create();
    }

    public static Wallet walletWithBalance(BigDecimal balance) {
        Wallet wallet = Wallet.create();
        wallet.credit(balance);
        return wallet;
    }
}
