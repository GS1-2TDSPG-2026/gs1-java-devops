# Phycocarbon API

> **Global Solution FIAP 2026/1 — Análise e Desenvolvimento de Sistemas**

Plataforma **IoT + IA** para otimização do cultivo de microalgas com integração de dados orbitais provenientes de **NASA POWER**, **Open-Meteo** e **INMET**. A API gerencia fazendas biológicas, tanques, biofotorreatores, marketplace de biomassa e créditos de carbono com rastreabilidade por hash criptográfico.

---

## 👥 Equipe

| Nome | RM | Turma |
|---|---|---|
| Alexander Dennis Isidro | RM565554 | 2TDSPG |
| Arthur Brito da Silva | RM562085 | 2TDSPG |
| Kelson Zhang | RM563748 | 2TDSPG |
| Luiz Felipe Flosi | RM563197 | 2TDSPG |
| Pedro Henrique Brum Lopes | RM561780 | 2TDSPG |

---

## 🔗 Links

| Recurso | URL |
|---|---|
| API (Azure) | `http://<IP_PUBLICO_AZURE>:8080` |
| Swagger UI | `http://<IP_PUBLICO_AZURE>:8080/swagger-ui.html` |
| OpenAPI JSON | `http://<IP_PUBLICO_AZURE>:8080/api-docs` |
| Vídeo Demo | *(em breve)* |
| Pitch (3 min) | *(em breve)* |

---

## 🏗️ Arquitetura Macro

```
┌─────────────────────────────────────────────────────────────────┐
│                     AZURE VIRTUAL MACHINE                        │
│                   Ubuntu 22.04 LTS — B2s                         │
│                   IP Público Estático                            │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                   Docker Engine                           │   │
│  │                   Rede: phyconet (bridge)                 │   │
│  │                                                           │   │
│  │  ┌─────────────────────┐    ┌──────────────────────────┐ │   │
│  │  │   rm563197-app      │    │      rm563197-db          │ │   │
│  │  │  Spring Boot 3.4.5  │───►│    PostgreSQL 16          │ │   │
│  │  │  Java 17 (JRE)      │    │    porta: 5432            │ │   │
│  │  │  porta: 8080        │    │    volume: rm563197-pgdata │ │   │
│  │  │  user: phycouser    │    │                           │ │   │
│  │  │  /app/phycocarbon   │    │    Schema: rm562085       │ │   │
│  │  └─────────────────────┘    └──────────────────────────┘ │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
│  NSG: porta 8080 aberta ao público                               │
│       porta 22 aberta para SSH                                   │
└─────────────────────────────────────────────────────────────────┘
         ▲
         │  HTTP :8080
         │
┌────────┴──────────┐        ┌──────────────────────────┐
│  Usuário / Mobile │        │   APIs Externas           │
│  Swagger UI       │        │   NASA POWER              │
└───────────────────┘        │   Open-Meteo              │
                             │   INMET                   │
                             └──────────────────────────┘
```

**Fluxo de dados:**
1. Usuário autentica via `POST /api/auth/login` → recebe JWT
2. Requisições autenticadas chegam na API Spring Boot (porta 8080)
3. API persiste/consulta dados no PostgreSQL (rede interna `phyconet`)
4. Schedulers coletam dados das APIs externas automaticamente
5. Dados ficam persistidos no volume `rm563197-pgdata`

---

## 📦 Stack Tecnológico

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.4.5 |
| Segurança | Spring Security + JWT (jjwt 0.12.6) |
| Persistência | Spring Data JPA + Hibernate + PostgreSQL 16 |
| Documentação | SpringDoc OpenAPI 2.8.9 (Swagger UI) |
| Containers | Docker + Docker Compose |
| Cloud | Azure Virtual Machine (Ubuntu 22.04) |
| APIs externas | NASA POWER, Open-Meteo, INMET |

---

## 🗄️ Modelo de Dados

```
TB_PERFIL (1) ──────── (N) TB_USUARIO (1) ──── (N) TB_FAZENDA (1) ── (N) TB_TANQUE
                                                         │                     │
                                                    (N)  │               (N)   │
                                                         ▼                     ▼
                                                  TB_DADO_ORBITAL      TB_LOTE_BIOMASSA (1)
                                                                               │
                                                                          (N)  │
                                                                               ▼
                                                                     TB_CREDITO_CARBONO (1)
                                                                               │
                                                                          (N)  │
                                                                               ▼
                                                                  TB_TRANSACAO_MARKETPLACE
```

> Todas as tabelas estão no schema `rm562085` dentro do banco `phycocarbon`.

---

## 🚀 How To — Do Clone ao Deploy em Nuvem

### Pré-requisitos locais

- Git
- Docker Desktop (Windows/Mac) **ou** Docker Engine (Linux)
- Conta Azure com créditos ativos

---

### PARTE 1 — Execução Local (desenvolvimento)

#### 1. Clonar o repositório

```bash
git clone https://github.com/<seu-usuario>/gs1-java-devops.git
cd gs1-java-devops
```

#### 2. Subir os containers

