package br.com.atelie.relatorio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FaturamentoPeriodoResponse(
        LocalDate inicio,
        LocalDate fim,
        BigDecimal faturamento,
        long quantidadeVendas
) {
}
