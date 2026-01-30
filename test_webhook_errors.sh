#!/bin/bash

echo "🧪 Teste de Códigos de Erro do Webhook PIX"
echo "=========================================="

# Teste 1: EndToEndId inválido (deve retornar 404)
echo ""
echo "1. Testando endToEndId inválido (esperado: 404 Not Found)"
echo "curl -X POST http://localhost:8080/pix/webhook/events"

RESPONSE_404=$(curl -s -w "HTTPCODE:%{http_code}" -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d '{
    "endToEndId": "E2E-INVALID-123456",
    "status": "CONFIRMED",
    "timestamp": "2024-01-29T15:30:00.000Z"
  }')

HTTP_CODE_404=${RESPONSE_404##*HTTPCODE:}
BODY_404=${RESPONSE_404%HTTPCODE:*}

echo "Status Code: $HTTP_CODE_404"
echo "Response Body: $BODY_404"

if [ "$HTTP_CODE_404" = "404" ]; then
    echo "✅ Teste 1 PASSOU: Retornou 404 para endToEndId inválido"
else
    echo "❌ Teste 1 FALHOU: Esperado 404, mas retornou $HTTP_CODE_404"
fi

# Teste 2: Payload inválido (deve retornar 400)
echo ""
echo "2. Testando payload inválido (esperado: 400 Bad Request)"

RESPONSE_400=$(curl -s -w "HTTPCODE:%{http_code}" -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d '{
    "endToEndId": "",
    "status": "INVALID_STATUS",
    "timestamp": "invalid-timestamp"
  }')

HTTP_CODE_400=${RESPONSE_400##*HTTPCODE:}
BODY_400=${RESPONSE_400%HTTPCODE:*}

echo "Status Code: $HTTP_CODE_400"
echo "Response Body: $BODY_400"

if [ "$HTTP_CODE_400" = "400" ]; then
    echo "✅ Teste 2 PASSOU: Retornou 400 para payload inválido"
else
    echo "❌ Teste 2 FALHOU: Esperado 400, mas retornou $HTTP_CODE_400"
fi

# Teste 3: Criar uma transferência válida e testar evento duplicado
echo ""
echo "3. Testando evento duplicado (esperado: primeiro 200, segundo 409)"

# Primeiro criar uma transferência
echo "3.1. Criando transferência PIX para teste..."
TRANSFER_RESPONSE=$(curl -s -X POST http://localhost:8080/pix/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "fromWalletId": 1,
    "pixKeyType": "EMAIL",
    "pixKeyValue": "test@example.com",
    "amount": 100.00
  }' 2>/dev/null)

if [ $? -ne 0 ] || [ -z "$TRANSFER_RESPONSE" ]; then
    echo "ℹ️  Não foi possível criar transferência (talvez carteiras/chaves não existam)"
    echo "   Testando com endToEndId simulado..."
    END_TO_END_ID="E2E-test-duplicate-123"
else
    END_TO_END_ID=$(echo $TRANSFER_RESPONSE | grep -o 'E2E-[^"]*' | head -1)
    echo "   Transferência criada com endToEndId: $END_TO_END_ID"
fi

# Primeiro webhook (deve funcionar - 200 OK)
echo "3.2. Primeiro webhook..."
RESPONSE_FIRST=$(curl -s -w "HTTPCODE:%{http_code}" -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d "{
    \"endToEndId\": \"$END_TO_END_ID\",
    \"status\": \"CONFIRMED\",
    \"timestamp\": \"2024-01-29T15:30:00.000Z\"
  }")

HTTP_CODE_FIRST=${RESPONSE_FIRST##*HTTPCODE:}
echo "Status Code: $HTTP_CODE_FIRST"

# Segundo webhook idêntico (deve retornar 409 Conflict)
echo "3.3. Segundo webhook (duplicado)..."
RESPONSE_DUPLICATE=$(curl -s -w "HTTPCODE:%{http_code}" -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d "{
    \"endToEndId\": \"$END_TO_END_ID\",
    \"status\": \"CONFIRMED\",
    \"timestamp\": \"2024-01-29T15:30:00.000Z\"
  }")

HTTP_CODE_DUPLICATE=${RESPONSE_DUPLICATE##*HTTPCODE:}
BODY_DUPLICATE=${RESPONSE_DUPLICATE%HTTPCODE:*}

echo "Status Code: $HTTP_CODE_DUPLICATE"
echo "Response Body: $BODY_DUPLICATE"

if [ "$HTTP_CODE_DUPLICATE" = "409" ]; then
    echo "✅ Teste 3 PASSOU: Retornou 409 para evento duplicado"
else
    echo "❌ Teste 3 FALHOU: Esperado 409, mas retornou $HTTP_CODE_DUPLICATE"
fi

echo ""
echo "=========================================="
echo "📊 RESUMO DOS TESTES"
echo "Test 1 (404): $([[ "$HTTP_CODE_404" = "404" ]] && echo "✅ PASSOU" || echo "❌ FALHOU")"
echo "Test 2 (400): $([[ "$HTTP_CODE_400" = "400" ]] && echo "✅ PASSOU" || echo "❌ FALHOU")"
echo "Test 3 (409): $([[ "$HTTP_CODE_DUPLICATE" = "409" ]] && echo "✅ PASSOU" || echo "❌ FALHOU")"
echo "=========================================="
