package br.com.atelie.estoque;

import br.com.atelie.comum.EstoqueInsuficienteException;
import br.com.atelie.comum.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Implementacao transacional do contrato de movimentacao de estoque. */
@Service
public class ServicoEstoqueJpa implements ServicoEstoque {

    @Override
    @Transactional(readOnly = true)
    public boolean haDisponibilidade(List<Linha> linhas) {
        Map<ItemEstoque, BigDecimal> pedidos = agrupar(linhas);
        return pedidos.entrySet().stream()
                .allMatch(pedido -> pedido.getKey().temEstoqueSuficiente(pedido.getValue()));
    }

    @Override
    @Transactional
    public synchronized void baixar(List<Linha> linhas, String motivo) {
        Map<ItemEstoque, BigDecimal> pedidos = agrupar(linhas);
        validarDisponibilidade(pedidos);
        pedidos.forEach((item, quantidade) -> item.registrarSaida(quantidade, motivo));
    }

    @Override
    @Transactional
    public synchronized void estornar(List<Linha> linhas, String motivo) {
        Map<ItemEstoque, BigDecimal> pedidos = agrupar(linhas);
        pedidos.forEach((item, quantidade) -> item.registrarEntrada(quantidade, motivo));
    }

    private static Map<ItemEstoque, BigDecimal> agrupar(List<Linha> linhas) {
        if (linhas == null || linhas.isEmpty()) {
            throw new RegraNegocioException("a movimentacao deve conter ao menos uma linha");
        }
        Map<ItemEstoque, BigDecimal> pedidos = new IdentityHashMap<>();
        for (Linha linha : linhas) {
            if (linha == null || linha.item() == null || linha.quantidade() == null
                    || linha.quantidade().signum() <= 0) {
                throw new RegraNegocioException("linha de estoque invalida");
            }
            linha.item().validarQuantidadePositiva(linha.quantidade());
            pedidos.merge(linha.item(), linha.quantidade(), BigDecimal::add);
        }
        return pedidos;
    }

    private static void validarDisponibilidade(Map<ItemEstoque, BigDecimal> pedidos) {
        for (Map.Entry<ItemEstoque, BigDecimal> pedido : pedidos.entrySet()) {
            ItemEstoque item = pedido.getKey();
            BigDecimal quantidade = pedido.getValue();
            if (!item.temEstoqueSuficiente(quantidade)) {
                throw new EstoqueInsuficienteException(
                        item.getNome(), item.getQuantidade(), quantidade);
            }
        }
    }
}
