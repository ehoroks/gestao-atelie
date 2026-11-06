-- Trilha B - faixa V2xx (convencao D3).
-- B5: desconto absoluto (em reais) e forma de pagamento.
-- O desconto nao pode exceder o subtotal; a validacao fica no dominio.

ALTER TABLE vendas
    ADD COLUMN desconto        NUMERIC(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN forma_pagamento VARCHAR(20);
