package br.com.atelie.catalogo;

import br.com.atelie.estoque.ItemEstoque;
import br.com.atelie.estoque.UnidadeMedida;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "insumos")
@DiscriminatorValue("INSUMO")
public class Insumo extends ItemEstoque {

    @Column(name = "custo_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal custoUnitario;

    protected Insumo() { }

    public Insumo(String nome, UnidadeMedida unidadeMedida, BigDecimal estoqueMinimo,
                  BigDecimal custoUnitario) {
        super(nome, unidadeMedida, estoqueMinimo);
        this.custoUnitario = exigirNaoNegativo(custoUnitario, "custoUnitario");
    }

    public void atualizarCustoUnitario(BigDecimal custoUnitario) {
        this.custoUnitario = exigirNaoNegativo(custoUnitario, "custoUnitario");
    }

    public BigDecimal getCustoUnitario() { return custoUnitario; }
}
