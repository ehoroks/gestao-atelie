package br.com.atelie.venda.dto;

import br.com.atelie.venda.FormaPagamento;
import br.com.atelie.venda.SituacaoVenda;
import br.com.atelie.venda.Venda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VendaResponse(
        Long id,
        Long clienteId,
        SituacaoVenda situacao,
        BigDecimal desconto,
        FormaPagamento formaPagamento,
        LocalDateTime criadoEm,
        List<ItemVendaResponse> itens
) {

    public static VendaResponse de(Venda venda) {
        List<ItemVendaResponse> itens = venda.getItens().stream()
                .map(ItemVendaResponse::de)
                .toList();
        return new VendaResponse(
                venda.getId(),
                venda.getClienteId(),
                venda.getSituacao(),
                venda.getDesconto(),
                venda.getFormaPagamento(),
                venda.getCriadoEm(),
                itens);
    }
}
