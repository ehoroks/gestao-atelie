package br.com.atelie.venda;

import br.com.atelie.comum.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Trilha B - B3/B4. Item de uma venda.
 *
 * REGRA CENTRAL: `precoUnitario` e uma COPIA CONGELADA do preco do produto no
 * momento em que o item foi criado. Esta entidade guarda apenas `produtoId`
 * (fronteira temporaria, como `Encomenda.clienteId`), nunca uma referencia viva
 * a `Produto`; portanto, mudar o preco do produto depois NAO altera itens ja
 * existentes.
 *
 * A partir da B4 o item pertence ao agregado `Venda` (associacao JPA). As
 * operacoes de adicionar/remover passam pelos metodos de `Venda`, que bloqueiam
 * alteracoes quando a venda nao esta em aberto.
 */
@Entity
@Table(name = "itens_venda",
        indexes = @Index(name = "idx_itens_venda_venda", columnList = "venda_id"))
public class ItemVenda {

    private static final int ESCALA_QUANTIDADE = 3;
    private static final int ESCALA_MONETARIA   = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "quantidade", nullable = false, precision = 12, scale = ESCALA_QUANTIDADE)
    private BigDecimal quantidade;

    /** Preco congelado no momento da venda. Nao possui setter. */
    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = ESCALA_MONETARIA)
    private BigDecimal precoUnitario;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /** Exigido pelo mapeamento ORM. */
    protected ItemVenda() {
    }

    public ItemVenda(Venda venda, Long produtoId, BigDecimal quantidade, BigDecimal precoUnitario) {
        this.venda = Objects.requireNonNull(venda, "venda e obrigatoria");
        this.produtoId = exigirIdPositivo(produtoId, "produtoId");
        this.quantidade = validarQuantidade(quantidade);
        this.precoUnitario = validarPreco(precoUnitario);
    }

    /**
     * Cria o item a partir do preco atual do produto. O valor e copiado; a
     * entidade nao guarda nenhuma referencia ao produto.
     */
    public static ItemVenda comPrecoCongelado(Venda venda, Long produtoId,
                                              BigDecimal quantidade, BigDecimal precoAtualDoProduto) {
        return new ItemVenda(venda, produtoId, quantidade, precoAtualDoProduto);
    }

    @PrePersist
    void aoPersistir() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public BigDecimal calcularSubtotal() {
        return precoUnitario.multiply(quantidade).setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }

    // --- validacoes ---

    private static BigDecimal validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new RegraNegocioException("quantidade do item deve ser maior que zero");
        }
        if (quantidade.scale() > ESCALA_QUANTIDADE) {
            throw new RegraNegocioException(
                    "quantidade do item deve ter no maximo %d casas decimais".formatted(ESCALA_QUANTIDADE));
        }
        return quantidade;
    }

    private static BigDecimal validarPreco(BigDecimal preco) {
        if (preco == null || preco.signum() < 0) {
            throw new RegraNegocioException("preco unitario nao pode ser negativo");
        }
        return preco.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }

    private static Long exigirIdPositivo(Long valor, String campo) {
        if (valor == null || valor <= 0) {
            throw new RegraNegocioException("%s deve ser positivo".formatted(campo));
        }
        return valor;
    }

    // --- acesso ---

    public Long getId() { return id; }
    public Venda getVenda() { return venda; }
    public Long getProdutoId() { return produtoId; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
