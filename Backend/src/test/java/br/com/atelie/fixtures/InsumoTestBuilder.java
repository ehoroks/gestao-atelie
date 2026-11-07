package br.com.atelie.fixtures;

import br.com.atelie.catalogo.Insumo;
import br.com.atelie.catalogo.InsumoRepository;
import br.com.atelie.estoque.UnidadeMedida;

import java.math.BigDecimal;

public class InsumoTestBuilder {

    private String nome = "Insumo de teste";
    private UnidadeMedida unidade = UnidadeMedida.M;
    private BigDecimal estoqueMinimo = BigDecimal.ZERO;
    private BigDecimal quantidade = BigDecimal.ZERO;
    private BigDecimal custoUnitario = new BigDecimal("0.50");

    public static InsumoTestBuilder umInsumo() {
        return new InsumoTestBuilder();
    }

    public InsumoTestBuilder comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public InsumoTestBuilder comUnidade(UnidadeMedida unidade) {
        this.unidade = unidade;
        return this;
    }

    public InsumoTestBuilder comEstoqueMinimo(String estoqueMinimo) {
        this.estoqueMinimo = new BigDecimal(estoqueMinimo);
        return this;
    }

    public InsumoTestBuilder comQuantidade(String quantidade) {
        this.quantidade = new BigDecimal(quantidade);
        return this;
    }

    public InsumoTestBuilder comCustoUnitario(String custoUnitario) {
        this.custoUnitario = new BigDecimal(custoUnitario);
        return this;
    }

    public Insumo build() {
        Insumo insumo = new Insumo(nome, unidade, estoqueMinimo, custoUnitario);
        if (quantidade.signum() > 0) {
            insumo.registrarEntrada(quantidade);
        }
        return insumo;
    }

    public Insumo persistir(InsumoRepository insumos) {
        return insumos.saveAndFlush(build());
    }
}
