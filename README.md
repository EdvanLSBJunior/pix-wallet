# PIX Wallet 💳

Um sistema de carteira digital que implementa funcionalidades do PIX (sistema de pagamentos instantâneos brasileiro), desenvolvido com **Spring Boot 3**, **Java 17** e **PostgreSQL**.

## 🚀 Funcionalidades

### 💰 Gerenciamento de Carteiras
- **Criar carteira**: Criação de novas carteiras digitais
- **Consultar saldo**: Verificação do saldo atual
- **Histórico de saldo**: Consulta do saldo em uma data específica
- **Operações básicas**: Crédito e débito na carteira
- **Transferências**: Transferência entre carteiras

### 🔑 Chaves PIX
- **Cadastro de chaves PIX**:
  - **Email**: Validação de formato de email
  - **Telefone**: Validação de formato de telefone (+55 11 99999-9999)
  - **EVP (Chave Aleatória)**: Geração automática de UUID
- **Validação de unicidade**: Cada chave PIX deve ser única no sistema

### 🔄 Transferências PIX
- **Transferência via chave PIX**: Transferência usando email, telefone ou EVP
- **Validações**: Saldo suficiente, chave PIX válida
- **Rastreamento**: Geração de End-to-End ID para cada transferência
- **Status de transferência**: PENDING, CONFIRMED, REJECTED
- **Histórico**: Registro completo de todas as transferências

### 🔗 Webhook PIX (Simulado)
- **Recebimento de eventos**: Endpoint que recebe eventos CONFIRMED/REJECTED
- **Controle de duplicação**: Ignora eventos duplicados baseado no timestamp
- **Processamento fora de ordem**: Garante que eventos mais antigos não sobrescrevam mais recentes
- **Reversão automática**: Estorna transferências rejeitadas automaticamente
- **Idempotência**: Múltiplos eventos com mesmo status são ignorados

## 🛠️ Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.5.9**
- **Spring Data JPA**
- **Spring Web**
- **Spring Validation**
- **PostgreSQL 15**
- **Lombok**
- **Flyway** (migrations de banco)
- **JUnit 5** (testes)
- **Mockito** (mocks para testes)
- **Docker Compose** (infraestrutura)

## 📋 Pré-requisitos

- **Java 17** ou superior
- **Maven 3.8+**
- **Docker** e **Docker Compose** (para PostgreSQL)

## ⚙️ Configuração e Execução

### 1. Clone o repositório
```bash
git clone <url-do-repositorio>
cd pix-wallet
```

### 2. Inicie o banco de dados
```bash
docker-compose up -d
```

### 3. Execute a aplicação
```bash
# No Windows
mvnw.cmd spring-boot:run

# No Linux/Mac
./mvnw spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### 4. Execute os testes
```bash
# No Windows
mvnw.cmd test

# No Linux/Mac
./mvnw test
```

## 📊 Banco de Dados

O sistema utiliza **PostgreSQL** com as seguintes tabelas:

- **wallet**: Armazena as carteiras
- **pix_key**: Chaves PIX associadas às carteiras
- **pix_transfer**: Histórico de transferências PIX
- **wallet_transaction**: Histórico de transações das carteiras

### Configuração do Banco
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pix_wallet
    username: postgres
    password: postgres
```

## 🔌 API Endpoints

### 🏦 Carteiras (`/wallets`)

#### Criar Carteira
```http
POST /wallets
```

#### Consultar Carteira
```http
GET /wallets/{id}
```

#### Operações de Saldo
```http
# Creditar
POST /wallets/{id}/credit
Content-Type: application/json

{
    "amount": 100.50
}

# Debitar
POST /wallets/{id}/debit
Content-Type: application/json

{
    "amount": 50.00
}

# Consultar saldo atual
GET /wallets/{id}/balance

# Consultar saldo histórico
GET /wallets/{id}/balance/history?at=2024-01-01T10:00:00Z
```

