# AquaOrbital API

**FIAP Global Solution 2026/1** — Análise e Desenvolvimento de Sistemas

Plataforma IoT + IA para otimização do cultivo de microalgas com integração de dados orbitais provenientes de NASA POWER, Open-Meteo e INMET. A API gerencia fazendas biológicas, tanques e biofotorreatores, marketplace de biomassa e créditos de carbono com rastreabilidade por hash criptográfico.

---

## Sumário

- [Visão Geral](#visão-geral)
- [Tecnologias](#tecnologias)
- [Arquitetura do Projeto](#arquitetura-do-projeto)
- [Modelo de Dados](#modelo-de-dados)
- [Configuração e Execução](#configuração-e-execução)
- [Autenticação](#autenticação)
- [Endpoints da API](#endpoints-da-api)
- [Schedulers](#schedulers)
- [Tratamento de Erros](#tratamento-de-erros)
- [Documentação Swagger](#documentação-swagger)
- [Equipe](#equipe)

---

## Visão Geral

O AquaOrbital conecta dados ambientais reais ao ciclo de produção de microalgas. A plataforma coleta irradiância PAR, temperatura, nebulosidade e variáveis meteorológicas de três fontes distintas, alimentando modelos de decisão para otimização de cultivo. Além da camada de dados ambientais, a solução oferece gestão completa de fazendas, tanques, lotes de biomassa e créditos de carbono negociáveis.

Principais capacidades:

- Cadastro de fazendas georeferenciadas com múltiplos tanques e biofotorreatores
- Coleta automatizada de dados de três fontes orbitais e meteorológicas via schedulers agendados
- Marketplace para compra e venda de biomassa e créditos de carbono (com hash SHA-256 para auditoria)
- Dashboard por fazenda consolidando status operacional, dados ambientais e posição de créditos

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.4.5 |
| Segurança | Spring Security + JWT (jjwt 0.12.6) |
| Persistência | Spring Data JPA + Hibernate + Oracle Database |
| Pool de conexões | HikariCP |
| Documentação | SpringDoc OpenAPI 2.8.9 (Swagger UI) |
| Utilitários | Lombok, Jackson, Bean Validation |
| Build | Maven (Spring Boot Maven Plugin) |
| APIs Externas | NASA POWER, Open-Meteo Forecast, Open-Meteo Archive (ERA5), INMET |

---

## Arquitetura do Projeto

```
src/main/java/br/com/fiap/aquaorbital/
├── config/
│   ├── CorsConfig.java               # CORS permissivo para todas as origens
│   ├── OpenApiConfig.java            # Configuração Swagger com Bearer Auth
│   ├── RestTemplateConfig.java       # RestTemplate com timeouts (10s conexão / 30s leitura)
│   └── SecurityConfig.java           # Spring Security stateless + filtro JWT
├── controller/
│   ├── AuthController.java
│   ├── BrDadoOrbitalController.java
│   ├── DadoOrbitalController.java
│   ├── Era5ArchiveController.java
│   ├── FazendaController.java
│   ├── MarketplaceController.java
│   ├── OpenMeteoController.java
│   ├── PerfilController.java
│   ├── TanqueController.java
│   └── UsuarioController.java
├── dto/                              # Records de entrada e saída
├── entity/                           # Entidades JPA (Oracle, schema rm562085)
├── exception/
│   └── GlobalExceptionHandler.java   # Tratamento centralizado de erros
├── repository/                       # Interfaces Spring Data JPA
├── scheduler/
│   ├── InMetSyncScheduler.java       # Coleta INMET — 01:00 diário
│   ├── NasaSyncScheduler.java        # Coleta NASA POWER — 06:00 diário
│   └── OpenMeteoSyncScheduler.java   # Coleta Open-Meteo — a cada 15 minutos
├── security/
│   ├── JwtAuthFilter.java
│   └── JwtService.java
└── AquaOrbitalApplication.java
```

---

## Modelo de Dados

```
TB_PERFIL
    |
    | N:1
    |
TB_USUARIO ──────────────────── TB_FAZENDA ──────── TB_TANQUE
                                     |                   |
                                     |                   | 1:N
                                     | 1:N               |
                                     |               TB_LOTE_BIOMASSA
                               TB_DADO_ORBITAL            |
                                                          | 1:N
                                                          |
                                                   TB_CREDITO_CARBONO
                                                          |
                                                          | 1:N
                                                          |
                                               TB_TRANSACAO_MARKETPLACE
```

### Descrição das entidades

**TB_USUARIO** — Implementa `UserDetails` do Spring Security. O campo `status = 'A'` determina conta ativa. A role (`ROLE_`) é derivada dinamicamente do perfil associado.

**TB_PERFIL** — Perfis de acesso reutilizáveis (ex.: `ADMIN`, `PRODUTOR`). Criados via API antes do cadastro de usuários.

**TB_FAZENDA** — Unidade produtiva georeferenciada (latitude/longitude). Status padrão `ATIVA`. Vinculada a um usuário responsável.

**TB_TANQUE** — Biofotorreator individual. Armazena faixas operacionais de pH (0–14) e temperatura por espécie de alga. Status padrão `ATIVO`. O código do tanque é único no sistema.

**TB_DADO_ORBITAL** — Registro diário de dados ambientais por fazenda: irradiância PAR (`irradiancia_par`), nebulosidade e temperatura ambiente. O campo `fonte` identifica a origem: `NASA_POWER`, `OPEN_METEO` ou `ERA5_ARCHIVE`.

**TB_DADO_ORBITAL (BrDadoOrbital)** — Dados brutos das estações INMET: temperatura máx/mín/média, umidade relativa, precipitação, velocidade e direção do vento, pressão atmosférica e radiação global. Armazena o JSON original da API para auditoria.

**TB_LOTE_BIOMASSA** — Lote de biomassa colhido de um tanque específico. Ciclo de vida: `DISPONIVEL` → `VENDIDO`. Lotes vendidos não podem ser alterados ou removidos.

**TB_CREDITO_CARBONO** — Crédito de carbono vinculado a um lote. Hash SHA-256 gerado no momento da criação para rastreabilidade. Ciclo: `GERADO` → `VALIDADO` → `DISPONIVEL` → `VENDIDO`.

**TB_TRANSACAO_MARKETPLACE** — Registro de transação comercial. Tipos suportados: `COMPRA_BIOMASSA`, `COMPRA_CREDITO_CARBONO` e `VENDA_BIOMASSA`. Status padrão `PENDENTE`, confirmado em `CONFIRMADA`.

---

## Configuração e Execução

### Pré-requisitos

- Java 17 ou superior
- Maven 3.8 ou superior
- Acesso ao Oracle Database (ambiente FIAP) ou configuração de H2 para desenvolvimento local

### Propriedades de configuração

O arquivo `src/main/resources/application.properties` contém as seguintes configurações principais:

```properties
# Banco de dados Oracle
spring.datasource.url=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
spring.datasource.username=<rm_do_aluno>
spring.datasource.password=<senha>
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Pool HikariCP
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.connection-timeout=30000

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.default_schema=<rm_do_aluno>

# JWT
aquaorbital.jwt.secret=<chave-secreta-minimo-32-caracteres>
aquaorbital.jwt.expiration=86400000

# INMET
inmet.estacao.codigo=A701
inmet.estacao.janela-dias=1
inmet.estacao.cron=0 0 1 * * *
```

### Execução local

```bash
# Clonar o repositório
git clone https://github.com/<org>/gs1-java-devops.git
cd gs1-java-devops

# Executar via Maven Wrapper
./mvnw spring-boot:run

# Ou gerar e executar o JAR
./mvnw clean package -DskipTests
java -jar target/aquaorbital-0.0.1-SNAPSHOT.jar
```

A aplicação inicia na porta **8080** por padrão.

---

## Autenticação

A API utiliza autenticação stateless com **JWT Bearer Token**. O token é gerado no login, expira em 24 horas (configurável) e deve ser enviado no header de todas as requisições protegidas.

### Fluxo de autenticação

1. Garanta que o perfil desejado existe via `POST /api/perfis`
2. Registre o usuário via `POST /api/auth/register` informando o `nomePerfil`
3. Autentique via `POST /api/auth/login` para obter o token JWT
4. Inclua o header `Authorization: Bearer <token>` em todas as requisições protegidas

### Rotas públicas (não requerem autenticação)

```
POST  /api/auth/login
POST  /api/auth/register
GET   /swagger-ui/**
GET   /api-docs/**
GET   /v3/api-docs/**
```

Todas as demais rotas exigem token válido e não expirado.

---

## Endpoints da API

### Autenticação — `/api/auth`

| Método | Rota | Descrição | Autenticação |
|--------|------|-----------|:---:|
| POST | `/api/auth/login` | Login — retorna token JWT | Não |
| POST | `/api/auth/register` | Cadastro de novo usuário | Não |

**Requisição de login:**
```json
{
  "email": "usuario@email.com",
  "senha": "minhasenha"
}
```

**Resposta de login:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "email": "usuario@email.com",
  "perfil": "ADMIN"
}
```

**Requisição de registro:**
```json
{
  "nome": "João Silva",
  "email": "joao@email.com",
  "senha": "senha123",
  "telefone": "11999999999",
  "nomePerfil": "PRODUTOR"
}
```

---

### Usuários — `/api/usuarios`

> Requer autenticação JWT

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/usuarios` | Listar todos os usuários (paginado) |
| GET | `/api/usuarios/{id}` | Buscar usuário por ID |
| GET | `/api/usuarios/perfil/{idPerfil}` | Listar usuários por perfil (paginado) |
| PUT | `/api/usuarios/{id}` | Atualizar dados do usuário |
| DELETE | `/api/usuarios/{id}` | Remover usuário |

**Campos atualizáveis:** `nome`, `email`, `telefone`, `status`

---

### Perfis — `/api/perfis`

> Requer autenticação JWT

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/perfis` | Listar todos os perfis |
| GET | `/api/perfis/{id}` | Buscar perfil por ID |
| POST | `/api/perfis` | Criar novo perfil |

**Requisição de criação:**
```json
{
  "nomePerfil": "PRODUTOR",
  "descricao": "Produtor de microalgas"
}
```

---

### Fazendas — `/api/fazendas`

> Requer autenticação JWT — suporte a HATEOAS

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/fazendas` | Criar fazenda (vinculada ao usuário autenticado) |
| GET | `/api/fazendas` | Listar todas as fazendas (paginado, ordenado por nome) |
| GET | `/api/fazendas/{id}` | Buscar fazenda por ID |
| GET | `/api/fazendas/{id}/dashboard` | Dashboard consolidado da fazenda |
| PUT | `/api/fazendas/{id}` | Atualizar dados da fazenda |
| DELETE | `/api/fazendas/{id}` | Remover fazenda |

**Requisição de criação:**
```json
{
  "nome": "Fazenda Solar Norte",
  "cidade": "Fortaleza",
  "uf": "CE",
  "latitude": -3.717220,
  "longitude": -38.543360
}
```

**Resposta do dashboard** (`GET /api/fazendas/{id}/dashboard`):
```json
{
  "idFazenda": 1,
  "nomeFazenda": "Fazenda Solar Norte",
  "totalTanques": 5,
  "tanquesAtivos": 4,
  "lotesDisponiveis": 2,
  "creditosDisponiveis": 1,
  "totalCo2Toneladas": 12.5000,
  "ultimoDadoOrbital": {
    "id": 42,
    "fonte": "NASA_POWER",
    "dtColeta": "2026-05-28",
    "irradianciaParTot": 5.3210,
    "nebulosidade": 18.00,
    "temperaturaAmbiente": 29.40
  }
}
```

A resposta das rotas de criação e busca inclui links HATEOAS para `self`, `dashboard`, `tanques` e `fazendas`.

---

### Tanques — `/api/tanques`

> Requer autenticação JWT — suporte a HATEOAS

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/tanques` | Criar tanque |
| GET | `/api/tanques` | Listar todos os tanques (paginado) |
| GET | `/api/tanques/fazenda/{fazendaId}` | Listar tanques de uma fazenda |
| GET | `/api/tanques/{id}` | Buscar tanque por ID |
| PUT | `/api/tanques/{id}` | Atualizar tanque |
| DELETE | `/api/tanques/{id}` | Remover tanque |

**Requisição de criação:**
```json
{
  "idFazenda": 1,
  "codigoTanque": "TQ-001",
  "tipoAlga": "Spirulina platensis",
  "capacidadeLitros": 5000.00,
  "phMin": 8.5,
  "phMax": 10.5,
  "temperaturaMin": 25.0,
  "temperaturaMax": 35.0,
  "dtInstalacao": "2025-01-15"
}
```

O `codigoTanque` deve ser único no sistema. Tentativas de duplicação retornam `409 Conflict`.

---

### Dados Orbitais — NASA POWER — `/api/dados-orbitais`

> Requer autenticação JWT

Integração com a [NASA POWER API](https://power.larc.nasa.gov/). Consulta parâmetros `ALLSKY_SFC_SW_DWN` (irradiância solar), `T2M` (temperatura) e `CLOUD_AMT` (nebulosidade) para as coordenadas da fazenda. A janela de coleta cobre os últimos 13 dias com lag de 5 dias (limitação da NASA). A operação é **idempotente**: dias já existentes são ignorados.

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/dados-orbitais/fazenda/{fazendaId}/sincronizar` | Sincronizar dados NASA para a fazenda |
| GET | `/api/dados-orbitais/fazenda/{fazendaId}` | Listar todos os dados orbitais da fazenda |
| GET | `/api/dados-orbitais/fazenda/{fazendaId}/ultimo` | Dado orbital mais recente |
| GET | `/api/dados-orbitais/fazenda/{fazendaId}/periodo` | Filtrar por período (`?inicio=yyyy-MM-dd&fim=yyyy-MM-dd`) |

A fazenda deve ter coordenadas cadastradas (`latitude` e `longitude`). Caso contrário, a API retorna `400 Bad Request`.

---

### Dados Orbitais — Open-Meteo — `/api/dados-orbitais/open-meteo`

> Requer autenticação JWT

Integração em tempo real com a [Open-Meteo Forecast API](https://open-meteo.com/). Coleta `shortwave_radiation`, `cloud_cover`, `temperature_2m` e `relative_humidity_2m` para os próximos 2 dias. Realiza **upsert**: atualiza registros `OPEN_METEO` já existentes para a data; preserva registros de outras fontes sem modificação.

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/dados-orbitais/open-meteo/fazenda/{fazendaId}/sincronizar` | Sincronizar previsão Open-Meteo para a fazenda |

---

### Dados Orbitais — ERA5 Archive — `/api/dados-orbitais/era5`

> Requer autenticação JWT

Carga histórica via [Open-Meteo Archive API](https://archive-api.open-meteo.com/) com dados ERA5 (reanálise climática). Período padrão: 2020–2024. Destinada à população inicial do banco de dados para treinamento de modelos de IA. Operação idempotente — dias já existentes são ignorados. Para bases grandes, recomenda-se executar fora do horário de pico ou processar um ano por vez via endpoint de período customizado.

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/dados-orbitais/era5/fazenda/{fazendaId}/carregar` | Carga do período padrão (2020–2024) |
| POST | `/api/dados-orbitais/era5/fazenda/{fazendaId}/carregar/periodo` | Período customizado (`?startDate=yyyy-MM-dd&endDate=yyyy-MM-dd`) |
| POST | `/api/dados-orbitais/era5/carregar-todas` | Carga para todas as fazendas cadastradas |

**Resposta:**
```json
{
  "fazendaId": 1,
  "diasInseridos": 1826,
  "fonte": "ERA5_ARCHIVE"
}
```

---

### Dados Orbitais — INMET — `/api/dados-orbitais/inmet`

> Requer autenticação JWT

Dados das estações meteorológicas automáticas brasileiras via [API INMET](https://apitempo.inmet.gov.br/). A sincronização automática ocorre diariamente às 01:00. A sincronização manual permite forçar a coleta para uma estação e data específicas. Registros duplicados (mesma estação, data e hora) são ignorados.

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/dados-orbitais/inmet` | Listar todos os registros INMET |
| GET | `/api/dados-orbitais/inmet/{id}` | Buscar registro por ID |
| GET | `/api/dados-orbitais/inmet/estacao/{codEstacao}` | Listar registros de uma estação |
| GET | `/api/dados-orbitais/inmet/estacao/{codEstacao}/ultimo` | Registro mais recente da estação |
| GET | `/api/dados-orbitais/inmet/estacao/{codEstacao}/periodo` | Filtrar por período (`?inicio=yyyy-MM-dd&fim=yyyy-MM-dd`) |
| POST | `/api/dados-orbitais/inmet/sincronizar` | Sincronização manual (`?codEstacao=A701&data=yyyy-MM-dd`) |
| DELETE | `/api/dados-orbitais/inmet/{id}` | Remover registro por ID |

---

### Marketplace — `/api/marketplace`

> Requer autenticação JWT

#### Lotes de Biomassa

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/marketplace/lotes` | Publicar novo lote de biomassa |
| GET | `/api/marketplace/lotes` | Listar todos os lotes (paginado) |
| GET | `/api/marketplace/lotes/disponiveis` | Listar lotes disponíveis para compra |
| GET | `/api/marketplace/lotes/{id}` | Buscar lote por ID |
| PATCH | `/api/marketplace/lotes/{id}/status` | Atualizar status do lote |
| DELETE | `/api/marketplace/lotes/{id}` | Remover lote (lotes com status `VENDIDO` não podem ser removidos) |

**Requisição de criação de lote:**
```json
{
  "idFazenda": 1,
  "idTanque": 2,
  "taxonomiaAlga": "Chlorella vulgaris",
  "pesoKg": 150.500,
  "precoUnitario": 45.00
}
```

#### Créditos de Carbono

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/marketplace/creditos` | Listar todos os créditos de carbono (paginado) |
| GET | `/api/marketplace/creditos/fazenda/{fazendaId}` | Extrato de CO2 por fazenda |
| GET | `/api/marketplace/creditos/{id}` | Buscar crédito por ID |
| PATCH | `/api/marketplace/creditos/{id}/validar` | Validar crédito (status: `GERADO` -> `VALIDADO`) |

#### Transacoes

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/marketplace/transacoes` | Realizar transação de compra |
| GET | `/api/marketplace/transacoes` | Listar todas as transações (paginado) |
| GET | `/api/marketplace/transacoes/minhas` | Listar transações do usuário autenticado |

**Tipos de transação aceitos:** `COMPRA_BIOMASSA`, `COMPRA_CREDITO_CARBONO`, `VENDA_BIOMASSA`

Uma transação do tipo `COMPRA_BIOMASSA` altera automaticamente o status do lote para `VENDIDO`. Uma transação do tipo `COMPRA_CREDITO_CARBONO` altera o status do crédito para `VENDIDO`. Apenas itens com status `DISPONIVEL` podem ser transacionados.

**Requisição de transação:**
```json
{
  "idLote": 3,
  "tipoTransacao": "COMPRA_BIOMASSA",
  "quantidade": 50.000,
  "valorTotal": 2250.00
}
```

---

## Schedulers

Os três schedulers executam de forma independente. Uma falha em uma fazenda não interrompe o processamento das demais — erros são registrados em log e a execução continua.

| Scheduler | Agendamento | Descricao |
|-----------|-------------|-----------|
| `NasaSyncScheduler` | `0 0 6 * * *` — 06:00 diário | Percorre todas as fazendas e sincroniza dados NASA POWER para cada uma |
| `OpenMeteoSyncScheduler` | `0 0/15 * * * *` — a cada 15 minutos | Sincroniza previsão Open-Meteo em tempo real para todas as fazendas |
| `InMetSyncScheduler` | Configurável via `inmet.estacao.cron` (padrão: `0 0 1 * * *`) | Coleta dados da estação INMET definida em `inmet.estacao.codigo` para a janela de dias configurada |

Todos os schedulers podem ser acionados manualmente via endpoints dedicados para fins de desenvolvimento e operação.

---

## Tratamento de Erros

Todos os erros são tratados de forma centralizada pelo `GlobalExceptionHandler` e retornam um corpo padronizado:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Fazenda não encontrada: 99",
  "timestamp": "2026-06-03T10:30:00"
}
```

Erros de validação retornam os campos com problemas:

```json
{
  "status": 400,
  "error": "Validation Error",
  "fields": {
    "email": "Email inválido",
    "senha": "Senha deve ter no mínimo 6 caracteres"
  },
  "timestamp": "2026-06-03T10:30:00"
}
```

| Situação | HTTP |
|----------|------|
| Entidade não encontrada | 404 Not Found |
| Falha de validação de campos | 400 Bad Request |
| Credenciais inválidas | 401 Unauthorized |
| Conflito (ex.: email ou código de tanque duplicado) | 409 Conflict |
| Erro interno do servidor | 500 Internal Server Error |

---

## Documentação Swagger

Com a aplicação em execução, a documentação interativa está disponível em:

```
http://localhost:8080/swagger-ui.html
```

Para autenticar no Swagger UI, clique em **Authorize**, insira `Bearer <seu-token>` no campo `bearerAuth` e confirme.

A especificação OpenAPI em formato JSON está disponível em:

```
http://localhost:8080/api-docs
```

---

## Equipe

Equipe AquaOrbital — FIAP Global Solution 2026/1

aquaorbital@fiap.com.br