```bash
# Entrar na pasta docker
cd docker

# Subir em background (modo detached)
docker compose up -d --build
```

> O build do JAR ocorre dentro do próprio container (multi-stage). Aguarde ~3 minutos na primeira execução.

#### 3. Verificar se os containers estão rodando

```bash
docker compose ps
```

Saída esperada:

```
NAME             STATUS          PORTS
rm563197-db      Up (healthy)    0.0.0.0:5432->5432/tcp
rm563197-app     Up (healthy)    0.0.0.0:8080->8080/tcp
```

#### 4. Visualizar logs

```bash
# Logs do banco
docker logs rm563197-db

# Logs da aplicação
docker logs rm563197-app

# Logs em tempo real (follow)
docker logs -f rm563197-app
```

#### 5. Verificar estrutura interna dos containers

```bash
# Container da aplicação
docker container exec rm563197-app whoami
docker container exec rm563197-app pwd
docker container exec rm563197-app ls -la

# Container do banco
docker container exec rm563197-db whoami
docker container exec rm563197-db pwd
docker container exec rm563197-db ls -la /var/lib/postgresql/data
```

#### 6. Acessar o banco e verificar persistência

```bash
# Conectar diretamente no container do PostgreSQL
docker container exec -it rm563197-db psql -U phycouser -d phycocarbon

# Dentro do psql — verificar tabelas criadas:
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'rm562085'
ORDER BY table_name;

# Verificar perfis inseridos:
SELECT * FROM rm562085.tb_perfil;

# Verificar usuários (após criar via API):
SELECT id_usuario, nome, email, status FROM rm562085.tb_usuario;

# Verificar fazendas:
SELECT * FROM rm562085.tb_fazenda;

# Sair do psql:
\q
```

#### 7. Testar a API

Acesse o Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

Fluxo de teste completo:

```bash
# 1) Criar perfil
curl -X POST http://localhost:8080/api/perfis \
  -H "Content-Type: application/json" \
  -d '{"nomePerfil":"ADMIN","descricao":"Administrador"}'

# 2) Registrar usuário
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teste","email":"teste@fiap.com","senha":"senha123","nomePerfil":"ADMIN"}'

# 3) Login — guardar o token retornado
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teste@fiap.com","senha":"senha123"}'

# 4) Criar fazenda (substituir <TOKEN> pelo JWT recebido)
curl -X POST http://localhost:8080/api/fazendas \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"nome":"Fazenda Teste","cidade":"São Paulo","uf":"SP","latitude":-23.5505,"longitude":-46.6333}'
```

#### 8. Parar os containers

```bash
docker compose down

# Para remover também o volume (APAGA OS DADOS):
docker compose down -v
```

---

### PARTE 2 — Deploy na Azure VM

#### Passo 1 — Criar a VM na Azure (via CLI)

```bash
# Login na Azure
az login

# Criar Resource Group
az group create \
  --name rg-phycocarbon \
  --location brazilsouth

# Criar IP público ESTÁTICO (obrigatório — não muda entre reinicializações)
az network public-ip create \
  --resource-group rg-phycocarbon \
  --name ip-phycocarbon \
  --allocation-method Static \
  --sku Standard

# Criar a VM (Ubuntu 22.04, B2s — 2 vCPUs, 4GB RAM)
az vm create \
  --resource-group rg-phycocarbon \
  --name vm-phycocarbon \
  --image Ubuntu2204 \
  --size Standard_B2s \
  --admin-username azureuser \
  --generate-ssh-keys \
  --public-ip-address ip-phycocarbon \
  --public-ip-sku Standard

# Verificar o IP público gerado
az network public-ip show \
  --resource-group rg-phycocarbon \
  --name ip-phycocarbon \
  --query ipAddress \
  --output tsv
```

> 💡 Guarde o IP retornado — é o endereço público da sua API.

#### Passo 2 — Abrir as portas no NSG

```bash
# Porta 8080 — API Spring Boot
az vm open-port \
  --resource-group rg-phycocarbon \
  --name vm-phycocarbon \
  --port 8080 \
  --priority 1001

# Porta 22 já vem aberta por padrão (SSH)
```

#### Passo 3 — Conectar na VM via SSH

```bash
# Substituir <IP_PUBLICO> pelo IP obtido no Passo 1
ssh azureuser@<IP_PUBLICO>
```

#### Passo 4 — Provisionar a VM (instalar Docker)

```bash
# Dentro da VM — executar o script de provisionamento
curl -fsSL https://raw.githubusercontent.com/<seu-usuario>/gs1-java-devops/main/provision.sh | sudo bash

# Aplicar grupo docker sem fazer logout
newgrp docker
```

**Ou manualmente:**

```bash
sudo apt-get update -y
sudo apt-get install -y ca-certificates curl gnupg git

sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

sudo usermod -aG docker azureuser
newgrp docker
```

#### Passo 5 — Clonar o repositório na VM

```bash
git clone https://github.com/<seu-usuario>/gs1-java-devops.git
cd gs1-java-devops
```