#### Transferência entre Carteiras
```http
POST /wallets/transfer
Content-Type: application/json

{
    "fromWalletId": 1,
    "toWalletId": 2,
    "amount": 25.00
}
```

### 🔑 Chaves PIX (`/wallets/{walletId}/pix-keys`)

#### Cadastrar Chave EVP (Aleatória)
```http
POST /wallets/1/pix-keys/evp
```

#### Cadastrar Chave Email
```http
POST /wallets/1/pix-keys/email
Content-Type: application/json

{
    "email": "usuario@exemplo.com"
}
```

#### Cadastrar Chave Telefone
```http
POST /wallets/1/pix-keys/phone
Content-Type: application/json

{
    "phone": "+5511999999999"
}
```

### 🔄 Transferências PIX (`/pix/transfers`)

#### Realizar Transferência PIX
```http
POST /pix/transfers
Content-Type: application/json

{
    "fromWalletId": 1,
    "pixKeyType": "EMAIL",
    "pixKeyValue": "destino@exemplo.com",
    "amount": 75.00
}
```

**Tipos de Chave PIX Aceitos:**
- `EMAIL`: Endereço de email
- `PHONE`: Número de telefone
- `EVP`: Chave aleatória (UUID)

**Resposta:**
```json
{
    "endToEndId": "E2E-123e4567-e89b-12d3-a456-426614174000",
    "amount": 75.00,
    "toWalletId": 2,
    "status": "PENDING",
    "createdAt": "2024-01-01T10:00:00Z"
}
```

### 🔗 Webhook PIX (`/pix/webhook`)

#### Receber Evento de Status da Transferência
```http
POST /pix/webhook/events
Content-Type: application/json

{
    "endToEndId": "E2E-123e4567-e89b-12d3-a456-426614174000",
    "status": "CONFIRMED",
    "timestamp": "2024-01-01T10:01:00Z"
}
```

**Status Aceitos:**
- `CONFIRMED`: Transferência confirmada
- `REJECTED`: Transferência rejeitada (será revertida automaticamente)

**Características do Webhook:**
- **Idempotência**: Eventos duplicados são ignorados (retorna 409 Conflict)
- **Ordenação**: Eventos mais antigos não sobrescrevem eventos mais recentes
- **Reversão Automática**: Transferências rejeitadas são automaticamente estornadas
- **Status Final**: Uma vez CONFIRMED ou REJECTED, o status não pode ser alterado
- **Códigos de Erro** (tratados pelo GlobalExceptionHandler):
  - `200 OK`: Evento processado com sucesso
  - `404 Not Found`: Transfer não encontrado para o endToEndId informado
  - `409 Conflict`: Evento duplicado ou mais antigo que o já processado
  - `400 Bad Request`: Dados inválidos na requisição (validação)
  - `500 Internal Server Error`: Erro interno do servidor

### 🧪 **Testar Códigos de Erro**
```powershell
# Execute o script de teste (Windows PowerShell)
.\test_webhook_errors.ps1

# Ou manualmente:
# Teste 404 - endToEndId inválido
curl -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d '{"endToEndId": "E2E-INVALID", "status": "CONFIRMED", "timestamp": "2024-01-29T15:30:00.000Z"}'
```

## 📝 Exemplos de Uso

### Cenário Completo: Transferência PIX

1. **Criar duas carteiras**
```bash
# Carteira do remetente
curl -X POST http://localhost:8080/wallets

# Carteira do destinatário  
curl -X POST http://localhost:8080/wallets
```

2. **Adicionar saldo na carteira do remetente**
```bash
curl -X POST http://localhost:8080/wallets/1/credit \
  -H "Content-Type: application/json" \
  -d '{"amount": 500.00}'
```

3. **Cadastrar chave PIX para o destinatário**
```bash
curl -X POST http://localhost:8080/wallets/2/pix-keys/email \
  -H "Content-Type: application/json" \
  -d '{"email": "destino@exemplo.com"}'
```

