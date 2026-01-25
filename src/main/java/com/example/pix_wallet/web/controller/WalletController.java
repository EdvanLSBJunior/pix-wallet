package com.example.pix_wallet.web.controller;

import com.example.pix_wallet.domain.dto.TransferRequest;
import com.example.pix_wallet.domain.model.Wallet;
import com.example.pix_wallet.domain.service.CreateWalletService;
import com.example.pix_wallet.domain.service.WalletOperationService;
import com.example.pix_wallet.domain.service.WalletQueryService;
import com.example.pix_wallet.domain.service.WalletTransferService;
import com.example.pix_wallet.web.dto.AmountRequest;
import com.example.pix_wallet.web.dto.WalletBalanceResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final CreateWalletService createWalletService;
    private final WalletOperationService walletOperationService;
    private final WalletQueryService walletQueryService;
    private final WalletTransferService walletTransferService;

    public WalletController(CreateWalletService createWalletService,
                            WalletOperationService walletOperationService, WalletQueryService walletQueryService, WalletTransferService walletTransferService) {
        this.createWalletService = createWalletService;
        this.walletOperationService = walletOperationService;
        this.walletQueryService = walletQueryService;
        this.walletTransferService = walletTransferService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Wallet createWallet() {
        return createWalletService.create();
    }

    @PostMapping("/{id}/credit")
    public void credit(@PathVariable Long id,
                         @RequestBody AmountRequest request) {
        walletOperationService.credit(id, request.amount());
    }

    @PostMapping("/{id}/debit")
    public void debit(@PathVariable Long id,
                        @RequestBody AmountRequest request) {
        walletOperationService.debit(id, request.amount());
    }

    @GetMapping("/{id}")
    public Wallet getById(@PathVariable Long id) {
        return walletQueryService.getById(id);
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transfer(@Valid @RequestBody TransferRequest request) {
        walletTransferService.transfer(
                request.fromWalletId(),
                request.toWalletId(),
                request.amount()
        );
    }

//    @GetMapping("/{Id}/balance")
//    public BigDecimal getCurrentBalance(@PathVariable Long walletId) {
//        return walletQueryService.getCurrentBalance(walletId);
//    }

    @GetMapping("/wallets/{id}/balance")
    public ResponseEntity<Map<String, BigDecimal>> getCurrentBalance(
            @PathVariable Long id
    ) {
        BigDecimal balance = walletQueryService.getBalance(id);
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    @GetMapping("/{id}/balance/history")
    public ResponseEntity<WalletBalanceResponse> getBalanceAt(
            @PathVariable Long id,
            @RequestParam Instant at
    ) {
        BigDecimal balance = walletQueryService.getBalanceAt(id, at);
        return ResponseEntity.ok(new WalletBalanceResponse(balance));
    }
}

