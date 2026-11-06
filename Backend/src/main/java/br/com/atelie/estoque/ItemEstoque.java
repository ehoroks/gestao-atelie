package br.com.atelie.estoque;

import br.com.atelie.comum.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Contrato de Sprint 0 com as operacoes de estoque implementadas na Trilha A.
 *
 * Nenhuma subclasse altera 'quantidade' diretamente: toda mudanca passa
 * pelos tres metodos abaixo, que sao os unicos que geram movimentacao.
 */
@MappedSuperclass
public abstract class ItemEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long          id;

    @Column(name = "nome", nullable = false, length = 120)
    protected String        nome;

    @Column(name = "quantidade", nullable = false, precision = 12, scale = 3)
    protected BigDecimal    quantidade    = BigDecimal.ZERO;

    @Column(name = "unidade_medida", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    protected UnidadeMedida unidadeMedida;

    @Column(name = "estoque_minimo", nullable = false, precision = 12, scale = 3)
    protected BigDecimal    estoqueMinimo = BigDecimal.ZERO;

    @Transient
    private final List<MovimentacaoEstoque> historico = new ArrayList<>();

    protected ItemEstoque() {
        // exigido pelo mapeamento ORM (Sprint 1)
    }

    protected ItemEstoque(String nome, UnidadeMedida unidadeMedida, BigDecimal estoqueMinimo) {
        this.nome          = exigirTexto(nome, "nome");
        this.unidadeMedida = Objects.requireNonNull(unidadeMedida, "unidadeMedida e obrigatoria");
        this.estoqueMinimo = exigirNaoNegativo(estoqueMinimo, "estoqueMinimo");
    }

    // --- operacoes de estoque (Trilha 1) ---

    /** Soma ao saldo e registra ENTRADA. */
    public void registrarEntrada(BigDecimal quantidade) {
        registrarEntrada(quantidade, null);
    }

    void registrarEntrada(BigDecimal quantidade, String motivo) {
        validarQuantidadePositiva(quantidade);
        this.quantidade = this.quantidade.add(quantidade);
        registrarMovimentacao(TipoMovimentacao.ENTRADA, quantidade, motivo);
    }

    /** Subtrai do saldo e registra SAIDA. Nunca deixa o saldo negativo. */
    public void registrarSaida(BigDecimal quantidade) {
        registrarSaida(quantidade, null);
    }

    void registrarSaida(BigDecimal quantidade, String motivo) {
        validarQuantidadePositiva(quantidade);
        if (!temEstoqueSuficiente(quantidade)) {
            throw new br.com.atelie.comum.EstoqueInsuficienteException(
                    nome, this.quantidade, quantidade);
        }
        this.quantidade = this.quantidade.subtract(quantidade);
        registrarMovimentacao(TipoMovimentacao.SAIDA, quantidade, motivo);
    }

    /** Sobrescreve o saldo e registra AJUSTE. Motivo obrigatorio. */
    public void ajustarQuantidade(BigDecimal novaQuantidade, String motivo) {
        exigirNaoNegativo(novaQuantidade, "novaQuantidade");
        validarUnidade(novaQuantidade);
        exigirTexto(motivo, "motivo");
        BigDecimal saldoAnterior = this.quantidade;
        this.quantidade = novaQuantidade;
        registrarMovimentacao(TipoMovimentacao.AJUSTE,
                novaQuantidade.subtract(saldoAnterior), motivo.trim());
    }

    public boolean temEstoqueSuficiente(BigDecimal quantidade) {
        validarQuantidadePositiva(quantidade);
        return this.quantidade.compareTo(quantidade) >= 0;
    }

    /** AMBIGUIDADE 9: saldo == estoqueMinimo conta como baixo? Decidir no Sprint 0. */
    public boolean estaComEstoqueBaixo() {
        return quantidade.compareTo(estoqueMinimo) <= 0;
    }

    // --- historico ---

    /** Unico ponto de escrita do historico. Chamado pelos tres metodos acima. */
    protected MovimentacaoEstoque registrarMovimentacao(TipoMovimentacao tipo,
                                                        BigDecimal quantidadeMovimentada,
                                                        String motivo) {
        MovimentacaoEstoque mov =
                new MovimentacaoEstoque(this, tipo, quantidadeMovimentada, this.quantidade, motivo);
        historico.add(mov);
        return mov;
    }

    public List<MovimentacaoEstoque> getHistorico() {
        return Collections.unmodifiableList(historico);
    }

    // --- validacoes compartilhadas ---

    protected void validarQuantidadePositiva(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException("Quantidade deve ser maior que zero");
        }
        validarUnidade(valor);
    }

    protected void validarUnidade(BigDecimal valor) {
        if (unidadeMedida != null && !unidadeMedida.aceita(valor)) {
            throw new RegraNegocioException(
                    "Unidade %s nao aceita quantidade fracionada: %s".formatted(unidadeMedida, valor));
        }
    }

    protected static BigDecimal exigirNaoNegativo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() < 0) {
            throw new RegraNegocioException("%s nao pode ser negativo".formatted(campo));
        }
        return valor;
    }

    protected static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("%s e obrigatorio".formatted(campo));
        }
        return valor.trim();
    }

    // --- acesso ---

    public Long          getId()            { return id; }
    public String        getNome()          { return nome; }
    public BigDecimal    getQuantidade()    { return quantidade; }
    public UnidadeMedida getUnidadeMedida() { return unidadeMedida; }
    public BigDecimal    getEstoqueMinimo() { return estoqueMinimo; }
}
