package br.com.atelie.encomenda;

import jakarta.validation.constraints.NotNull;

public record AtualizarSituacaoRequest(@NotNull SituacaoEncomenda situacao) {
}
