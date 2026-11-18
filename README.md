# Sistema Bancário — API REST

API REST que simula o núcleo de um banco digital, desenvolvida com **Java e Spring Boot**. O sistema cobre clientes, contas correntes com cheque especial, depósitos, saques, transferências, extrato e empréstimos pessoais, com todos os valores em dinheiro tratados com `BigDecimal`.

O principal objetivo do projeto foi praticar **regras de negócio que envolvem dinheiro**: porcentagens, juros, taxas, quitação parcial e operações que precisam ser atômicas.

## Tecnologias

- **Java 21**
- **Spring Boot 4** (Spring Web MVC, Spring Data JPA)
- **Bean Validation** (Jakarta Validation) para validar as requisições
- **MySQL** como banco de dados relacional
- **MapStruct** para conversão entre entidades e DTOs
- **Lombok** para reduzir código repetitivo
- **Maven** para build e dependências

## Estrutura do Projeto

- `controller`: controllers REST que expõem os endpoints HTTP
- `service`: regras de negócio e controle de transações (`@Transactional`)
- `domain`: entidades JPA e enums. `Account` e `Loan` concentram as próprias regras (modelo de domínio rico)
- `repository`: repositórios do Spring Data JPA
- `dto`: records de requisição e resposta, com as anotações de validação
- `Mapper`: interfaces do MapStruct que convertem entidades em DTOs
- `exception`: exceções personalizadas para cada tipo de erro
- `handler`: tratamento global de exceções, com respostas padronizadas (`ProblemDetail`)
- `config`: configuração web (paginação)

## Modelo de Domínio

| Entidade | Principais campos |
| --- | --- |
| **Client** (cliente) | nome, CPF (único, 11 dígitos), e-mail (único), renda mensal |
| **Account** (conta) | número (único, 6 dígitos), saldo, limite do cheque especial, valor usado do cheque, status, cliente |
| **Extract** (extrato) | tipo, valor, data/hora, conta |
| **Loan** (empréstimo) | valor solicitado, taxa de juros mensal, parcelas, valor total, valor da parcela, parcelas pagas, status, conta |

- Status da conta: `ACTIVE` (ativa), `BLOCKED` (bloqueada), `CLOSED` (encerrada)
- Status do empréstimo: `ACTIVE` (ativo), `FINISHED` (quitado)
- Tipos de movimentação: `DEPOSIT` (depósito), `WITHDRAWAL` (saque), `TRANSFER_SENT` (transferência enviada), `TRANSFER_RECEIVED` (transferência recebida), `LOAN_CREDIT` (crédito de empréstimo), `LOAN_PAYMENT` (pagamento de parcela), `RATE` (taxa)

Toda operação que altera o saldo gera uma movimentação no extrato. As movimentações nunca são editadas nem apagadas.

## Regras de Negócio

### Conta

- O número da conta é gerado automaticamente: 6 dígitos aleatórios, com garantia de que não se repete.
- Cada cliente pode ter uma conta.
- O **limite do cheque especial** é definido na abertura da conta: **20% da renda mensal** do cliente.
- Uma conta `BLOCKED` ou `CLOSED` não realiza nenhuma operação.
- A conta só pode ser encerrada com saldo zero, sem cheque especial em uso e sem empréstimo ativo.

### Cheque especial

- Saque e transferência podem usar **saldo + cheque especial disponível**.
- O saldo nunca fica negativo. Quando o saldo não é suficiente, a diferença é registrada como valor usado do cheque.
- O próximo depósito primeiro quita o cheque usado **mais 10% de taxa**, e só o que sobrar entra no saldo. A taxa gera uma movimentação do tipo `RATE`.
- A quitação parcial é permitida: cada R$ 1,10 depositado quita R$ 1,00 do cheque e R$ 0,10 de taxa.

**Exemplo**: renda mensal de R$ 3.000 → limite do cheque especial de R$ 600.

| Operação | Saldo | Cheque usado | Extrato |
| --- | --- | --- | --- |
| Depósito de R$ 100 | 100,00 | 0,00 | DEPOSIT 100,00 |
| Saque de R$ 300 | 0,00 | 200,00 | WITHDRAWAL 300,00 |
| Depósito de R$ 110 (quitação parcial) | 0,00 | 100,00 | DEPOSIT 110,00 e RATE 10,00 |
| Depósito de R$ 200 (quita tudo) | 90,00 | 0,00 | DEPOSIT 200,00 e RATE 10,00 |

### Transferência

- A transferência é feita pelo número da conta.
- Não é possível transferir para a mesma conta.
- Gera duas movimentações: `TRANSFER_SENT` na conta de origem e `TRANSFER_RECEIVED` na conta de destino. Se o destino estiver usando o cheque especial, o valor recebido quita o cheque primeiro, seguindo a mesma regra do depósito.
- A operação inteira roda em uma única transação: se qualquer etapa falhar, nada é salvo.

### Empréstimo

- Valor máximo: **10 vezes** a renda mensal do cliente.
- Parcelas: de **1 a 24**.
- Taxa de juros mensal (juros simples), conforme a quantidade de parcelas:

| Parcelas | Taxa mensal |
| --- | --- |
| 1 a 6 | 2% |
| 7 a 12 | 3% |
| 13 a 24 | 4% |

