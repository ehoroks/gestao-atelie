package br.com.atelie.estoque;

import br.com.atelie.comum.RegraNegocioException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * CONTRATO CONGELADO - Sprint 0.
 * Estado, assinaturas e validacoes de entrada sao definitivos.
 * Os corpos marcados com TODO-T1 pertencem a Trilha 1 (atividades A1/A2).
 *
 * Nenhuma subclasse altera 'quantidade' diretamente: toda mudanca passa
 * pelos tres metodos abaixo, que sao os unicos que geram movimentacao.
 */
public abstract class ItemEstoque {

    protected Long          id;
    protected String        nome;
    protected BigDecimal    quantidade    = BigDecimal.ZERO;
    protected UnidadeMedida unidadeMedida;
    protected BigDecimal    estoqueMinimo = BigDecimal.ZERO;

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
        validarQuantidadePositiva(quantidade);
        throw new UnsupportedOperationException("TODO-T1 (A1): registrarEntrada");
    }

    /** Subtrai do saldo e registra SAIDA. Nunca deixa o saldo negativo. */
    public void registrarSaida(BigDecimal quantidade) {
        validarQuantidadePositiva(quantidade);
        throw new UnsupportedOperationException("TODO-T1 (A1): registrarSaida");
    }

    /** Sobrescreve o saldo e registra AJUSTE. Motivo obrigatorio. */
    public void ajustarQuantidade(BigDecimal novaQuantidade, String motivo) {
        exigirNaoNegativo(novaQuantidade, "novaQuantidade");
        validarUnidade(novaQuantidade);
        exigirTexto(motivo, "motivo");
        throw new UnsupportedOperationException("TODO-T1 (A1): ajustarQuantidade");
    }

    public boolean temEstoqueSuficiente(BigDecimal quantidade) {
        validarQuantidadePositiva(quantidade);
        throw new UnsupportedOperationException("TODO-T1 (A2): temEstoqueSuficiente");
    }

    /** AMBIGUIDADE 9: saldo == estoqueMinimo conta como baixo? Decidir no Sprint 0. */
    public boolean estaComEstoqueBaixo() {
        throw new UnsupportedOperationException("TODO-T1 (A2): estaComEstoqueBaixo");
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
