package br.com.atelie.aplicacao;

import br.com.atelie.catalogo.Produto;
import br.com.atelie.catalogo.ProdutoRepository;
import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.estoque.ServicoEstoque;
import br.com.atelie.venda.ItemVenda;
import br.com.atelie.venda.Venda;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Objects;

/** Resolve os produtos da venda e delega a baixa ao servico de estoque. */
@Service
public class AdaptadorEstoqueDaVenda implements EstoqueDaVenda {

    private final ProdutoRepository produtos;
    private final ServicoEstoque estoque;

    public AdaptadorEstoqueDaVenda(ProdutoRepository produtos, ServicoEstoque estoque) {
        this.produtos = Objects.requireNonNull(produtos, "repositorio de produtos obrigatorio");
        this.estoque = Objects.requireNonNull(estoque, "servico de estoque obrigatorio");
    }

    @Override
    @Transactional
    public void baixarItensDaVenda(Venda venda) {
        estoque.baixar(montarLinhas(venda, true), "venda %s".formatted(venda.getId()));
    }

    @Override
    @Transactional
    public void estornarItensDaVenda(Venda venda) {
        estoque.estornar(montarLinhas(venda, false),
                "cancelamento da venda %s".formatted(venda.getId()));
    }

    private List<ServicoEstoque.Linha> montarLinhas(Venda venda, boolean exigirProdutoAtivo) {
        Objects.requireNonNull(venda, "venda obrigatoria");
        Map<Long, BigDecimal> quantidadesPorProduto = new TreeMap<>();
        for (ItemVenda item : venda.getItens()) {
            quantidadesPorProduto.merge(item.getProdutoId(), item.getQuantidade(), BigDecimal::add);
        }

        List<ServicoEstoque.Linha> linhas = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> pedido : quantidadesPorProduto.entrySet()) {
            Produto produto = produtos.buscarPorIdParaAtualizacao(pedido.getKey())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", pedido.getKey()));
            if (exigirProdutoAtivo && !produto.isAtivo()) {
                throw new RegraNegocioException(
                        "produto %d esta desativado e nao pode ser vendido".formatted(pedido.getKey()));
            }
            linhas.add(new ServicoEstoque.Linha(produto, pedido.getValue()));
        }
        return List.copyOf(linhas);
    }
}
