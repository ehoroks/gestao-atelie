package br.com.atelie.venda.dto;

import jakarta.validation.constraints.Positive;

/** Venda pode ser aberta sem cliente (venda de balcao). */
public record AberturaVendaRequest(

        @Positive(message = "clienteId deve ser positivo quando informado")
        Long clienteId
) {
}
