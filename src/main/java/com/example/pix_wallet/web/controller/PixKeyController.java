package com.example.pix_wallet.web.controller;

import com.example.pix_wallet.domain.model.PixKey;
import com.example.pix_wallet.domain.service.RegisterPixKeyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wallets/{walletId}/pix-keys")
public class PixKeyController {

    private final RegisterPixKeyService registerPixKeyService;

    public PixKeyController(RegisterPixKeyService registerPixKeyService) {
        this.registerPixKeyService = registerPixKeyService;
    }

    @PostMapping("/evp")
    @ResponseStatus(HttpStatus.CREATED)
    public PixKey registerEVP(@PathVariable Long walletId) {
        return registerPixKeyService.registerEVP(walletId);
    }

    @PostMapping("/email")
    @ResponseStatus(HttpStatus.CREATED)
    public PixKey registerEmail(
            @PathVariable Long walletId,
            @RequestBody Map<String, String> body
    ) {
        return registerPixKeyService.registerEmail(walletId, body.get("email"));
    }

    @PostMapping("/phone")
    @ResponseStatus(HttpStatus.CREATED)
    public PixKey registerPhone(
            @PathVariable Long walletId,
            @RequestBody Map<String, String> body
    ) {
        return registerPixKeyService.registerPhone(walletId, body.get("phone"));
    }
}
