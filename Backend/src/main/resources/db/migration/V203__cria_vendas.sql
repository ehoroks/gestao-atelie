-- Trilha B - faixa V2xx (convencao D3).
-- B4: agregado de vendas e associacao JPA dos itens (substitui a fronteira
-- temporaria `venda_id` por FK). `cliente_id` e anulavel (venda de balcao).

CREATE TABLE vendas (
    id          BIGSERIAL   PRIMARY KEY,
    cliente_id  BIGINT,
    situacao    VARCHAR(20) NOT NULL,
    criado_em   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vendas_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE RESTRICT
);

CREATE INDEX idx_vendas_cliente ON vendas (cliente_id);

ALTER TABLE itens_venda
    ADD CONSTRAINT fk_itens_venda_venda
        FOREIGN KEY (venda_id) REFERENCES vendas (id) ON DELETE CASCADE;
