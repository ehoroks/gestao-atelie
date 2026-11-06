package br.com.atelie.venda.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Desconto absoluto em reais (ambiguidade 2 resolvida como valor, nao percentual). */
public record AplicarDescontoRequest(

        @NotNull(message = "valor e obrigatorio")
        @DecimalMin(value = "0.00", message = "valor nao pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "valor deve ter no maximo 2 casas decimais")
        BigDecimal valor
) {
}
