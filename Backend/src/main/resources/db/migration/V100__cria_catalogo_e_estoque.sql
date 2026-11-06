-- Trilha A: persistencia minima de categorias e produtos para catalogo/estoque.
CREATE TABLE categorias (
    id   BIGSERIAL PRIMARY KEY,
    nome VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE produtos (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(120) NOT NULL,
    quantidade      NUMERIC(12, 3) NOT NULL DEFAULT 0,
    unidade_medida  VARCHAR(10) NOT NULL,
    estoque_minimo  NUMERIC(12, 3) NOT NULL DEFAULT 0,
    categoria_id    BIGINT,
    tamanho         VARCHAR(40),
    cor             VARCHAR(60),
    preco           NUMERIC(12, 2) NOT NULL,
    descricao       VARCHAR(1000),
    foto            VARCHAR(500),
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_produtos_categoria
        FOREIGN KEY (categoria_id) REFERENCES categorias (id) ON DELETE RESTRICT
);
