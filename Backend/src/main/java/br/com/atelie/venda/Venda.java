package br.com.atelie.venda;

import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.comum.TransicaoInvalidaException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Trilha B - B4. Agregado de venda.
 *
 * Regra central: so e permitido adicionar/remover itens enquanto a venda esta
 * em {@link SituacaoVenda#EM_ABERTO}. Apos finalizar/cancelar, qualquer tentativa
 * lanca {@link VendaNaoEditavelException}.
 *
 * A venda pode existir sem cliente (venda de balcao), conforme a multiplicidade
 * 0..1 do modelo; por isso `clienteId` e anulavel.
 */
@Entity
@Table(name = "vendas")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 20)
    private SituacaoVenda situacao = SituacaoVenda.EM_ABERTO;

    /** Desconto absoluto (em reais), nunca percentual. */
    @Column(name = "desconto", nullable = false, precision = 12, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", length = 20)
    private FormaPagamento formaPagamento;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "cancelada_em")
    private LocalDateTime canceladaEm;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVenda> itens = new ArrayList<>();

    /** Exigido pelo mapeamento ORM. */
    protected Venda() {
    }

    public Venda(Long clienteId) {
        if (clienteId != null && clienteId <= 0) {
            throw new RegraNegocioException("clienteId deve ser positivo quando informado");
        }
        this.clienteId = clienteId;
    }

    @PrePersist
    void aoPersistir() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    // --- operacoes do agregado (B4) ---

    public ItemVenda adicionarItem(Long produtoId, BigDecimal quantidade,
                                   BigDecimal precoAtualDoProduto) {
        exigirVendaEmAberto();
        ItemVenda item = ItemVenda.comPrecoCongelado(this, produtoId, quantidade, precoAtualDoProduto);
        itens.add(item);
        return item;
    }

    public void removerItem(Long itemId) {
        exigirVendaEmAberto();
        boolean removido = itens.removeIf(item -> Objects.equals(item.getId(), itemId));
        if (!removido) {
            throw new RecursoNaoEncontradoException("ItemVenda", itemId);
        }
    }

    /**
     * B5: desconto absoluto em reais. Nao pode ser negativo nem exceder o
     * subtotal atual dos itens. A API nao aceita desconto percentual.
     */
    public void aplicarDesconto(BigDecimal valor) {
        exigirVendaEmAberto();
        if (valor == null || valor.signum() < 0) {
            throw new RegraNegocioException("desconto nao pode ser negativo");
        }
        BigDecimal normalizado = valor.setScale(2, RoundingMode.HALF_UP);
        if (normalizado.compareTo(calcularSubtotal()) > 0) {
            throw new RegraNegocioException("desconto nao pode exceder o subtotal da venda");
        }
        this.desconto = normalizado;
    }

    /** B5: forma de pagamento so pode ser definida com a venda em aberto. */
    public void definirFormaPagamento(FormaPagamento formaPagamento) {
        exigirVendaEmAberto();
        this.formaPagamento = Objects.requireNonNull(
                formaPagamento, "forma de pagamento e obrigatoria");
    }

    /**
     * B7: finaliza a venda. Exige venda em aberto, pelo menos um item e forma
     * de pagamento definida; depois transiciona para CONCLUIDA.
     *
     * A baixa de estoque (I1) NAO acontece aqui: ela e coordenada por
     * `ServicoVenda` atraves da porta `EstoqueDaVenda`, para nunca haver
     * chamada direta de entidade para entidade.
     */
    public void finalizar() {
        validarFinalizacao();
        alterarSituacao(SituacaoVenda.CONCLUIDA);
    }

    /** Valida as precondicoes sem alterar o estado, para a orquestracao do estoque. */
    public void validarFinalizacao() {
        if (situacao != SituacaoVenda.EM_ABERTO) {
            throw new TransicaoInvalidaException(situacao, SituacaoVenda.CONCLUIDA);
        }
        if (itens.isEmpty()) {
            throw new RegraNegocioException("venda nao pode ser finalizada sem itens");
        }
        if (formaPagamento == null) {
            throw new RegraNegocioException(
                    "venda nao pode ser finalizada sem forma de pagamento");
        }
    }

    /** B8: registra o cancelamento depois do estorno coordenado pelo servico. */
    public void cancelar(LocalDateTime data, String motivo) {
        validarCancelamento(data, motivo);
        alterarSituacao(SituacaoVenda.CANCELADA);
        this.canceladaEm = data;
        this.motivoCancelamento = motivo.trim();
    }

    /** Valida o cancelamento sem mudar o agregado, antes de estornar o estoque. */
    public void validarCancelamento(LocalDateTime data, String motivo) {
        if (situacao != SituacaoVenda.CONCLUIDA) {
            throw new TransicaoInvalidaException(situacao, SituacaoVenda.CANCELADA);
        }
        if (data == null) {
            throw new RegraNegocioException("data do cancelamento e obrigatoria");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("motivo do cancelamento e obrigatorio");
        }
        if (motivo.trim().length() > 500) {
            throw new RegraNegocioException("motivo do cancelamento deve ter no maximo 500 caracteres");
        }
    }

    private void exigirVendaEmAberto() {
        if (!situacao.permiteEdicaoDeItens()) {
            throw new VendaNaoEditavelException(situacao);
        }
    }

    // --- calculos (B6) ---

    /** Soma dos subtotais dos itens (preco congelado x quantidade). */
    public BigDecimal calcularSubtotal() {
        return itens.stream()
                .map(ItemVenda::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * B6: subtotal - desconto, com piso em zero.
     *
     * O piso e necessario porque o desconto e validado no momento da aplicacao,
     * mas itens podem ser removidos depois (venda continua em aberto), deixando o
     * subtotal menor que o desconto ja registrado.
     */
    public BigDecimal calcularValorFinal() {
        BigDecimal valorFinal = calcularSubtotal().subtract(desconto);
        return valorFinal.signum() < 0 ? BigDecimal.ZERO : valorFinal;
    }

    /**
     * Transicao de estado interna; as operacoes publicas validam os dados e
     * coordenam as integracoes antes de chegar aqui.
     */
    void alterarSituacao(SituacaoVenda destino) {
        if (!situacao.podeTransicionarPara(destino)) {
            throw new TransicaoInvalidaException(situacao, destino);
        }
        situacao = destino;
    }

    // --- acesso ---

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public SituacaoVenda getSituacao() { return situacao; }
    public BigDecimal getDesconto() { return desconto; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getCanceladaEm() { return canceladaEm; }
    public String getMotivoCancelamento() { return motivoCancelamento; }

    public List<ItemVenda> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
