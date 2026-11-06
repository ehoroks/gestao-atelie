-- Trilha B - B8: registra data e motivo do cancelamento da venda.
ALTER TABLE vendas
    ADD COLUMN cancelada_em TIMESTAMP,
    ADD COLUMN motivo_cancelamento VARCHAR(500);
