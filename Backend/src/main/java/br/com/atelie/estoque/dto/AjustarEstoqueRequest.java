package br.com.atelie.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AjustarEstoqueRequest(
        @NotNull @DecimalMin("0.000") @Digits(integer = 9, fraction = 3) BigDecimal novaQuantidade,
        @NotBlank @Size(max = 500) String motivo
) { }