4. **Realizar transferência PIX**
```bash
curl -X POST http://localhost:8080/pix/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "fromWalletId": 1,
    "pixKeyType": "EMAIL", 
    "pixKeyValue": "destino@exemplo.com",
    "amount": 100.00
  }'
```

5. **Simular confirmação ou rejeição via webhook**

O webhook PIX permite confirmar ou rejeitar transferências. Use o `endToEndId` retornado na resposta da transferência.

### 📋 **Exemplos de Webhook - CONFIRMED**

```bash
# Confirmar a transferência (valores são movimentados)
curl -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d '{
    "endToEndId": "E2E-123e4567-e89b-12d3-a456-426614174000",
    "status": "CONFIRMED",
    "timestamp": "2024-01-29T15:30:00.000Z"
  }'

# Resposta esperada: 200 OK
# Efeito: O valor é debitado da carteira origem e creditado na carteira destino
```

### ❌ **Exemplos de Webhook - REJECTED**

```bash
# Rejeitar a transferência (nenhuma movimentação acontece)
curl -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d '{
    "endToEndId": "E2E-123e4567-e89b-12d3-a456-426614174000",
    "status": "REJECTED",
    "timestamp": "2024-01-29T15:31:00.000Z"
  }'

# Resposta esperada: 200 OK
# Efeito: Transferência marcada como rejeitada, valores permanecem inalterados
```

### 🧪 **Exemplo Completo - Cenário CONFIRMED**

```bash
#!/bin/bash

# 1. Criar transferência PIX e capturar endToEndId
echo "Criando transferência PIX..."
RESPONSE=$(curl -s -X POST http://localhost:8080/pix/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "fromWalletId": 1,
    "pixKeyType": "EMAIL",
    "pixKeyValue": "destino@exemplo.com",
    "amount": 100.00
  }')

echo "Transferência criada: $RESPONSE"

# 2. Extrair o endToEndId
END_TO_END_ID=$(echo $RESPONSE | jq -r '.endToEndId')
echo "EndToEndId: $END_TO_END_ID"

# 3. Verificar status inicial (PENDING)
echo "Status inicial: PENDING (valores ainda não movimentados)"

# 4. Confirmar via webhook
echo "Confirmando transferência via webhook..."
curl -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d "{
    \"endToEndId\": \"$END_TO_END_ID\",
    \"status\": \"CONFIRMED\",
    \"timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%S.000Z)\"
  }"

echo "✅ Transferência confirmada! Valores foram movimentados."
```

### 🧪 **Exemplo Completo - Cenário REJECTED**

```bash
#!/bin/bash

# 1. Criar transferência PIX
RESPONSE=$(curl -s -X POST http://localhost:8080/pix/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "fromWalletId": 1,
    "pixKeyType": "EMAIL", 
    "pixKeyValue": "destino@exemplo.com",
    "amount": 50.00
  }')

END_TO_END_ID=$(echo $RESPONSE | jq -r '.endToEndId')

# 2. Rejeitar via webhook
echo "Rejeitando transferência via webhook..."
curl -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d "{
    \"endToEndId\": \"$END_TO_END_ID\",
    \"status\": \"REJECTED\",
    \"timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%S.000Z)\"
  }"

echo "❌ Transferência rejeitada! Valores permaneceram nas carteiras originais."
```

### 🚫 **Exemplos de Casos de Erro**

