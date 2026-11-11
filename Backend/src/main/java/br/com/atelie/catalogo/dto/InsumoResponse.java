package br.com.atelie.catalogo.dto;

import br.com.atelie.catalogo.Insumo;
import br.com.atelie.estoque.UnidadeMedida;
import java.math.BigDecimal;

public record InsumoResponse(
        Long id, String nome, UnidadeMedida unidadeMedida,
        BigDecimal quantidade, BigDecimal estoqueMinimo, boolean estoqueBaixo,
        BigDecimal custoUnitario
) {
    public static InsumoResponse de(Insumo insumo) {
        return new InsumoResponse(insumo.getId(), insumo.getNome(), insumo.getUnidadeMedida(),
                insumo.getQuantidade(), insumo.getEstoqueMinimo(),
                insumo.estaComEstoqueBaixo(), insumo.getCustoUnitario());
    }
}