- **Valor total** = valor solicitado + (valor solicitado × taxa × parcelas)
- **Valor da parcela** = valor total ÷ parcelas
- A parcela não pode passar de **30% da renda mensal**. Se passar, o empréstimo é recusado.
- Só é permitido **um empréstimo ativo** por conta.
- Quando aprovado, o valor solicitado é creditado na conta (`LOAN_CREDIT`).
- Pagar uma parcela desconta o valor da conta (`LOAN_PAYMENT`). Ao pagar a última, o empréstimo passa para `FINISHED`.
- A **simulação** aplica exatamente as mesmas regras e cálculos da contratação, mas não salva nada.

**Exemplo**: R$ 1.000 em 10 parcelas → taxa de 3% → total de R$ 1.300,00 → parcela de R$ 130,00.

## Endpoints

### Clientes

| Método | Endpoint | Descrição |
| --- | --- | --- |
| POST | `/clients` | Cadastra um cliente |
| GET | `/clients/{id}` | Busca um cliente pelo id |

### Contas

| Método | Endpoint | Descrição |
| --- | --- | --- |
| POST | `/accounts/{clientId}` | Abre uma conta para o cliente |
| GET | `/accounts/{id}` | Mostra o saldo, o cheque disponível e se o cheque está em uso |
| POST | `/accounts/{id}/deposit` | Deposita |
| POST | `/accounts/{id}/withdrawal` | Saca |
| POST | `/accounts/transfers` | Transfere entre contas |
| GET | `/accounts/{id}/extracts` | Extrato paginado (mais recente primeiro) |
| PATCH | `/accounts/{id}/block` | Bloqueia a conta |
| PATCH | `/accounts/{id}/cancel` | Encerra a conta |

### Empréstimos

| Método | Endpoint | Descrição |
| --- | --- | --- |
| POST | `/accounts/{id}/loans/simulation` | Simula um empréstimo sem contratar |
| POST | `/accounts/{id}/loans` | Contrata um empréstimo |
| PATCH | `/accounts/{id}/loans/payinstallment` | Paga a próxima parcela do empréstimo ativo |

## Exemplos de Requisição

**Cadastrar cliente** — `POST /clients`

```json
{
  "name": "Maria Silva",
  "cpf": "12345678901",
  "email": "maria@email.com",
  "monthlyIncome": 3000.00
}
```

**Depositar ou sacar** — `POST /accounts/1/deposit`

```json
{
  "value": 150.00
}
```

**Transferir** — `POST /accounts/transfers`

```json
{
  "originAccountNumber": "482913",
  "destinationAccountNumber": "731045",
  "value": 200.00
}
```

**Simular ou contratar empréstimo** — `POST /accounts/1/loans/simulation`

```json
{
  "requestAmount": 1000.00,
  "numberInstallments": 10
}
```

Resposta:

```json
{
  "requestAmount": 1000.00,
  "interestRate": 0.03,
  "numberInstallments": 10,
  "totalValue": 1300.0000,
  "installmentAmount": 130.00
}
```

## Paginação

O endpoint de extrato aceita:

| Parâmetro | Descrição | Exemplo |
| --- | --- | --- |
| `page` | Número da página (começa em 1) | `page=1` |
| `size` | Itens por página (padrão e máximo: 10) | `size=5` |

Exemplo: `GET /accounts/1/extracts?page=1&size=5`

## Tratamento de Erros

Todos os erros seguem o mesmo formato (`ProblemDetail`, RFC 9457), com um `timestamp`:

| Status | Quando |
| --- | --- |
| `400 Bad Request` | Campos inválidos ou valores inválidos |
| `404 Not Found` | Cliente, conta ou empréstimo ativo não encontrado |
| `409 Conflict` | CPF ou e-mail já cadastrado, ou violação de integridade no banco |
| `422 Unprocessable Content` | Regra de negócio violada (saldo insuficiente, conta bloqueada, empréstimo recusado...) |
| `500 Internal Server Error` | Erro inesperado (os detalhes vão para o log e não são expostos) |

Exemplo:

```json
{
  "type": "about:blank",
  "title": "Regra de negocio violada.",
  "status": 422,
  "detail": "Saldo insuficiente.",
  "instance": "/accounts/1/withdrawal",
  "timestamp": "2026-11-17T14:30:00Z"
}
```

## Como Executar

### Pré-requisitos

- Java 21+
- Maven
- MySQL rodando (local ou Docker)

### Configuração

O banco de dados e as tabelas são criados automaticamente ao iniciar a aplicação (`ddl-auto: update` + `createDatabaseIfNotExist=true`).

As configurações de conexão ficam em `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sistemaBancario?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: ${DB_PASSWORD}
```

Antes de rodar a aplicação, crie a variável de ambiente `DB_PASSWORD` com a senha do seu MySQL local.

### Rodando a aplicação

```bash
mvn spring-boot:run
```

A API fica disponível em `http://localhost:8080`.

## Próximos Passos

- Testes unitários das regras de conta e empréstimo (JUnit + Mockito)
- Documentação da API com Swagger / OpenAPI
- Várias contas por cliente
- Autenticação e autorização (Spring Security)
- Docker Compose para subir a aplicação e o banco juntos