```bash
# Erro 404 - EndToEndId não encontrado
curl -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d '{
    "endToEndId": "E2E-INVALID-ID",
    "status": "CONFIRMED",
    "timestamp": "2024-01-29T15:30:00.000Z"
  }'

# Resposta: 404 Not Found
# {
#   "timestamp": "2024-01-29T15:30:00.000Z",
#   "status": 404,
#   "error": "Transfer Not Found",
#   "message": "Transfer not found for endToEndId: E2E-INVALID-ID",
#   "path": "/pix/webhook/events"
# }

# Erro 409 - Evento duplicado
curl -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d '{
    "endToEndId": "E2E-ALREADY-PROCESSED",
    "status": "CONFIRMED",
    "timestamp": "2024-01-29T15:30:00.000Z"
  }'

# Resposta: 409 Conflict  
# {
#   "timestamp": "2024-01-29T15:30:00.000Z",
#   "status": 409,
#   "error": "Webhook Event Ignored", 
#   "message": "Webhook event ignored: Duplicate or outdated event for transfer 123",
#   "path": "/pix/webhook/events"
# }

# Erro 400 - Payload inválido
curl -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d '{
    "endToEndId": "",
    "status": "CONFIRMED",
    "timestamp": "2024-01-29T15:30:00.000Z"
  }'

# Resposta: 400 Bad Request
# {
#   "timestamp": "2024-01-29T15:30:00.000Z", 
#   "status": 400,
#   "error": "Validation Error",
#   "message": "endToEndId: must not be blank",
#   "path": "/pix/webhook/events"
# }
```

**⚠️ Importante**: 
- Use o `endToEndId` retornado na resposta da transferência PIX
- O `timestamp` deve estar no formato ISO-8601 (ex: `2024-01-01T10:01:00.000Z`)
- Os status válidos são: `CONFIRMED` ou `REJECTED`

### Exemplo Completo de Teste do Webhook

```bash
# 1. Fazer transferência PIX e capturar o endToEndId
RESPONSE=$(curl -s -X POST http://localhost:8080/pix/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "fromWalletId": 1,
    "pixKeyType": "EMAIL", 
    "pixKeyValue": "destino@exemplo.com",
    "amount": 100.00
  }')

echo "Transferência criada: $RESPONSE"

# 2. Extrair o endToEndId da resposta (se usando jq)
END_TO_END_ID=$(echo $RESPONSE | jq -r '.endToEndId')
echo "EndToEndId: $END_TO_END_ID"

# 3. Confirmar via webhook
curl -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d "{
    \"endToEndId\": \"$END_TO_END_ID\",
    \"status\": \"CONFIRMED\",
    \"timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)\"
  }"
```

### 💻 **Exemplos PowerShell (Windows)**

Para usuários Windows, aqui estão os exemplos usando PowerShell:

