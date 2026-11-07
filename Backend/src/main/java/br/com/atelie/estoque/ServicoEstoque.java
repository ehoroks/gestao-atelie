package br.com.atelie.estoque;

import java.math.BigDecimal;
import java.util.List;

/**
 * CONTRATO CONGELADO - Sprint 0. Implementacao: Trilha 1 (atividade 1.5).
 *
 * Fronteira unica entre Vendas/Encomendas e o estoque. Venda e Encomenda
 * NAO chamam registrarSaida diretamente - chamam este servico, que aplica
 * as linhas de forma atomica (tudo ou nada).
 */
public interface ServicoEstoque {

    /** Uma linha de movimentacao. Vale para produto vendido ou insumo consumido. */
    record Linha(ItemEstoque item, BigDecimal quantidade) {}

    /** Verifica todas as linhas sem alterar nada. */
    boolean haDisponibilidade(List<Linha> linhas);

    /**
     * Baixa todas as linhas. Atomico.
     * @throws br.com.atelie.comum.EstoqueInsuficienteException na primeira linha sem saldo
     */
    void baixar(List<Linha> linhas, String motivo);

    /** Devolve ao estoque (cancelamento de venda, encomenda cancelada). Atomico. */
    void estornar(List<Linha> linhas, String motivo);
}
