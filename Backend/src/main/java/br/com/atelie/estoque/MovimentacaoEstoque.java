package br.com.atelie.estoque;

import br.com.atelie.comum.RegraNegocioException;
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
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro imutavel. Historico e append-only: nao existe setter nem delete.
 * Criado exclusivamente por ItemEstoque.
 */
@Entity
@Table(name = "movimentacoes_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, updatable = false)
    private ItemEstoque item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private TipoMovimentacao tipo;

    @Column(nullable = false, updatable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "saldo_resultante", nullable = false, updatable = false, precision = 12, scale = 3)
    private BigDecimal saldoResultante;

    @Column(name = "data", nullable = false, updatable = false)
    private LocalDateTime data;

    @Column(length = 500, updatable = false)
    private String motivo;

    protected MovimentacaoEstoque() {
    }

    MovimentacaoEstoque(ItemEstoque item,
                        TipoMovimentacao tipo,
                        BigDecimal quantidade,
                        BigDecimal saldoResultante,
                        String motivo) {
        if (tipo == TipoMovimentacao.AJUSTE && (motivo == null || motivo.isBlank())) {
            throw new RegraNegocioException("Ajuste de estoque exige motivo");
        }
        this.item            = Objects.requireNonNull(item);
        this.tipo            = Objects.requireNonNull(tipo);
        this.quantidade      = Objects.requireNonNull(quantidade);
        this.saldoResultante = Objects.requireNonNull(saldoResultante);
        this.motivo          = motivo;
        this.data            = LocalDateTime.now();
    }

    public Long             getId()              { return id; }
    public ItemEstoque      getItem()            { return item; }
    public TipoMovimentacao getTipo()            { return tipo; }
    public BigDecimal       getQuantidade()      { return quantidade; }
    public BigDecimal       getSaldoResultante() { return saldoResultante; }
    public LocalDateTime    getData()            { return data; }
    public String           getMotivo()          { return motivo; }
}
