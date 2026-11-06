package br.com.atelie.aplicacao;

import br.com.atelie.venda.Venda;

/**
 * Trilha B - B7. Porta da integracao I1 (Vendas x Estoque).
 *
 * Coordenacao da baixa de estoque ao finalizar a venda. A regra do planejamento
 * (sugestao das ambiguidades) e nao fazer chamada direta de entidade para
 * entidade: `ServicoVenda` chama esta porta, e a implementacao cuida de
 * resolver `produtoId` -> `ItemEstoque` e invocar `ServicoEstoque.baixar`.
 *
 * ADAPTADOR PENDENTE: exige `ProdutoRepository` e a implementacao de producao
 * de `ServicoEstoque`, que pertencem a Trilha A. Enquanto nao existir um bean
 * desta interface no contexto, `ServicoVenda` responde HTTP 503 e nao conclui
 * a venda; assim, nao confirma uma operacao sem baixar o estoque.
 */
@FunctionalInterface
public interface EstoqueDaVenda {

    /**
     * Verifica disponibilidade e baixa o estoque dos itens da venda.
     *
     * @throws br.com.atelie.comum.EstoqueInsuficienteException se saldo
     *         insuficiente; a transacao desfaz a finalizacao (rollback).
     */
    void baixarItensDaVenda(Venda venda);
}
