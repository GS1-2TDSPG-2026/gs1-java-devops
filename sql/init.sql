CREATE SCHEMA IF NOT EXISTS rm562085;

-- ── Sequences (nomes idênticos às @SequenceGenerator) ────────
CREATE SEQUENCE IF NOT EXISTS rm562085.sq_perfil               START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS rm562085.sq_usuario              START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS rm562085.sq_fazenda              START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS rm562085.sq_tanque               START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS rm562085.sq_dado_orbital         START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS rm562085.sq_dado_orbital_br      START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS rm562085.sq_lote_biomassa        START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS rm562085.sq_credito_carbono      START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS rm562085.sq_transacao_marketplace START 1 INCREMENT 1;

-- ── TB_PERFIL ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rm562085.tb_perfil (
    id_perfil     BIGINT PRIMARY KEY DEFAULT nextval('rm562085.sq_perfil'),
    nome_perfil   VARCHAR(50)  NOT NULL UNIQUE,
    descricao     VARCHAR(200),
    criado_em     TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── TB_USUARIO ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rm562085.tb_usuario (
    id_usuario    BIGINT PRIMARY KEY DEFAULT nextval('rm562085.sq_usuario'),
    id_perfil     BIGINT       NOT NULL REFERENCES rm562085.tb_perfil(id_perfil),
    nome          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    senha_hash    TEXT         NOT NULL,
    telefone      VARCHAR(20),
    status        VARCHAR(20)  NOT NULL DEFAULT 'A',
    criado_em     TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── TB_FAZENDA ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rm562085.tb_fazenda (
    id_fazenda             BIGINT PRIMARY KEY DEFAULT nextval('rm562085.sq_fazenda'),
    id_usuario_responsavel BIGINT       NOT NULL REFERENCES rm562085.tb_usuario(id_usuario),
    nome                   VARCHAR(150) NOT NULL,
    cidade                 VARCHAR(100) NOT NULL,
    uf                     VARCHAR(2)   NOT NULL,
    latitude               NUMERIC(10,6),
    longitude              NUMERIC(10,6),
    status                 VARCHAR(20)  NOT NULL DEFAULT 'ATIVA',
    criado_em              TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em          TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── TB_TANQUE ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rm562085.tb_tanque (
    id_tanque         BIGINT PRIMARY KEY DEFAULT nextval('rm562085.sq_tanque'),
    id_fazenda        BIGINT       NOT NULL REFERENCES rm562085.tb_fazenda(id_fazenda),
    codigo_tanque     VARCHAR(30)  NOT NULL UNIQUE,
    tipo_alga         VARCHAR(50)  NOT NULL,
    capacidade_litros NUMERIC(10,2),
    ph_min            NUMERIC(4,2),
    ph_max            NUMERIC(4,2),
    temperatura_min   NUMERIC(4,2),
    temperatura_max   NUMERIC(4,2),
    status            VARCHAR(20)  NOT NULL DEFAULT 'ATIVO',
    dt_instalacao     DATE,
    criado_em         TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em     TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── TB_DADO_ORBITAL ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rm562085.tb_dado_orbital (
    id_dado_orbital      BIGINT PRIMARY KEY DEFAULT nextval('rm562085.sq_dado_orbital'),
    id_fazenda           BIGINT       NOT NULL REFERENCES rm562085.tb_fazenda(id_fazenda),
    fonte                VARCHAR(50)  NOT NULL,
    dt_coleta            DATE         NOT NULL,
    irradiancia_par      NUMERIC(10,4),
    nebulosidade         NUMERIC(5,2),
    temperatura_ambiente NUMERIC(5,2),
    latitude             NUMERIC(10,6),
    longitude            NUMERIC(10,6),
    criado_em            TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em        TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── TB_DADO_ORBITAL_BR (INMET) ────────────────────────────────
CREATE TABLE IF NOT EXISTS rm562085.tb_dado_orbital_br (
    id_dado_orbital_br BIGINT PRIMARY KEY DEFAULT nextval('rm562085.sq_dado_orbital_br'),
    cod_estacao        VARCHAR(10)  NOT NULL,
    nome_estacao       VARCHAR(100),
    dt_medicao         VARCHAR(10)  NOT NULL,
    hr_medicao         VARCHAR(4),
    temp_maxima        NUMERIC(6,2),
    temp_minima        NUMERIC(6,2),
    temp_media         NUMERIC(6,2),
    umidade_relativa   NUMERIC(6,2),
    precipitacao       NUMERIC(8,2),
    velocidade_vento   NUMERIC(6,2),
    direcao_vento      NUMERIC(6,2),
    pressao_atm        NUMERIC(8,2),
    radiacao_global    NUMERIC(10,2),
    json_original      TEXT,
    criado_em          TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── TB_LOTE_BIOMASSA ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rm562085.tb_lote_biomassa (
    id_lote        BIGINT PRIMARY KEY DEFAULT nextval('rm562085.sq_lote_biomassa'),
    id_fazenda     BIGINT         NOT NULL REFERENCES rm562085.tb_fazenda(id_fazenda),
    id_tanque      BIGINT         NOT NULL REFERENCES rm562085.tb_tanque(id_tanque),
    taxonomia_alga VARCHAR(100)   NOT NULL,
    peso_kg        NUMERIC(10,3)  NOT NULL,
    preco_unitario NUMERIC(12,2)  NOT NULL,
    status         VARCHAR(20)    NOT NULL DEFAULT 'DISPONIVEL',
    dt_colheita    DATE           NOT NULL DEFAULT CURRENT_DATE,
    criado_em      TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em  TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ── TB_CREDITO_CARBONO ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rm562085.tb_credito_carbono (
    id_credito     BIGINT PRIMARY KEY DEFAULT nextval('rm562085.sq_credito_carbono'),
    id_fazenda     BIGINT         NOT NULL REFERENCES rm562085.tb_fazenda(id_fazenda),
    id_lote        BIGINT         NOT NULL REFERENCES rm562085.tb_lote_biomassa(id_lote),
    co2_toneladas  NUMERIC(12,4)  NOT NULL,
    hash_auditoria VARCHAR(256)   NOT NULL UNIQUE,
    status         VARCHAR(20)    NOT NULL DEFAULT 'GERADO',
    dt_validacao   TIMESTAMP,
    criado_em      TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em  TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ── TB_TRANSACAO_MARKETPLACE ──────────────────────────────────
CREATE TABLE IF NOT EXISTS rm562085.tb_transacao_marketplace (
    id_transacao         BIGINT PRIMARY KEY DEFAULT nextval('rm562085.sq_transacao_marketplace'),
    id_usuario_comprador BIGINT         NOT NULL REFERENCES rm562085.tb_usuario(id_usuario),
    id_lote              BIGINT         REFERENCES rm562085.tb_lote_biomassa(id_lote),
    id_credito           BIGINT         REFERENCES rm562085.tb_credito_carbono(id_credito),
    tipo_transacao       VARCHAR(30)    NOT NULL,
    quantidade           NUMERIC(12,3)  NOT NULL,
    valor_total          NUMERIC(14,2)  NOT NULL,
    status               VARCHAR(20)    NOT NULL DEFAULT 'PENDENTE',
    criado_em            TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em        TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ── Dados iniciais ────────────────────────────────────────────
INSERT INTO rm562085.tb_perfil (nome_perfil, descricao) VALUES
    ('ADMIN',      'Administrador do sistema'),
    ('PRODUTOR',   'Produtor de microalgas'),
    ('COMPRADOR',  'Comprador de biomassa e créditos'),
    ('INVESTIDOR', 'Investidor em créditos de carbono')
ON CONFLICT (nome_perfil) DO NOTHING;
