package br.com.atelie.encomenda;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EncomendaRequest(
        @NotNull Long clienteId,
        Long produtoId,
        @NotBlank @Size(max = 500) String descricao,
        @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal valor,
        LocalDate dataPedido,
        @NotNull @FutureOrPresent LocalDate dataPrevistaEntrega,
        @Size(max = 1000) String observacoes,
        List<@Valid InsumoConsumo> insumos
) {

    public record InsumoConsumo(
            @NotNull Long insumoId,
            @NotNull @DecimalMin("0.001") @Digits(integer = 9, fraction = 3) BigDecimal quantidade
    ) {
    }
}
