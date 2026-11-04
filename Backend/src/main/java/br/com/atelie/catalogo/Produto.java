package br.com.atelie.catalogo;

import br.com.atelie.estoque.ItemEstoque;
import br.com.atelie.estoque.UnidadeMedida;

import java.math.BigDecimal;

/**
 * CONTRATO CONGELADO - Sprint 0. Corpos marcados: Trilha 2.
 */
public class Produto extends ItemEstoque {

    private Categoria  categoria;
    private String     tamanho;
    private String     cor;
    private BigDecimal preco;
    private String     descricao;
    private String     foto;
    private boolean    ativo = true;

    protected Produto() { }

    public Produto(String nome, UnidadeMedida unidadeMedida, BigDecimal estoqueMinimo,
                   Categoria categoria, BigDecimal preco) {
        super(nome, unidadeMedida, estoqueMinimo);
        this.categoria = categoria;
        this.preco     = exigirNaoNegativo(preco, "preco");
    }

    public void atualizarDados(String nome, Categoria categoria, String tamanho, String cor,
                               BigDecimal preco, String descricao, String foto) {
        throw new UnsupportedOperationException("TODO-T2 (2.1): atualizarDados");
    }

    public void desativar() {
        throw new UnsupportedOperationException("TODO-T2 (2.1): desativar");
    }

    /** AMBIGUIDADE 5: e 'ativo', e 'quantidade > 0', ou os dois? */
    public boolean estaDisponivel() {
        throw new UnsupportedOperationException("TODO-T2 (2.1): estaDisponivel");
    }

    /** Escrita de preco restrita ao pacote: so ComposicaoPreco.aplicarComoPrecoDo chama. */
    void definirPreco(BigDecimal novoPreco) {
        this.preco = exigirNaoNegativo(novoPreco, "preco");
    }

    public Categoria  getCategoria() { return categoria; }
    public String     getTamanho()   { return tamanho; }
    public String     getCor()       { return cor; }
    public BigDecimal getPreco()     { return preco; }
    public String     getDescricao() { return descricao; }
    public String     getFoto()      { return foto; }
    public boolean    isAtivo()      { return ativo; }
}
