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
        BigDecimal subtotal,
        BigDecimal valorFinal,
        FormaPagamento formaPagamento,
        LocalDateTime criadoEm,
        LocalDateTime canceladaEm,
        String motivoCancelamento,
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
                venda.calcularSubtotal(),
                venda.calcularValorFinal(),
                venda.getFormaPagamento(),
                venda.getCriadoEm(),
                venda.getCanceladaEm(),
                venda.getMotivoCancelamento(),
                itens);
    }
}
