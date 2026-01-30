package com.example.pix_wallet.web.controller;

import com.example.pix_wallet.domain.service.PixWebhookService;
import com.example.pix_wallet.web.dto.PixWebhookEventRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pix/webhook")
public class PixWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(PixWebhookController.class);

    private final PixWebhookService pixWebhookService;

    public PixWebhookController(PixWebhookService pixWebhookService) {
        this.pixWebhookService = pixWebhookService;
    }

    @PostMapping("/events")
    public ResponseEntity<Void> receiveWebhookEvent(
            @RequestBody @Valid PixWebhookEventRequest request
    ) {
        logger.info("Received webhook event: {}", request);

        pixWebhookService.processWebhookEvent(
                request.endToEndId(),
                request.status(),
                request.timestamp()
        );

        return ResponseEntity.ok().build();
    }
}
