-- Trilha B - faixa V2xx (convencao D3).
-- B3: itens de venda. `preco_unitario` e uma copia congelada do preco do
-- produto no momento da venda; nao ha FK para produtos/vendas nesta etapa,
-- pois essas tabelas pertencem as Trilhas A e B4.

CREATE TABLE itens_venda (
    id              BIGSERIAL     PRIMARY KEY,
    venda_id        BIGINT        NOT NULL,
    produto_id      BIGINT        NOT NULL,
    quantidade      NUMERIC(12,3) NOT NULL,
    preco_unitario  NUMERIC(12,2) NOT NULL,
    criado_em       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_itens_venda_venda ON itens_venda (venda_id);
CREATE INDEX idx_itens_venda_produto ON itens_venda (produto_id);
