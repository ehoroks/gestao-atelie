package br.com.atelie.venda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CancelarVendaRequest(
        @NotNull LocalDateTime data,
        @NotBlank @Size(max = 500) String motivo
) {
}
