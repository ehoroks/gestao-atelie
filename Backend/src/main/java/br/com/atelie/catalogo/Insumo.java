package br.com.atelie.catalogo;

import br.com.atelie.estoque.ItemEstoque;
import br.com.atelie.estoque.UnidadeMedida;

import java.math.BigDecimal;

public class Insumo extends ItemEstoque {

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
