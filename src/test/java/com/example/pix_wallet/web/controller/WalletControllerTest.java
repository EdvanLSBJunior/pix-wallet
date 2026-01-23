package com.example.pix_wallet.web.controller;

import com.example.pix_wallet.domain.exception.InsufficientBalanceException;
import com.example.pix_wallet.domain.exception.WalletNotFoundException;
import com.example.pix_wallet.domain.model.Wallet;
import com.example.pix_wallet.domain.service.CreateWalletService;
import com.example.pix_wallet.domain.service.WalletOperationService;
import com.example.pix_wallet.domain.service.WalletQueryService;
import com.example.pix_wallet.domain.service.WalletTransferService;
import com.example.pix_wallet.web.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
@Import(GlobalExceptionHandler.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateWalletService createWalletService;

    @MockBean
    private WalletOperationService walletOperationService;

    @MockBean
    private WalletTransferService walletTransferService;

    @MockBean
    private WalletQueryService walletQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateWallet() throws Exception {
        Wallet wallet = Wallet.create();

        when(createWalletService.create()).thenReturn(wallet);

        mockMvc.perform(post("/wallets"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value(0.00));
    }

    @Test
    void shouldReturn422WhenInsufficientBalance() throws Exception {
        doThrow(new InsufficientBalanceException(BigDecimal.ZERO, new BigDecimal("10")))
                .when(walletOperationService)
                .debit(1L, new BigDecimal("10"));

        mockMvc.perform(post("/wallets/1/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", 10))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldReturn404WhenWalletNotFound() throws Exception {
        doThrow(new WalletNotFoundException(99L))
                .when(walletOperationService)
                .credit(99L, new BigDecimal("10"));

        mockMvc.perform(post("/wallets/99/credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", 10))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenInvalidTransferRequest() throws Exception {
        mockMvc.perform(post("/wallets/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of(
                                        "fromWalletId", 1,
                                        "toWalletId", 2,
                                        "amount", -10
                                )
                        )))
                .andExpect(status().isBadRequest());
    }
}
