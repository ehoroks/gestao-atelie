-- Trilha A (integracao D6): persistencia de insumos e historico append-only.
-- A raiz comum preserva os ids ja existentes de produtos e permite que
-- movimentacoes referenciem produto e insumo por uma unica FK.
CREATE TABLE item_estoque (
    id              BIGSERIAL PRIMARY KEY,
    tipo            VARCHAR(20) NOT NULL,
    nome            VARCHAR(120) NOT NULL,
    quantidade      NUMERIC(12, 3) NOT NULL DEFAULT 0,
    unidade_medida  VARCHAR(10) NOT NULL,
    estoque_minimo  NUMERIC(12, 3) NOT NULL DEFAULT 0,
    CONSTRAINT ck_item_estoque_quantidade CHECK (quantidade >= 0),
    CONSTRAINT ck_item_estoque_tipo CHECK (tipo IN ('PRODUTO', 'INSUMO'))
);

INSERT INTO item_estoque (id, tipo, nome, quantidade, unidade_medida, estoque_minimo)
SELECT id, 'PRODUTO', nome, quantidade, unidade_medida, estoque_minimo
FROM produtos;

SELECT setval(pg_get_serial_sequence('item_estoque', 'id'),
              COALESCE((SELECT MAX(id) FROM item_estoque), 1),
              EXISTS (SELECT 1 FROM item_estoque));

CREATE TABLE produtos_novo (
    id           BIGINT PRIMARY KEY REFERENCES item_estoque (id) ON DELETE RESTRICT,
    categoria_id BIGINT REFERENCES categorias (id) ON DELETE RESTRICT,
    tamanho      VARCHAR(40),
    cor          VARCHAR(60),
    preco        NUMERIC(12, 2) NOT NULL,
    descricao    VARCHAR(1000),
    foto         VARCHAR(500),
    ativo        BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO produtos_novo (id, categoria_id, tamanho, cor, preco, descricao, foto, ativo)
SELECT id, categoria_id, tamanho, cor, preco, descricao, foto, ativo
FROM produtos;

DROP TABLE produtos;
ALTER TABLE produtos_novo RENAME TO produtos;

CREATE TABLE insumos (
    id             BIGINT PRIMARY KEY REFERENCES item_estoque (id) ON DELETE RESTRICT,
    custo_unitario NUMERIC(12, 4) NOT NULL DEFAULT 0,
    CONSTRAINT ck_insumos_custo CHECK (custo_unitario >= 0)
);

CREATE TABLE movimentacoes_estoque (
    id               BIGSERIAL PRIMARY KEY,
    item_id          BIGINT NOT NULL REFERENCES item_estoque (id) ON DELETE RESTRICT,
    tipo             VARCHAR(20) NOT NULL,
    quantidade       NUMERIC(12, 3) NOT NULL,
    saldo_resultante NUMERIC(12, 3) NOT NULL,
    data             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo           VARCHAR(500),
    CONSTRAINT ck_movimentacoes_tipo CHECK (tipo IN ('ENTRADA', 'SAIDA', 'AJUSTE'))
);

CREATE INDEX idx_movimentacoes_item_id ON movimentacoes_estoque (item_id, id);
