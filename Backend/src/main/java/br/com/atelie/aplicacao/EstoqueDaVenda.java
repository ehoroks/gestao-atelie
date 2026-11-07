package br.com.atelie.aplicacao;

import br.com.atelie.venda.Venda;

/**
 * Trilha B - B7/B8. Porta das integracoes I1/I5 (Vendas x Estoque).
 *
 * `ServicoVenda` usa esta porta para baixar o estoque ao finalizar e estornar
 * ao cancelar. O adaptador resolve `produtoId` -> `ItemEstoque` e delega as
 * operacoes a `ServicoEstoque`.
 */
public interface EstoqueDaVenda {

    /**
     * Verifica disponibilidade e baixa o estoque dos itens da venda.
     *
     * @throws br.com.atelie.comum.EstoqueInsuficienteException se saldo
     *         insuficiente; a transacao desfaz a finalizacao (rollback).
     */
    void baixarItensDaVenda(Venda venda);

    /** Devolve ao estoque os produtos de uma venda que foi concluida. */
    void estornarItensDaVenda(Venda venda);
}
