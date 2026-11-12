package br.com.atelie.estoque.dto;

import br.com.atelie.catalogo.Produto;
import br.com.atelie.estoque.ItemEstoque;
import br.com.atelie.estoque.UnidadeMedida;
import java.math.BigDecimal;

public record EstoqueItemResponse(Long id, String nome, String tipo, UnidadeMedida unidadeMedida,
                                  BigDecimal quantidade, BigDecimal estoqueMinimo, boolean estoqueBaixo) {
    public static EstoqueItemResponse de(ItemEstoque item) {
        return new EstoqueItemResponse(item.getId(), item.getNome(), item instanceof Produto ? "PRODUTO" : "INSUMO",
                item.getUnidadeMedida(), item.getQuantidade(), item.getEstoqueMinimo(), item.estaComEstoqueBaixo());
    }
}
