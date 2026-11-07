package br.com.atelie.encomenda;

import br.com.atelie.catalogo.Produto;
import br.com.atelie.catalogo.Insumo;
import br.com.atelie.cliente.Cliente;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.comum.TransicaoInvalidaException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Estado de dominio puro. O consumo de insumos e responsabilidade do
 * ServicoEncomenda, nao desta entidade (integracao I2).
 */
@Entity
@Table(name = "encomendas")
public class Encomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, updatable = false)
    private Cliente cliente;

    /** Compatibilidade com os testes de dominio anteriores a I4; nao e persistido. */
    @jakarta.persistence.Transient
    private Long clienteIdLegado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", updatable = false)
    private Produto produto;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_pedido", nullable = false)
    private LocalDate dataPedido;

    @Column(name = "data_prevista_entrega", nullable = false)
    private LocalDate previsaoEntrega;

    @Column(length = 1000)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoEncomenda situacao = SituacaoEncomenda.REGISTRADA;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "concluido_em")
    private LocalDateTime concluidoEm;

    @Column(name = "insumos_consumidos", nullable = false)
    private boolean insumosConsumidos;

    @Version
    @Column(name = "versao", nullable = false)
    private long versao;

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EncomendaInsumo> insumos = new ArrayList<>();

    protected Encomenda() {
    }

    public Encomenda(Long clienteId, Produto produto, String descricao, BigDecimal valor,
                     LocalDate dataPedido, LocalDate previsaoEntrega, String observacoes) {
        if (clienteId == null || clienteId <= 0) {
            throw new RegraNegocioException("clienteId deve ser positivo");
        }
        this.clienteIdLegado = clienteId;
        this.produto = produto;
        atribuirDados(descricao, valor, dataPedido, previsaoEntrega, observacoes);
    }

    public Encomenda(Cliente cliente, Produto produto, String descricao, BigDecimal valor,
                     LocalDate dataPedido, LocalDate previsaoEntrega, String observacoes) {
        this.cliente = Objects.requireNonNull(cliente, "cliente e obrigatorio");
        if (cliente.getId() != null && cliente.getId() <= 0) {
            throw new RegraNegocioException("clienteId deve ser positivo");
        }
        this.produto = produto;
        atribuirDados(descricao, valor, dataPedido, previsaoEntrega, observacoes);
    }

    private void atribuirDados(String descricao, BigDecimal valor, LocalDate dataPedido,
                               LocalDate previsaoEntrega, String observacoes) {
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

    @PrePersist
    void aoPersistir() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public void adicionarInsumo(Insumo insumo, BigDecimal quantidade) {
        Objects.requireNonNull(insumo, "insumo e obrigatorio");
        if (quantidade == null || quantidade.signum() <= 0
                || !insumo.getUnidadeMedida().aceita(quantidade)) {
            throw new RegraNegocioException("quantidade de insumo invalida");
        }
        boolean repetido = insumos.stream().anyMatch(item ->
                item.getInsumo() == insumo || (item.getInsumo().getId() != null
                        && item.getInsumo().getId().equals(insumo.getId())));
        if (repetido) {
            throw new RegraNegocioException("insumo repetido na encomenda: " + insumo.getNome());
        }
        insumos.add(new EncomendaInsumo(this, insumo, quantidade));
    }

    /** Nao e setter livre: aplica as transicoes previstas em SituacaoEncomenda. */
    public void atualizarSituacao(SituacaoEncomenda novaSituacao) {
        if (!situacao.podeTransicionarPara(novaSituacao)) {
            throw new TransicaoInvalidaException(situacao, novaSituacao);
        }
        situacao = novaSituacao;
        if (novaSituacao.isTerminal()) {
            concluidoEm = LocalDateTime.now();
        }
    }

    public void marcarInsumosConsumidos() {
        if (!situacao.consomeInsumos()) {
            throw new RegraNegocioException("insumos so podem ser marcados ao iniciar producao");
        }
        insumosConsumidos = true;
    }

    public boolean estaPendente() { return situacao.isPendente(); }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Long getClienteId() { return cliente != null ? cliente.getId() : clienteIdLegado; }
    public Produto getProduto() { return produto; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getDataPedido() { return dataPedido; }
    public LocalDate getPrevisaoEntrega() { return previsaoEntrega; }
    public String getObservacoes() { return observacoes; }
    public SituacaoEncomenda getSituacao() { return situacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getConcluidoEm() { return concluidoEm; }
    public boolean isInsumosConsumidos() { return insumosConsumidos; }
    public long getVersao() { return versao; }
    public List<EncomendaInsumo> getInsumos() { return Collections.unmodifiableList(insumos); }
}
