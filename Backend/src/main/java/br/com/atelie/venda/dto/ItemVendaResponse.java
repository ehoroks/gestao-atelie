package br.com.atelie.venda.dto;

import br.com.atelie.venda.ItemVenda;

import java.math.BigDecimal;

public record ItemVendaResponse(
        Long id,
        Long produtoId,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {

    public static ItemVendaResponse de(ItemVenda item) {
        return new ItemVendaResponse(
                item.getId(),
                item.getProdutoId(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.calcularSubtotal());
    }
}
