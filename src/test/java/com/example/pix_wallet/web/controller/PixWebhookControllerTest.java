package com.example.pix_wallet.web.controller;

import com.example.pix_wallet.domain.exception.TransferNotFoundException;
import com.example.pix_wallet.domain.exception.WebhookEventIgnoredException;
import com.example.pix_wallet.domain.model.PixTransferStatus;
import com.example.pix_wallet.domain.service.PixWebhookService;
import com.example.pix_wallet.web.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PixWebhookController.class)
@Import(GlobalExceptionHandler.class)
class PixWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PixWebhookService pixWebhookService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldReceiveConfirmedWebhookEvent() throws Exception {
        String endToEndId = "E2E-123456";
        Instant timestamp = Instant.now();

        Map<String, Object> request = Map.of(
                "endToEndId", endToEndId,
                "status", "CONFIRMED",
                "timestamp", timestamp.toString()
        );

        mockMvc.perform(post("/pix/webhook/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(pixWebhookService).processWebhookEvent(endToEndId, PixTransferStatus.CONFIRMED, timestamp);
    }

    @Test
    void shouldReceiveRejectedWebhookEvent() throws Exception {
        String endToEndId = "E2E-789012";
        Instant timestamp = Instant.now();

        Map<String, Object> request = Map.of(
                "endToEndId", endToEndId,
                "status", "REJECTED",
                "timestamp", timestamp.toString()
        );

        mockMvc.perform(post("/pix/webhook/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(pixWebhookService).processWebhookEvent(endToEndId, PixTransferStatus.REJECTED, timestamp);
    }

    @Test
    void shouldReturnBadRequestForInvalidPayload() throws Exception {
        Map<String, Object> request = Map.of(
                "endToEndId", "",  // Invalid: blank
                "status", "CONFIRMED", // Status válido
                "timestamp", "2024-01-29T15:30:00.000Z" // Timestamp válido
        );

        mockMvc.perform(post("/pix/webhook/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(pixWebhookService, never()).processWebhookEvent(any(), any(), any());
    }

    @Test
    void shouldReturnInternalServerErrorWhenServiceThrowsException() throws Exception {
        String endToEndId = "E2E-ERROR";
        Instant timestamp = Instant.now();

        Map<String, Object> request = Map.of(
                "endToEndId", endToEndId,
                "status", "CONFIRMED",
                "timestamp", timestamp.toString()
        );

        doThrow(new RuntimeException("Service error"))
                .when(pixWebhookService)
                .processWebhookEvent(endToEndId, PixTransferStatus.CONFIRMED, timestamp);

        mockMvc.perform(post("/pix/webhook/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void shouldReturnNotFoundForInvalidEndToEndId() throws Exception {
        String endToEndId = "E2E-INVALID";
        Instant timestamp = Instant.now();

        Map<String, Object> request = Map.of(
                "endToEndId", endToEndId,
                "status", "CONFIRMED",
                "timestamp", timestamp.toString()
        );

        doThrow(new TransferNotFoundException(endToEndId))
                .when(pixWebhookService)
                .processWebhookEvent(endToEndId, PixTransferStatus.CONFIRMED, timestamp);

        mockMvc.perform(post("/pix/webhook/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Transfer Not Found"))
                .andExpect(jsonPath("$.message").value("Transfer not found for endToEndId: " + endToEndId));
    }

    @Test
    void shouldReturnConflictForDuplicateEvent() throws Exception {
        String endToEndId = "E2E-DUPLICATE";
        Instant timestamp = Instant.now();

        Map<String, Object> request = Map.of(
                "endToEndId", endToEndId,
                "status", "CONFIRMED",
                "timestamp", timestamp.toString()
        );

        doThrow(new WebhookEventIgnoredException("Duplicate event"))
                .when(pixWebhookService)
                .processWebhookEvent(endToEndId, PixTransferStatus.CONFIRMED, timestamp);

        mockMvc.perform(post("/pix/webhook/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Webhook Event Ignored"));
    }
}
