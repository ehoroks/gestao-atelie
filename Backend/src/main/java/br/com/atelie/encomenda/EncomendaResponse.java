package br.com.atelie.encomenda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record EncomendaResponse(
        Long id,
        Long clienteId,
        String clienteNome,
        Long produtoId,
        String produtoNome,
        String descricao,
        BigDecimal valor,
        SituacaoEncomenda situacao,
        boolean pendente,
        LocalDate dataPedido,
        LocalDate dataPrevistaEntrega,
        String observacoes,
        LocalDateTime criadoEm,
        LocalDateTime concluidoEm,
        boolean insumosConsumidos,
        List<InsumoItem> insumos
) {

    public record InsumoItem(Long insumoId, String nome, BigDecimal quantidade) { }

    public static EncomendaResponse de(Encomenda encomenda) {
        return new EncomendaResponse(
                encomenda.getId(),
                encomenda.getClienteId(),
                encomenda.getCliente() == null ? null : encomenda.getCliente().getNome(),
                encomenda.getProduto() == null ? null : encomenda.getProduto().getId(),
                encomenda.getProduto() == null ? null : encomenda.getProduto().getNome(),
                encomenda.getDescricao(),
                encomenda.getValor(),
                encomenda.getSituacao(),
                encomenda.estaPendente(),
                encomenda.getDataPedido(),
                encomenda.getPrevisaoEntrega(),
                encomenda.getObservacoes(),
                encomenda.getCriadoEm(),
                encomenda.getConcluidoEm(),
                encomenda.isInsumosConsumidos(),
                encomenda.getInsumos().stream()
                        .map(item -> new InsumoItem(item.getInsumo().getId(), item.getInsumo().getNome(),
                                item.getQuantidade()))
                        .toList());
    }
}
