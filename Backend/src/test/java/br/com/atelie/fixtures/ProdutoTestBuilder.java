package br.com.atelie.fixtures;

import br.com.atelie.catalogo.Categoria;
import br.com.atelie.catalogo.Produto;
import br.com.atelie.catalogo.ProdutoRepository;
import br.com.atelie.estoque.UnidadeMedida;

import java.math.BigDecimal;

public class ProdutoTestBuilder {

    private String nome = "Produto de teste";
    private UnidadeMedida unidade = UnidadeMedida.UN;
    private BigDecimal estoqueMinimo = BigDecimal.ZERO;
    private BigDecimal quantidade = BigDecimal.ZERO;
    private Categoria categoria;
    private BigDecimal preco = new BigDecimal("100.00");

    public static ProdutoTestBuilder umProduto() {
        return new ProdutoTestBuilder();
    }

    public ProdutoTestBuilder comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public ProdutoTestBuilder comUnidade(UnidadeMedida unidade) {
        this.unidade = unidade;
        return this;
    }

    public ProdutoTestBuilder comEstoqueMinimo(String estoqueMinimo) {
        this.estoqueMinimo = new BigDecimal(estoqueMinimo);
        return this;
    }

    public ProdutoTestBuilder comQuantidade(String quantidade) {
        this.quantidade = new BigDecimal(quantidade);
        return this;
    }

    public ProdutoTestBuilder comCategoria(Categoria categoria) {
        this.categoria = categoria;
        return this;
    }

    public ProdutoTestBuilder comPreco(String preco) {
        this.preco = new BigDecimal(preco);
        return this;
    }

    public Produto build() {
        Produto produto = new Produto(nome, unidade, estoqueMinimo, categoria, preco);
        if (quantidade.signum() > 0) {
            produto.registrarEntrada(quantidade);
        }
        return produto;
    }

    public Produto persistir(ProdutoRepository produtos) {
        return produtos.saveAndFlush(build());
    }
}
