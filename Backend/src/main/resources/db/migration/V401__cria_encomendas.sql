-- Trilha D: encomendas reutilizam clientes da Trilha B e produtos/insumos da A.
CREATE TABLE encomendas (
    id                     BIGSERIAL PRIMARY KEY,
    cliente_id             BIGINT NOT NULL REFERENCES clientes (id) ON DELETE RESTRICT,
    produto_id             BIGINT REFERENCES produtos (id) ON DELETE RESTRICT,
    descricao              VARCHAR(500) NOT NULL,
    valor                  NUMERIC(12, 2) NOT NULL,
    data_pedido            DATE NOT NULL,
    data_prevista_entrega  DATE NOT NULL,
    observacoes            VARCHAR(1000),
    situacao               VARCHAR(20) NOT NULL,
    criado_em              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    concluido_em           TIMESTAMP,
    insumos_consumidos     BOOLEAN NOT NULL DEFAULT FALSE,
    versao                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_encomendas_valor CHECK (valor >= 0),
    CONSTRAINT ck_encomendas_situacao CHECK (
        situacao IN ('REGISTRADA', 'EM_PRODUCAO', 'PRONTA', 'ENTREGUE', 'CANCELADA'))
);

CREATE INDEX idx_encomendas_cliente ON encomendas (cliente_id);
CREATE INDEX idx_encomendas_situacao ON encomendas (situacao);
CREATE INDEX idx_encomendas_data_pedido ON encomendas (data_pedido);

CREATE TABLE encomenda_insumos (
    id            BIGSERIAL PRIMARY KEY,
    encomenda_id  BIGINT NOT NULL REFERENCES encomendas (id) ON DELETE CASCADE,
    insumo_id     BIGINT NOT NULL REFERENCES insumos (id) ON DELETE RESTRICT,
    quantidade    NUMERIC(12, 3) NOT NULL,
    CONSTRAINT ck_encomenda_insumos_quantidade CHECK (quantidade > 0),
    CONSTRAINT uq_encomenda_insumos UNIQUE (encomenda_id, insumo_id)
);
