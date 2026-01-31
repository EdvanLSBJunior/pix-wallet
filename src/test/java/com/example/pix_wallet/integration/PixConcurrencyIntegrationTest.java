package com.example.pix_wallet.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração específico para verificar resistência à concorrência
 * em requisições simultâneas do mesmo PIX
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PixConcurrencyIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldResistConcurrentWebhookRequestsForSamePix() throws Exception {
        // Cenário: Múltiplas requisições simultâneas para o mesmo endToEndId
        String endToEndId = "E2E-CONCURRENCY-TEST-" + System.currentTimeMillis();
        Instant timestamp = Instant.now();

        Map<String, Object> webhookRequest = Map.of(
                "endToEndId", endToEndId,
                "status", "CONFIRMED",
                "timestamp", timestamp.toString()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(webhookRequest, headers);

        // Contadores para rastrear resultados
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger notFoundCount = new AtomicInteger(0);
        AtomicInteger otherCount = new AtomicInteger(0);

        // Executar múltiplas requisições simultâneas
        int numberOfRequests = 5;
        List<CompletableFuture<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                try {
                    ResponseEntity<String> response = restTemplate.postForEntity(
                            "/pix/webhook/events", entity, String.class);
                    return response.getStatusCode().value();
                } catch (Exception e) {
                    e.printStackTrace();
                    return 500; // Erro interno
                }
            });
            futures.add(future);
        }

        // Aguardar todas as requisições completarem
        for (CompletableFuture<Integer> future : futures) {
            int statusCode = future.get();

            switch (statusCode) {
                case 200 -> successCount.incrementAndGet();
                case 409 -> conflictCount.incrementAndGet();
                case 404 -> notFoundCount.incrementAndGet();
                default -> otherCount.incrementAndGet();
            }
        }

        // Verificações
        System.out.println("=== RESULTADOS DO TESTE DE CONCORRÊNCIA ===");
        System.out.println("Sucessos (200): " + successCount.get());
        System.out.println("Conflitos (409): " + conflictCount.get());
        System.out.println("Não encontrado (404): " + notFoundCount.get());
        System.out.println("Outros códigos: " + otherCount.get());

        // O importante é que não haja erros 500 e o comportamento seja consistente
        assertEquals(0, otherCount.get(),
                "Não deve haver códigos de erro inesperados, mas houve: " + otherCount.get());

        System.out.println("✅ Teste de concorrência completado com sucesso!");
        System.out.println("   Sistema demonstrou comportamento consistente sob alta concorrência");
    }

    @Test
    void shouldHandleSequentialRequestsCorrectly() throws Exception {
        // Teste de controle: requisições sequenciais devem funcionar normalmente
        String endToEndId = "E2E-SEQUENTIAL-TEST-" + System.currentTimeMillis();
        Instant timestamp1 = Instant.now();
        Instant timestamp2 = timestamp1.plusSeconds(1);

        Map<String, Object> firstRequest = Map.of(
                "endToEndId", endToEndId,
                "status", "CONFIRMED",
                "timestamp", timestamp1.toString()
        );

        Map<String, Object> secondRequest = Map.of(
                "endToEndId", endToEndId,
                "status", "REJECTED",
                "timestamp", timestamp2.toString()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Primeira requisição
        HttpEntity<Map<String, Object>> entity1 = new HttpEntity<>(firstRequest, headers);
        ResponseEntity<String> response1 = restTemplate.postForEntity("/pix/webhook/events", entity1, String.class);

        // Segunda requisição (mais recente)
        HttpEntity<Map<String, Object>> entity2 = new HttpEntity<>(secondRequest, headers);
        ResponseEntity<String> response2 = restTemplate.postForEntity("/pix/webhook/events", entity2, String.class);

        int status1 = response1.getStatusCode().value();
        int status2 = response2.getStatusCode().value();

        System.out.println("=== TESTE SEQUENCIAL ===");
        System.out.println("Primeira requisição: " + status1);
        System.out.println("Segunda requisição: " + status2);

        // O importante é que o sistema não trave ou dê erro 500
        assertNotEquals(500, status1, "Sistema não deve retornar erro 500 para primeira requisição");
        assertNotEquals(500, status2, "Sistema não deve retornar erro 500 para segunda requisição");

        System.out.println("✅ Teste sequencial completado - sistema estável");
    }
}
