package br.com.atelie.catalogo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AtualizarCustoRequest(
        @NotNull @DecimalMin("0.0000") @Digits(integer = 10, fraction = 4) BigDecimal custoUnitario
) { }
