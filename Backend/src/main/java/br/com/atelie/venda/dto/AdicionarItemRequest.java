package br.com.atelie.venda.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * `precoUnitario` e temporariamente informado pelo cliente da API porque a
 * Trilha A ainda nao expoe um `ProdutoRepository`. Na integracao I1 ele passa a
 * ser lido de `Produto.preco` no servidor e nunca aceito do request.
 */
public record AdicionarItemRequest(

        @NotNull(message = "produtoId e obrigatorio")
        @Positive(message = "produtoId deve ser positivo")
        Long produtoId,

        @NotNull(message = "quantidade e obrigatoria")
        @DecimalMin(value = "0.001", message = "quantidade deve ser maior que zero")
        @Digits(integer = 9, fraction = 3, message = "quantidade deve ter no maximo 3 casas decimais")
        BigDecimal quantidade,

        @NotNull(message = "precoUnitario e obrigatorio")
        @DecimalMin(value = "0.00", message = "precoUnitario nao pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "precoUnitario deve ter no maximo 2 casas decimais")
        BigDecimal precoUnitario
) {
}
