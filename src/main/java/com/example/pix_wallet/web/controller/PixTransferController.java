package com.example.pix_wallet.web.controller;

import com.example.pix_wallet.domain.model.PixTransfer;
import com.example.pix_wallet.domain.service.PixTransferService;
import com.example.pix_wallet.web.dto.PixTransferRequest;
import com.example.pix_wallet.web.dto.PixTransferResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pix/transfers")
public class PixTransferController {

    private final PixTransferService pixTransferService;

    public PixTransferController(PixTransferService pixTransferService) {
        this.pixTransferService = pixTransferService;
    }

    @PostMapping
    public ResponseEntity<PixTransferResponse> transfer(
            @RequestBody @Valid PixTransferRequest request
    ) {
        PixTransfer transfer = pixTransferService.transfer(
                request.fromWalletId(),
                request.pixKeyType(),
                request.pixKeyValue(),
                request.amount()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PixTransferResponse.from(transfer));
    }
}

