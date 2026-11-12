package br.com.atelie.estoque.dto;

import br.com.atelie.estoque.MovimentacaoEstoque;
import br.com.atelie.estoque.TipoMovimentacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoResponse(Long id, Long itemId, TipoMovimentacao tipo,
                                   BigDecimal quantidade, BigDecimal saldoResultante,
                                   LocalDateTime data, String motivo) {
    public static MovimentacaoResponse de(MovimentacaoEstoque m) {
        return new MovimentacaoResponse(m.getId(), m.getItem().getId(), m.getTipo(),
                m.getQuantidade(), m.getSaldoResultante(), m.getData(), m.getMotivo());
    }
}
