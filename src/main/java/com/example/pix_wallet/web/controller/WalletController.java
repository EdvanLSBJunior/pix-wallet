package com.example.pix_wallet.web.controller;

import com.example.pix_wallet.domain.model.Wallet;
import com.example.pix_wallet.domain.service.CreateWalletService;
import com.example.pix_wallet.domain.service.WalletOperationService;
import com.example.pix_wallet.web.dto.AmountRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final CreateWalletService createWalletService;
    private final WalletOperationService walletOperationService;

    public WalletController(CreateWalletService createWalletService,
                            WalletOperationService walletOperationService) {
        this.createWalletService = createWalletService;
        this.walletOperationService = walletOperationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Wallet createWallet() {
        return createWalletService.create();
    }

    @PostMapping("/{id}/credit")
    public Wallet credit(@PathVariable Long id,
                         @RequestBody AmountRequest request) {
        return walletOperationService.credit(id, request.amount());
    }

    @PostMapping("/{id}/debit")
    public Wallet debit(@PathVariable Long id,
                        @RequestBody AmountRequest request) {
        return walletOperationService.debit(id, request.amount());
    }
}