#### Passo 6 — Subir os containers na VM

```bash
cd docker
docker compose up -d --build
```

> O build pode levar 5–10 minutos na primeira vez (download das imagens + build Maven).

#### Passo 7 — Monitorar a inicialização

```bash
# Acompanhar logs em tempo real
docker logs -f rm563197-app
```

Aguarde a linha:
```
Started PhycoCarbonApplication in X.XXX seconds
```

#### Passo 8 — Validar o deploy em nuvem

```bash
# Verificar containers rodando
docker compose ps

# Verificar logs dos dois containers
docker logs rm563197-db
docker logs rm563197-app

# Verificar estrutura e usuário nos containers
docker container exec rm563197-app whoami   # deve retornar: phycouser
docker container exec rm563197-app pwd      # deve retornar: /app/phycocarbon
docker container exec rm563197-app ls -la

docker container exec rm563197-db whoami    # deve retornar: postgres
docker container exec rm563197-db pwd
docker container exec rm563197-db ls -la /var/lib/postgresql/data

# Verificar persistência no banco
docker container exec -it rm563197-db psql -U phycouser -d phycocarbon \
  -c "SELECT table_name FROM information_schema.tables WHERE table_schema='rm562085';"

docker container exec -it rm563197-db psql -U phycouser -d phycocarbon \
  -c "SELECT * FROM rm562085.tb_perfil;"

# Testar a API publicamente
curl http://<IP_PUBLICO>:8080/api-docs | head -5
```

#### Passo 9 — Acessar o Swagger UI em nuvem

```
http://<IP_PUBLICO>:8080/swagger-ui.html
```

---

### PARTE 3 — Gerenciamento de custos (importante para créditos estudantis)

```bash
# DESALOCAR a VM quando não estiver em uso (para o billing de compute)
# O IP estático continua reservado (~R$15/mês), mas a VM não cobra vCPU/RAM
az vm deallocate \
  --resource-group rg-phycocarbon \
  --name vm-phycocarbon

# Religar a VM antes da apresentação
az vm start \
  --resource-group rg-phycocarbon \
  --name vm-phycocarbon

# Verificar status
az vm show \
  --resource-group rg-phycocarbon \
  --name vm-phycocarbon \
  --show-details \
  --query powerState
```

> ⚠️ Use `deallocate` — nunca `stop`. O comando `stop` mantém o billing de compute ativo.

---

## 🔐 Autenticação

Fluxo JWT:

1. `POST /api/perfis` — criar perfil (ex.: `ADMIN`)
2. `POST /api/auth/register` — registrar usuário
3. `POST /api/auth/login` — obter token JWT
4. Incluir header `Authorization: Bearer <token>` em todas as demais rotas

### Rotas públicas

```
POST /api/auth/login
POST /api/auth/register
GET  /swagger-ui/**
GET  /api-docs/**
```

---

## 📋 Endpoints — Resumo

| Módulo | Rotas | CRUD |
|---|---|---|
| Auth | `/api/auth/login`, `/api/auth/register` | C |
| Perfis | `/api/perfis` | CR |
| Usuários | `/api/usuarios` | CRUD |
| Fazendas | `/api/fazendas` | CRUD |
| Tanques | `/api/tanques` | CRUD |
| Dados NASA | `/api/dados-orbitais/fazenda/{id}` | CR |
| Dados Open-Meteo | `/api/dados-orbitais/open-meteo/fazenda/{id}` | C |
| Dados ERA5 | `/api/dados-orbitais/era5/fazenda/{id}` | C |
| Dados INMET | `/api/dados-orbitais/inmet` | CRD |
| Marketplace Lotes | `/api/marketplace/lotes` | CRUD |
| Marketplace Créditos | `/api/marketplace/creditos` | CR |
| Transações | `/api/marketplace/transacoes` | CR |

---

## ⚠️ Tratamento de Erros

| HTTP | Situação |
|---|---|
| 400 | Validação de campos |
| 401 | Token inválido/ausente |
| 404 | Entidade não encontrada |
| 409 | Conflito (email/código duplicado) |
| 500 | Erro interno |

---

## 🗂️ Estrutura do Repositório

```
gs1-java-devops/
├── docker/
│   ├── Dockerfile              # Imagem personalizada da API
│   ├── docker-compose.yml      # Orquestração dos 2 containers
│   └── sql/
│       └── init.sql            # DDL + dados iniciais (PostgreSQL)
├── src/                        # Código-fonte Java
│   └── main/resources/
│       ├── application.properties          # Profile padrão (Oracle FIAP)
│       └── application-docker.properties   # Profile Docker (PostgreSQL)
├── provision.sh                # Script de provisionamento da VM Azure
├── pom.xml
└── README.md
```

---

## 🔄 Agendadores Automáticos

| Scheduler | Horário | Fonte |
|---|---|---|
| NasaSyncScheduler | 06:00 diário | NASA POWER |
| OpenMeteoSyncScheduler | A cada 15 min | Open-Meteo |
| InMetSyncScheduler | 01:00 diário | INMET |
