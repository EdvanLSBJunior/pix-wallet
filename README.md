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

5. **Simular confirmação via webhook (opcional)**

Para testar o webhook, você precisa enviar uma requisição com os seguintes campos obrigatórios:

```bash
# Confirmar a transferência
curl -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d '{
    "endToEndId": "E2E-123e4567-e89b-12d3-a456-426614174000",
    "status": "CONFIRMED",
    "timestamp": "2024-01-01T10:01:00.000Z"
  }'

# Ou rejeitar a transferência (será revertida automaticamente)
curl -X POST http://localhost:8080/pix/webhook/events \
  -H "Content-Type: application/json" \
  -d '{
    "endToEndId": "E2E-123e4567-e89b-12d3-a456-426614174000",
    "status": "REJECTED", 
    "timestamp": "2024-01-01T10:02:00.000Z"
  }'
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
- ✅ **Idempotência**: Eventos duplicados são ignorados
- ✅ **Ordenação temporal**: Eventos antigos não sobrescrevem recentes
- ✅ **Estados finais**: CONFIRMED/REJECTED não podem ser alterados
- ✅ **Reversão automática**: Transferências rejeitadas são estornadas

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
