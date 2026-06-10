# 🌿 Phycocarbon API

> **Global Solution FIAP 2026/1 — Análise e Desenvolvimento de Sistemas**

Plataforma **IoT + IA** para otimização do cultivo de microalgas com integração de dados orbitais provenientes de **NASA POWER**, **Open-Meteo** e **INMET**. A API gerencia fazendas biológicas, tanques, biofotorreatores, marketplace de biomassa e créditos de carbono com rastreabilidade por hash criptográfico.

> 🌍 **Conexão com a Economia Espacial:** o projeto consome dados satelitais da NASA (radiação solar, temperatura, umidade) para otimizar em tempo real as condições de cultivo de microalgas — organismos capazes de sequestrar CO₂ até 10x mais eficientemente que plantas terrestres. Créditos de carbono gerados são tokenizados com hash criptográfico e comercializados via marketplace integrado.

[![Deploy](https://img.shields.io/badge/API-Online%20Azure-blue)](http://57.156.65.216:8080)
[![Swagger](https://img.shields.io/badge/Docs-Swagger%20UI-green)](http://57.156.65.216:8080/swagger-ui.html)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen)](https://spring.io/projects/spring-boot)

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
| 🚀 API em produção (Azure) | http://57.156.65.216:8080 |
| 📖 Swagger UI | https://gs1-java-devops.onrender.com/swagger-ui/index.html |
| 📄 OpenAPI JSON | https://gs1-java-devops.onrender.com/api-docs |
| 🎥 Vídeo Demo (até 10 min) | https://youtu.be/8bm3bk12Tg4 |
| 🎯 Pitch (3 min) | https://www.youtube.com/watch?v=E6RTz_CEIDc |

---

## 🏗️ Arquitetura Macro

```
┌─────────────────────────────────────────────────────────────┐
│                     AZURE VM (Ubuntu 22.04)                  │
│                     IP: 57.156.65.216                        │
│                                                              │
│   ┌──────────────────────┐    ┌──────────────────────────┐  │
│   │   rm563197-app       │    │      rm563197-db          │  │
│   │  Spring Boot :8080   │◄──►│    PostgreSQL 16 :5432   │  │
│   │  (Java 17)           │    │    schema: rm562085       │  │
│   └──────────┬───────────┘    └──────────────────────────┘  │
│              │  rede interna: phyconet                       │
│              │  volume: rm563197-pgdata                      │
└──────────────┼──────────────────────────────────────────────┘
               │
    ┌──────────▼──────────┐
    │   APIs Externas     │
    │  NASA POWER (06h)   │
    │  Open-Meteo (15min) │
    │  INMET (01h)        │
    └─────────────────────┘
```

**Fluxo de dados:**

1. Usuário autentica via `POST /api/auth/login` → recebe **JWT**
2. Requisições com `Authorization: Bearer <token>` chegam no Spring Boot (porta 8080)
3. `JwtAuthFilter` valida o token antes de qualquer controller ser atingido
4. API persiste/consulta dados no **PostgreSQL** (rede interna `phyconet`)
5. **Schedulers** coletam dados das APIs externas automaticamente
6. Dados persistidos no volume nomeado `rm563197-pgdata`

---

## 📦 Stack Tecnológico

| Camada | Tecnologia | Versão |
|---|---|---|
| Linguagem | Java | 17 |
| Framework | Spring Boot | 3.4.5 |
| Segurança | Spring Security + JWT (jjwt) | 0.12.6 |
| Persistência | Spring Data JPA + Hibernate | - |
| Banco de Dados | PostgreSQL | 16 |
| Documentação | SpringDoc OpenAPI (Swagger UI) | 2.8.9 |
| Hipermídia | Spring HATEOAS | - |
| Validação | Spring Validation (Bean Validation 3.0) | - |
| Produtividade | Lombok + Spring Boot DevTools | - |
| Containers | Docker + Docker Compose | - |
| Cloud | Azure Virtual Machine | Ubuntu 22.04 |
| APIs externas | NASA POWER, Open-Meteo, INMET | - |

---

## 🗄️ Modelo de Dados

```
TB_PERFIL (1) ────────── (N) TB_USUARIO (1) ──── (N) TB_FAZENDA (1) ── (N) TB_TANQUE
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

## 🧬 Modelagem Avançada JPA

### Herança com `@MappedSuperclass` — `AuditEntity`

Todas as entidades do sistema estendem `AuditEntity`, que injeta automaticamente os campos de auditoria `criado_em` e `atualizado_em` via callbacks JPA (`@PrePersist` / `@PreUpdate`):

```java
@MappedSuperclass
@Getter @Setter
public abstract class AuditEntity {

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm    = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
```

Todas as entidades concretas (`Fazenda`, `Tanque`, `Usuario`, `LoteBiomassa`, etc.) herdam essa classe, garantindo rastreabilidade de criação e atualização sem repetição de código.

### Entidade `Fazenda` — relacionamentos e herança aplicados

```java
@Entity
@Table(name = "TB_FAZENDA", schema = "rm562085")
public class Fazenda extends AuditEntity {          // ← herança de AuditEntity

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_fazenda")
    @SequenceGenerator(name = "sq_fazenda", sequenceName = "rm562085.SQ_FAZENDA", allocationSize = 1)
    @Column(name = "id_fazenda")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_responsavel", nullable = false)
    private Usuario usuarioResponsavel;             // ← relacionamento N:1

    @OneToMany(mappedBy = "fazenda", fetch = FetchType.LAZY)
    private List<Tanque> tanques;                   // ← relacionamento 1:N

    @OneToMany(mappedBy = "fazenda", fetch = FetchType.LAZY)
    private List<LoteBiomassa> lotes;

    @OneToMany(mappedBy = "fazenda", fetch = FetchType.LAZY)
    private List<CreditoCarbono> creditos;

    // latitude, longitude, nome, cidade, uf, status ...
}
```

### Entidade `Tanque` — múltiplas tabelas com relacionamentos

```java
@Entity
@Table(name = "TB_TANQUE", schema = "rm562085")
public class Tanque extends AuditEntity {           // ← herança de AuditEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fazenda", nullable = false)
    private Fazenda fazenda;                        // ← relacionamento N:1

    @OneToMany(mappedBy = "tanque", fetch = FetchType.LAZY)
    private List<LoteBiomassa> lotes;               // ← relacionamento 1:N

    // codigoTanque, tipoAlga, capacidadeLitros,
    // phMin, phMax, temperaturaMin, temperaturaMax,
    // status, dtInstalacao ...
}
```

---

## 📝 DTOs e Java Records

A transferência de dados entre camadas é feita **exclusivamente via Java Records imutáveis**, nunca expondo entidades JPA diretamente.

### Request DTOs (com validação embutida)

```java
public class FazendaDTOs {

    // DTO de criação — todos os campos obrigatórios validados
    public record CriarFazendaRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "Cidade é obrigatória")
        String cidade,

        @NotBlank(message = "UF é obrigatória")
        @Size(min = 2, max = 2, message = "UF deve ter 2 caracteres")
        String uf,

        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        BigDecimal longitude

    ) {}

    // DTO de atualização — todos os campos opcionais (PATCH semântico via PUT)
    public record AtualizarFazendaRequest(
        @Size(max = 150) String nome,
        String cidade,
        @Size(min = 2, max = 2) String uf,
        BigDecimal latitude,
        BigDecimal longitude,
        String status
    ) {}
}
```

---

## ✅ Validação de Dados (Spring Validation)

Todos os endpoints que recebem corpo utilizam `@Valid` no parâmetro do controller:

```java
@PostMapping
public ResponseEntity<EntityModel<FazendaResponse>> criar(
        @Valid @RequestBody CriarFazendaRequest request,   // ← @Valid dispara Bean Validation
        @AuthenticationPrincipal UserDetails userDetails) { ... }
```

| Anotação | Campo | Regra |
|---|---|---|
| `@NotBlank` | `nome`, `cidade`, `uf` | Não pode ser nulo nem vazio |
| `@Size(max=150)` | `nome` | Máximo 150 caracteres |
| `@Size(min=2,max=2)` | `uf` | Exatamente 2 caracteres |
| `@DecimalMin/Max` | `latitude` | Entre -90.0 e 90.0 |
| `@DecimalMin/Max` | `longitude` | Entre -180.0 e 180.0 |
| `@NotBlank + @Email` | `email` (Usuario) | Formato de e-mail válido |

---

## 🚨 Tratamento de Exceções — `GlobalExceptionHandler`

O `@RestControllerAdvice` centraliza todos os erros e retorna JSON padronizado:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Schema de erro simples
    public record ErrorResponse(
        int status, String error, String message, LocalDateTime timestamp) {}

    // Schema de erro de validação (com mapa de campos)
    public record ValidationErrorResponse(
        int status, String error, Map<String, String> fields, LocalDateTime timestamp) {}

    @ExceptionHandler(EntityNotFoundException.class)      // → 404
    @ExceptionHandler(MethodArgumentNotValidException.class) // → 400 com mapa de campos
    @ExceptionHandler(BadCredentialsException.class)      // → 401
    @ExceptionHandler(IllegalArgumentException.class)     // → 409
    @ExceptionHandler(Exception.class)                    // → 500
}
```

| HTTP | Exceção | Situação |
|---|---|---|
| 400 | `MethodArgumentNotValidException` | Campos inválidos (`@Valid` falhou) |
| 401 | `BadCredentialsException` | Email ou senha incorretos |
| 404 | `EntityNotFoundException` | Entidade não encontrada |
| 409 | `IllegalArgumentException` | Email/código duplicado |
| 500 | `Exception` | Erro interno inesperado |

---

## 🔐 Segurança — Spring Security + JWT

### Estratégia implementada

| Aspecto | Implementação |
|---|---|
| Algoritmo | `HS256` via `Keys.hmacShaKeyFor` |
| Secret | Variável de ambiente `aquaorbital.jwt.secret` |
| Expiração | Configurável via `aquaorbital.jwt.expiration` (ms) |
| Sessão | `STATELESS` — sem sessão no servidor |
| Senha | `BCryptPasswordEncoder` |
| Filtro | `JwtAuthFilter extends OncePerRequestFilter` |

### Fluxo do `JwtAuthFilter`

```java
// Para cada requisição:
// 1. Extrai o header Authorization
// 2. Valida prefixo "Bearer "
// 3. Extrai e valida o JWT via JwtService
// 4. Carrega UserDetails do banco
// 5. Injeta autenticação no SecurityContext
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(request, response, filterChain) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // passa sem autenticar
            return;
        }
        String jwt       = authHeader.substring(7);
        String userEmail = jwtService.extrairUsername(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValido(jwt, userDetails)) {
                // injeta autenticação no contexto da requisição
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

### Endpoints públicos vs. protegidos

```java
// SecurityConfig.java
private static final String[] PUBLIC_URLS = {
    "/api/auth/login",
    "/api/auth/register",
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/api-docs/**",
    "/v3/api-docs/**"
};

// Toda rota fora da lista acima exige token JWT válido:
.authorizeHttpRequests(auth -> auth
    .requestMatchers(PUBLIC_URLS).permitAll()
    .anyRequest().authenticated()
)
```

---

## 🔗 HATEOAS

Respostas de recursos individuais incluem links de navegação **HAL**. Exemplo real do `FazendaController`:

```java
private EntityModel<FazendaResponse> toModel(FazendaResponse response) {
    return EntityModel.of(response,
        linkTo(methodOn(FazendaController.class)
            .buscar(response.id())).withSelfRel(),
        linkTo(methodOn(FazendaController.class)
            .dashboard(response.id())).withRel("dashboard"),
        linkTo(methodOn(TanqueController.class)
            .listarPorFazenda(response.id())).withRel("tanques"),
        linkTo(methodOn(FazendaController.class)
            .listar(Pageable.unpaged())).withRel("fazendas")
    );
}
```

Resposta JSON com links:

```json
{
  "id": 1,
  "nome": "Fazenda Solar Norte",
  "cidade": "Fortaleza",
  "uf": "CE",
  "latitude": -3.7172,
  "longitude": -38.5433,
  "status": "ATIVA",
  "criadoEm": "2026-06-01T10:00:00",
  "_links": {
    "self":      { "href": "http://57.156.65.216:8080/api/fazendas/1" },
    "dashboard": { "href": "http://57.156.65.216:8080/api/fazendas/1/dashboard" },
    "tanques":   { "href": "http://57.156.65.216:8080/api/tanques?fazendaId=1" },
    "fazendas":  { "href": "http://57.156.65.216:8080/api/fazendas" }
  }
}
```

---

## 📋 Endpoints

| Módulo | Método | Rota | Descrição |
|---|---|---|---|
| **Auth** | POST | `/api/auth/register` | Registrar usuário |
| **Auth** | POST | `/api/auth/login` | Login → retorna JWT |
| **Perfis** | POST | `/api/perfis` | Criar perfil |
| **Perfis** | GET | `/api/perfis` | Listar perfis |
| **Usuários** | GET | `/api/usuarios` | Listar usuários |
| **Usuários** | GET | `/api/usuarios/{id}` | Buscar por ID |
| **Usuários** | PUT | `/api/usuarios/{id}` | Atualizar |
| **Usuários** | DELETE | `/api/usuarios/{id}` | Remover |
| **Fazendas** | POST | `/api/fazendas` | Criar fazenda |
| **Fazendas** | GET | `/api/fazendas` | Listar (paginado) |
| **Fazendas** | GET | `/api/fazendas/{id}` | Buscar por ID |
| **Fazendas** | GET | `/api/fazendas/{id}/dashboard` | Dashboard completo |
| **Fazendas** | GET | `/api/fazendas/minhas` | Fazendas do usuário autenticado |
| **Fazendas** | PUT | `/api/fazendas/{id}` | Atualizar |
| **Fazendas** | DELETE | `/api/fazendas/{id}` | Remover |
| **Tanques** | CRUD | `/api/tanques` | Gestão de tanques |
| **Dados NASA** | POST | `/api/dados-orbitais/fazenda/{id}` | Coletar dados NASA |
| **Dados NASA** | GET | `/api/dados-orbitais/fazenda/{id}` | Listar dados coletados |
| **Open-Meteo** | POST | `/api/dados-orbitais/open-meteo/fazenda/{id}` | Coletar previsão |
| **ERA5** | POST | `/api/dados-orbitais/era5/fazenda/{id}` | Coletar ERA5 |
| **INMET** | CRUD | `/api/dados-orbitais/inmet` | Dados INMET |
| **Lotes** | CRUD | `/api/marketplace/lotes` | Lotes de biomassa |
| **Créditos** | CR | `/api/marketplace/creditos` | Créditos de carbono |
| **Transações** | CR | `/api/marketplace/transacoes` | Transações marketplace |

---

## 🔄 Agendadores Automáticos

| Scheduler | Horário | Fonte | Dado coletado |
|---|---|---|---|
| `NasaSyncScheduler` | 06:00 diário | NASA POWER | Radiação solar, temperatura, umidade orbital |
| `OpenMeteoSyncScheduler` | A cada 15 min | Open-Meteo | Previsão climática em tempo real |
| `InMetSyncScheduler` | 01:00 diário | INMET | Dados meteorológicos nacionais |

---

## 🗂️ Estrutura do Repositório

```
gs1-java-devops/
├── docker/
│   ├── Dockerfile                          # Imagem multi-stage da API
│   ├── docker-compose.yml                  # Orquestração dos 2 containers
│   └── sql/
│       └── init.sql                        # DDL + dados iniciais (PostgreSQL)
├── src/main/java/br/com/fiap/Phycocarbon/
│   ├── config/
│   │   └── SecurityConfig.java             # Spring Security + filtros + CORS
│   ├── controller/
│   │   ├── FazendaController.java          # CRUD + HATEOAS + Dashboard
│   │   ├── TanqueController.java
│   │   ├── AuthController.java
│   │   └── ...
│   ├── dto/
│   │   ├── FazendaDTOs.java                # CriarFazendaRequest, AtualizarFazendaRequest (Records)
│   │   └── ResponseDTOs.java               # FazendaResponse, DashboardFazendaResponse (Records)
│   ├── entity/
│   │   ├── AuditEntity.java                # @MappedSuperclass — herança de auditoria
│   │   ├── Fazenda.java                    # extends AuditEntity
│   │   ├── Tanque.java                     # extends AuditEntity
│   │   ├── Usuario.java                    # extends AuditEntity
│   │   ├── LoteBiomassa.java
│   │   ├── CreditoCarbono.java
│   │   └── TransacaoMarketplace.java
│   ├── exception/
│   │   └── GlobalExceptionHandler.java     # @RestControllerAdvice — erros padronizados
│   ├── repository/
│   │   ├── FazendaRepository.java          # JpaRepository<Fazenda, Long>
│   │   └── ...
│   ├── scheduler/
│   │   ├── NasaSyncScheduler.java
│   │   ├── OpenMeteoSyncScheduler.java
│   │   └── InMetSyncScheduler.java
│   ├── security/
│   │   ├── JwtAuthFilter.java              # OncePerRequestFilter
│   │   └── JwtService.java                 # geração e validação de tokens
│   └── service/
│       ├── FazendaService.java
│       └── ...
└── src/main/resources/
    ├── application.properties              # Profile padrão (Oracle FIAP)
    └── application-docker.properties       # Profile Docker (PostgreSQL)
```

---

## 🧪 Testes da API

### Pré-requisito: obter token JWT

```bash
# 1. Criar perfil (rota pública)
curl -s -X POST http://57.156.65.216:8080/api/perfis \
  -H "Content-Type: application/json" \
  -d '{"nomePerfil":"ADMIN","descricao":"Administrador"}'

# 2. Registrar usuário (rota pública)
curl -s -X POST http://57.156.65.216:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teste","email":"teste@fiap.com","senha":"senha123","nomePerfil":"ADMIN"}'

# 3. Login — salvar o token retornado
curl -s -X POST http://57.156.65.216:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teste@fiap.com","senha":"senha123"}'
```

Resposta esperada do login:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer"
}
```

### CRUD de Fazendas

```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# CREATE — espera 201 Created
curl -s -X POST http://57.156.65.216:8080/api/fazendas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nome":"Fazenda Solar Norte","cidade":"Fortaleza","uf":"CE","latitude":-3.7172,"longitude":-38.5433}'

# READ — listar todas (paginado)
curl -s -X GET "http://57.156.65.216:8080/api/fazendas?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# READ — buscar por ID (retorna com _links HATEOAS)
curl -s -X GET http://57.156.65.216:8080/api/fazendas/1 \
  -H "Authorization: Bearer $TOKEN"

# READ — dashboard da fazenda
curl -s -X GET http://57.156.65.216:8080/api/fazendas/1/dashboard \
  -H "Authorization: Bearer $TOKEN"

# UPDATE — espera 200 OK
curl -s -X PUT http://57.156.65.216:8080/api/fazendas/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nome":"Fazenda Solar Norte Atualizada","status":"ATIVA"}'

# DELETE — espera 204 No Content
curl -s -X DELETE http://57.156.65.216:8080/api/fazendas/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Testando validações (erros esperados)

```bash
# Campo obrigatório ausente → espera 400 Bad Request
curl -s -X POST http://57.156.65.216:8080/api/fazendas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cidade":"São Paulo"}'
```

Resposta esperada `400`:
```json
{
  "status": 400,
  "error": "Validation Error",
  "fields": {
    "nome": "Nome é obrigatório",
    "uf": "UF é obrigatória"
  },
  "timestamp": "2026-06-09T12:00:00"
}
```

```bash
# UF com tamanho errado → espera 400
curl -s -X POST http://57.156.65.216:8080/api/fazendas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teste","cidade":"SP","uf":"SPP","latitude":-23.5,"longitude":-46.6}'
```

Resposta esperada `400`:
```json
{
  "status": 400,
  "error": "Validation Error",
  "fields": { "uf": "UF deve ter 2 caracteres" },
  "timestamp": "2026-06-09T12:00:00"
}
```

```bash
# Sem token → espera 401 Unauthorized
curl -s -X GET http://57.156.65.216:8080/api/fazendas
```

Resposta esperada `401`:
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Credenciais inválidas",
  "timestamp": "2026-06-09T12:00:00"
}
```

```bash
# Recurso inexistente → espera 404 Not Found
curl -s -X GET http://57.156.65.216:8080/api/fazendas/9999 \
  -H "Authorization: Bearer $TOKEN"
```

Resposta esperada `404`:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Fazenda não encontrada: 9999",
  "timestamp": "2026-06-09T12:00:00"
}
```

---

## 🚀 How To — Do Clone ao Deploy em Nuvem

### Pré-requisitos locais

- Git
- Docker Desktop (Windows/Mac) ou Docker Engine (Linux)
- Conta Azure com créditos ativos

---

### PARTE 1 — Execução Local

#### 1. Clonar o repositório

```bash
git clone https://github.com/GS1-2TDSPG-2026/gs1-java-devops.git
cd gs1-java-devops
```

#### 2. Subir os containers

```bash
cd docker
docker compose up -d --build
```

> Build multi-stage do JAR ocorre dentro do container. Aguarde ~3 minutos na primeira execução.

#### 3. Verificar containers

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
docker logs rm563197-db
docker logs rm563197-app
docker logs -f rm563197-app    # tempo real
```

#### 5. Inspecionar containers

```bash
docker container exec rm563197-app whoami   # phycouser
docker container exec rm563197-app pwd      # /app/phycocarbon
docker container exec rm563197-app ls -la

docker container exec rm563197-db whoami    # postgres
docker container exec rm563197-db pwd
docker container exec rm563197-db ls -la /var/lib/postgresql/data
```

#### 6. Verificar persistência no banco

```bash
docker container exec -it rm563197-db psql -U phycouser -d phycocarbon

-- Listar tabelas do schema:
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'rm562085' ORDER BY table_name;

-- Verificar perfis:
SELECT * FROM rm562085.tb_perfil;

-- Verificar usuários:
SELECT id_usuario, nome, email, status FROM rm562085.tb_usuario;

-- Verificar fazendas:
SELECT * FROM rm562085.tb_fazenda;

\q
```

#### 7. Parar os containers

```bash
docker compose down

# Remove também o volume (APAGA OS DADOS):
docker compose down -v
```

---

### PARTE 2 — Deploy na Azure VM

#### Passo 1 — Criar VM

```bash
az login

az group create --name rg-phycocarbon --location brazilsouth

az network public-ip create \
  --resource-group rg-phycocarbon \
  --name ip-phycocarbon \
  --allocation-method Static \
  --sku Standard

az vm create \
  --resource-group rg-phycocarbon \
  --name vm-phycocarbon \
  --image Ubuntu2204 \
  --size Standard_B2s \
  --admin-username azureuser \
  --generate-ssh-keys \
  --public-ip-address ip-phycocarbon \
  --public-ip-sku Standard

# Guardar o IP retornado:
az network public-ip show \
  --resource-group rg-phycocarbon \
  --name ip-phycocarbon \
  --query ipAddress --output tsv
```

#### Passo 2 — Abrir porta 8080

```bash
az vm open-port \
  --resource-group rg-phycocarbon \
  --name vm-phycocarbon \
  --port 8080 --priority 1001
```

#### Passo 3 — Conectar via SSH

```bash
ssh azureuser@<IP_PUBLICO>
```

#### Passo 4 — Provisionar Docker na VM

```bash
curl -fsSL https://raw.githubusercontent.com/GS1-2TDSPG-2026/gs1-java-devops/main/provision.sh | sudo bash
newgrp docker
```

#### Passo 5 — Clonar e subir

```bash
git clone https://github.com/GS1-2TDSPG-2026/gs1-java-devops.git
cd gs1-java-devops/docker
docker compose up -d --build
```

#### Passo 6 — Monitorar inicialização

```bash
docker logs -f rm563197-app
# Aguardar: Started PhycoCarbonApplication in X.XXX seconds
```

#### Passo 7 — Validar deploy

```bash
docker compose ps
docker container exec rm563197-app whoami
docker container exec rm563197-app pwd
docker container exec rm563197-app ls -la
docker container exec rm563197-db whoami
docker container exec rm563197-db ls -la /var/lib/postgresql/data

docker container exec -it rm563197-db psql -U phycouser -d phycocarbon \
  -c "SELECT table_name FROM information_schema.tables WHERE table_schema='rm562085';"

docker container exec -it rm563197-db psql -U phycouser -d phycocarbon \
  -c "SELECT * FROM rm562085.tb_perfil;"

curl http://<IP_PUBLICO>:8080/api-docs | head -5
```

#### Passo 8 — Acessar Swagger em nuvem

```
http://<IP_PUBLICO>:8080/swagger-ui.html
```

---

### PARTE 3 — Gerenciamento de Custos Azure

```bash
# DESALOCAR quando não estiver em uso (para o billing de compute)
az vm deallocate --resource-group rg-phycocarbon --name vm-phycocarbon

# Religar antes da apresentação
az vm start --resource-group rg-phycocarbon --name vm-phycocarbon

# Verificar status
az vm show \
  --resource-group rg-phycocarbon \
  --name vm-phycocarbon \
  --show-details --query powerState
```

> ⚠️ **Use `deallocate`, nunca `stop`.** O comando `stop` mantém o billing de compute ativo.

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Versão | Finalidade |
|---|---|---|
| Java | 17 | Linguagem principal |
| Spring Boot | 3.4.5 | Framework base |
| Spring Data JPA + Hibernate | - | ORM e persistência relacional |
| Spring Security | - | Autenticação e autorização |
| jjwt | 0.12.6 | Geração e validação de tokens JWT |
| Spring HATEOAS | - | Links de hipermídia nas respostas |
| Spring Validation | - | Bean Validation 3.0 nos DTOs |
| Lombok | - | Redução de boilerplate |
| Spring Boot DevTools | - | Produtividade no desenvolvimento |
| SpringDoc OpenAPI | 2.8.9 | Documentação Swagger UI automática |
| PostgreSQL | 16 | Banco de dados relacional |
| Docker + Docker Compose | - | Containerização e orquestração |
| Azure VM | Ubuntu 22.04 | Infraestrutura em nuvem |
| NASA POWER API | - | Dados satelitais de radiação e clima |
| Open-Meteo API | - | Previsão climática em tempo real |
| INMET API | - | Dados meteorológicos nacionais |
