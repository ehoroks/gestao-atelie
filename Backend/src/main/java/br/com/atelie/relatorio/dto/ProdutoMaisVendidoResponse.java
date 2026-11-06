package br.com.atelie.relatorio.dto;

import java.math.BigDecimal;

public record ProdutoMaisVendidoResponse(
        Long produtoId,
        BigDecimal quantidadeVendida,
        BigDecimal valorVendido
) {
}