#### **Webhook CONFIRMED (PowerShell)**
```powershell
# Confirmar transferência
$body = @{
    endToEndId = "E2E-123e4567-e89b-12d3-a456-426614174000"
    status = "CONFIRMED"
    timestamp = "2024-01-29T15:30:00.000Z"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/pix/webhook/events" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

#### **Webhook REJECTED (PowerShell)**
```powershell
# Rejeitar transferência
$body = @{
    endToEndId = "E2E-123e4567-e89b-12d3-a456-426614174000" 
    status = "REJECTED"
    timestamp = "2024-01-29T15:31:00.000Z"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/pix/webhook/events" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

#### **Exemplo Completo PowerShell**
```powershell
# 1. Criar transferência PIX
$transferBody = @{
    fromWalletId = 1
    pixKeyType = "EMAIL"
    pixKeyValue = "destino@exemplo.com" 
    amount = 100.00
} | ConvertTo-Json

$transferResponse = Invoke-RestMethod -Uri "http://localhost:8080/pix/transfers" `
    -Method POST `
    -ContentType "application/json" `
    -Body $transferBody

Write-Host "Transferência criada com endToEndId: $($transferResponse.endToEndId)"

# 2. Confirmar via webhook
$webhookBody = @{
    endToEndId = $transferResponse.endToEndId
    status = "CONFIRMED" 
    timestamp = (Get-Date -AsUTC).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8080/pix/webhook/events" `
        -Method POST `
        -ContentType "application/json" `
        -Body $webhookBody
    
    Write-Host "✅ Transferência confirmada com sucesso!" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ Erro $statusCode : $($_.Exception.Message)" -ForegroundColor Red
}
```

#### **Testar Códigos de Erro (PowerShell)**
```powershell
# Executar script de teste automático
.\test_webhook_errors.ps1

# Ou testar manualmente:

# Teste 404 - endToEndId inválido
try {
    $errorBody = @{
        endToEndId = "E2E-INVALID-123"
        status = "CONFIRMED"
        timestamp = "2024-01-29T15:30:00.000Z"
    } | ConvertTo-Json
    
    Invoke-RestMethod -Uri "http://localhost:8080/pix/webhook/events" `
        -Method POST -ContentType "application/json" -Body $errorBody
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "Código de status: $statusCode" -ForegroundColor Yellow
}
```

## 🧪 Estrutura de Testes

O projeto possui testes unitários abrangentes:

- **Testes de Modelo**: Validação das entidades e regras de negócio
- **Testes de Serviço**: Lógica de negócios e operações
- **Testes de Controller**: Endpoints da API REST
- **Testes de Repositório**: Operações de persistência

### Executar testes específicos
```bash
# Todos os testes
mvnw.cmd test

# Testes de um serviço específico
mvnw.cmd -Dtest=WalletOperationServiceTest test

# Testes de um controller específico  
mvnw.cmd -Dtest=WalletControllerTest test
```

### 📋 **Scripts de Exemplo Prontos**

O projeto inclui scripts PowerShell prontos para testar os webhooks:

```powershell
# Testar webhook CONFIRMED (valores são movimentados)
.\webhook_confirmed_example.ps1

# Testar webhook REJECTED (valores permanecem inalterados)  
.\webhook_rejected_example.ps1

# Testar códigos de erro (404, 409, 400)
.\test_webhook_errors.ps1
```

**Características dos scripts:**
- ✅ Criam transferências automaticamente
- ✅ Verificam saldos antes e depois  
- ✅ Mostram diferenças de comportamento CONFIRMED vs REJECTED
- ✅ Testam códigos de erro (404, 409, 400)
- ✅ Interface colorida e informativa

## 🔒 Tratamento de Erros

A API possui tratamento global de exceções com retornos padronizados:

### Erros Comuns

#### Carteira não encontrada (404)
```json
{
    "timestamp": "2024-01-01T10:00:00Z",
    "status": 404,
    "error": "Wallet not found: 999"
}
```

#### Saldo insuficiente (422)
```json
{
    "timestamp": "2024-01-01T10:00:00Z", 
    "status": 422,
    "error": "Insufficient balance. Balance: 50.00, amount: 100.00"
}
```

#### Transferência inválida (422)
```json
{
    "timestamp": "2024-01-01T10:00:00Z",
    "status": 422, 
    "error": "Source and destination wallets must be different"
}
```

#### Chave PIX não encontrada (404)
```json
{
    "timestamp": "2024-01-01T10:00:00Z",
    "status": 404,
    "error": "Pix key not found: usuario@exemplo.com"
}
```

## 🏗️ Arquitetura

O projeto segue uma **arquitetura em camadas** com separação clara de responsabilidades:

```
src/main/java/com/example/pix_wallet/
│
├── domain/                 # Camada de Domínio
│   ├── model/             # Entidades JPA
│   ├── repository/        # Interfaces de repositório
│   ├── service/          # Lógica de negócio
│   ├── exception/        # Exceções customizadas
│   └── dto/              # Objects de transferência
│
└── web/                   # Camada de Apresentação
    ├── controller/        # Controllers REST
    ├── dto/              # DTOs de request/response
    └── exception/        # Tratamento de exceções
```

### Princípios Aplicados

- **Domain-Driven Design (DDD)**: Modelagem rica do domínio
- **Clean Architecture**: Separação de responsabilidades
- **SOLID**: Princípios de design orientado a objetos
- **Transaction Management**: Controle transacional automático
- **Validation**: Validação de entrada com Bean Validation

### Fluxo do Webhook PIX

```
1. Transferência PIX criada (status: PENDING)
   → Apenas validação de saldo, SEM movimentação
2. Sistema externo processa a transferência
3. Webhook recebe evento CONFIRMED/REJECTED
4. Sistema valida timestamp e evita duplicatas
5. Status é atualizado (se válido)
6. Se CONFIRMED: valores são transferidos (débito + crédito)
7. Se REJECTED: nada acontece (não houve movimentação prévia)
```

**Fluxo de Valores:**
- ✅ **PENDING**: Transferência registrada, valores permanecem nas carteiras originais
- ✅ **CONFIRMED**: Valores são debitados da origem e creditados no destino
- ✅ **REJECTED**: Não há movimentação (valores já estão corretos)

**Características de Segurança:**
- ✅ **Exactly-Once Processing**: Lock pessimista + transações garantem processamento único
- ✅ **Idempotência**: Eventos duplicados são ignorados
- ✅ **Ordenação temporal**: Eventos antigos não sobrescrevem recentes
- ✅ **Estados finais**: CONFIRMED/REJECTED não podem ser alterados
- ✅ **Atomicidade**: Operações de débito/crédito são atômicas
- ✅ **Consistência**: Constraint única no banco previne duplicações
- ✅ **Optimistic Locking**: Controle de versão em carteiras previne race conditions

### 🛡️ **Garantias Exactly-Once (Missão Crítica)**

O sistema implementa múltiplas camadas de proteção para evitar inconsistências e garantir processamento exactly-once:

#### **1. Lock Pessimista no Webhook**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<PixTransfer> findByEndToEndIdWithLock(String endToEndId);
```
- **Previne**: Múltiplos webhooks simultâneos processando o mesmo transfer
- **Garante**: Apenas uma thread por vez pode modificar um transfer

#### **2. Controle de Versão Otimista nas Carteiras**
```java
@Version
private Long version;  // Na entidade Wallet
```
- **Previne**: Race conditions em operações simultâneas na mesma carteira
- **Garante**: Falha rápida se carteira foi modificada por outra transação

#### **3. Constraint Única no Banco de Dados**
```sql
CREATE UNIQUE INDEX idx_pix_transfer_webhook_exactly_once 
ON pix_transfer (end_to_end_id, status, last_status_update);
```
- **Previne**: Múltiplas atualizações de status para o mesmo evento
- **Garante**: Falha no banco se tentar processar evento duplicado

#### **4. Transações ACID Completas**
```java
@Transactional
public void processWebhookEvent(...) { /* operações atômicas */ }
```
- **Previne**: Inconsistências parciais (débito sem crédito)
- **Garante**: Rollback completo em caso de erro

#### **5. Validação Temporal de Eventos**
- **Previne**: Eventos mais antigos sobrescreverem eventos mais recentes
- **Garante**: Ordem cronológica correta dos status

**Resultado**: **Zero inconsistências** mesmo com:
- 🔥 Múltiplas instâncias da aplicação
- 🔥 Webhooks duplicados/fora de ordem
- 🔥 Falhas de rede/timeout
- 🔥 Operações simultâneas na mesma carteira

## 🔧 Configurações

### Profiles de Ambiente

A aplicação pode ser configurada para diferentes ambientes através de profiles:

```yaml
# application-dev.yml (desenvolvimento)
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

# application-prod.yml (produção)  
spring:
  jpa:
    show-sql: false
```

### Variáveis de Ambiente

```bash
# Banco de dados
DATABASE_URL=jdbc:postgresql://localhost:5432/pix_wallet
DATABASE_USERNAME=postgres  
DATABASE_PASSWORD=postgres

# Profile ativo
SPRING_PROFILES_ACTIVE=dev
```

## 📈 Melhorias Futuras

- [x] **Webhook PIX** para confirmação/rejeição de transferências ✅
- [ ] **Autenticação e Autorização** (Spring Security)
- [ ] **Rate Limiting** para APIs públicas
- [ ] **Auditoria** de operações
- [ ] **Notificações** em tempo real
- [ ] **Cache** Redis para consultas frequentes
- [ ] **Documentação** com OpenAPI/Swagger
- [ ] **Monitoramento** com Actuator
- [ ] **Containerização** completa com Docker
- [ ] **CI/CD** pipeline
- [ ] **Validação de CPF/CNPJ** para chaves PIX
