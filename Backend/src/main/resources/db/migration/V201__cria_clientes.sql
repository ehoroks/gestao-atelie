-- Trilha B - faixa V2xx (convencao D3).
-- B2: clientes. Telefone e armazenado somente com digitos (10 ou 11).

CREATE TABLE clientes (
    id         BIGSERIAL    PRIMARY KEY,
    nome       VARCHAR(120) NOT NULL,
    telefone   VARCHAR(20)  NOT NULL,
    email      VARCHAR(180),
    criado_em  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clientes_nome ON clientes (lower(nome));
CREATE INDEX idx_clientes_telefone ON clientes (telefone);
