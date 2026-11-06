package br.com.atelie.encomenda;

import br.com.atelie.catalogo.Insumo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(name = "encomenda_insumos",
        uniqueConstraints = @UniqueConstraint(name = "uk_encomenda_insumo", columnNames = {"encomenda_id", "insumo_id"}))
public class EncomendaInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false, updatable = false)
    private Encomenda encomenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "insumo_id", nullable = false, updatable = false)
    private Insumo insumo;

    @Column(nullable = false, precision = 12, scale = 3, updatable = false)
    private BigDecimal quantidade;

    protected EncomendaInsumo() {
    }

    EncomendaInsumo(Encomenda encomenda, Insumo insumo, BigDecimal quantidade) {
        this.encomenda = encomenda;
        this.insumo = insumo;
        this.quantidade = quantidade;
    }

    public Long getId() { return id; }
    public Encomenda getEncomenda() { return encomenda; }
    public Insumo getInsumo() { return insumo; }
    public BigDecimal getQuantidade() { return quantidade; }
}
