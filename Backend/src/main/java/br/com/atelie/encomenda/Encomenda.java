package br.com.atelie.encomenda;

import br.com.atelie.catalogo.Produto;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.comum.TransicaoInvalidaException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Estado de dominio puro. O consumo de insumos e responsabilidade do
 * ServicoEncomenda, nao desta entidade (integracao I2).
 */
public class Encomenda {
    private Long id;
    private final Long clienteId;
    private final Produto produto;
    private final String descricao;
    private final BigDecimal valor;
    private final LocalDate dataPedido;
    private final LocalDate previsaoEntrega;
    private final String observacoes;
    private SituacaoEncomenda situacao = SituacaoEncomenda.REGISTRADA;

    public Encomenda(Long clienteId, Produto produto, String descricao, BigDecimal valor,
                     LocalDate dataPedido, LocalDate previsaoEntrega, String observacoes) {
        if (clienteId == null || clienteId <= 0) {
            throw new RegraNegocioException("clienteId deve ser positivo");
        }
        this.clienteId = clienteId;
        this.produto = Objects.requireNonNull(produto, "produto e obrigatorio");
        if (descricao == null || descricao.isBlank()) {
            throw new RegraNegocioException("descricao e obrigatoria");
        }
        this.descricao = descricao.trim();
        if (valor == null || valor.signum() < 0) {
            throw new RegraNegocioException("valor nao pode ser negativo");
        }
        this.valor = valor;
        this.dataPedido = Objects.requireNonNull(dataPedido, "dataPedido e obrigatoria");
        this.previsaoEntrega = Objects.requireNonNull(previsaoEntrega, "previsaoEntrega e obrigatoria");
        if (previsaoEntrega.isBefore(dataPedido)) {
            throw new RegraNegocioException("previsao de entrega anterior ao pedido");
        }
        this.observacoes = observacoes;
    }

    /** Nao e setter livre: aplica as transicoes previstas em SituacaoEncomenda. */
    public void atualizarSituacao(SituacaoEncomenda novaSituacao) {
        if (!situacao.podeTransicionarPara(novaSituacao)) {
            throw new TransicaoInvalidaException(situacao, novaSituacao);
        }
        situacao = novaSituacao;
    }

    public boolean estaPendente() { return situacao.isPendente(); }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public Produto getProduto() { return produto; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getDataPedido() { return dataPedido; }
    public LocalDate getPrevisaoEntrega() { return previsaoEntrega; }
    public String getObservacoes() { return observacoes; }
    public SituacaoEncomenda getSituacao() { return situacao; }
}
