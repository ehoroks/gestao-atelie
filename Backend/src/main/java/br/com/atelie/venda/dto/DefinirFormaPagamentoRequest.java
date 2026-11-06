package br.com.atelie.venda.dto;

import br.com.atelie.venda.FormaPagamento;
import jakarta.validation.constraints.NotNull;

public record DefinirFormaPagamentoRequest(

        @NotNull(message = "formaPagamento e obrigatoria")
        FormaPagamento formaPagamento
) {
}
