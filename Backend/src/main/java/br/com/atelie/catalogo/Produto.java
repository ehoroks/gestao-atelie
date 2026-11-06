package br.com.atelie.catalogo;

import br.com.atelie.estoque.ItemEstoque;
import br.com.atelie.estoque.UnidadeMedida;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Trilha A: produto catalogado com saldo de estoque próprio.
 */
@Entity
@Table(name = "produtos")
public class Produto extends ItemEstoque {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria  categoria;

    @Column(name = "tamanho", length = 40)
    private String     tamanho;

    @Column(name = "cor", length = 60)
    private String     cor;

    @Column(name = "preco", nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(name = "descricao", length = 1000)
    private String     descricao;

    @Column(name = "foto", length = 500)
    private String     foto;

    @Column(name = "ativo", nullable = false)
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
        this.nome = exigirTexto(nome, "nome");
        this.preco = exigirNaoNegativo(preco, "preco");
        this.categoria = categoria;
        this.tamanho = normalizarOpcional(tamanho);
        this.cor = normalizarOpcional(cor);
        this.descricao = normalizarOpcional(descricao);
        this.foto = normalizarOpcional(foto);
    }

    public void desativar() {
        ativo = false;
    }

    /** Um produto pode ser vendido se estiver ativo e houver saldo positivo. */
    public boolean estaDisponivel() {
        return ativo && quantidade.signum() > 0;
    }

    private static String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
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
