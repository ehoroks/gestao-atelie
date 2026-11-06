package br.com.atelie.estoque;

import br.com.atelie.comum.EstoqueInsuficienteException;
import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.comum.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Implementacao transacional do contrato de movimentacao de estoque. */
@Service
public class ServicoEstoqueJpa implements ServicoEstoque {

    private final ItemEstoqueRepository itens;
    private final MovimentacaoEstoqueRepository movimentacoes;

    public ServicoEstoqueJpa(ItemEstoqueRepository itens,
                             MovimentacaoEstoqueRepository movimentacoes) {
        this.itens = itens;
        this.movimentacoes = movimentacoes;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean haDisponibilidade(List<Linha> linhas) {
        Map<Long, BigDecimal> quantidades = agruparPorId(linhas);
        Map<ItemEstoque, BigDecimal> pedidos = carregarPedidos(quantidades, false);
        return pedidos.entrySet().stream()
                .allMatch(pedido -> pedido.getKey().temEstoqueSuficiente(pedido.getValue()));
    }

    @Override
    @Transactional
    public void baixar(List<Linha> linhas, String motivo) {
        Map<Long, BigDecimal> quantidades = agruparPorId(linhas);
        Map<ItemEstoque, BigDecimal> pedidos = carregarPedidos(quantidades, true);
        validarDisponibilidade(pedidos);
        for (Map.Entry<ItemEstoque, BigDecimal> pedido : pedidos.entrySet()) {
            ItemEstoque item = pedido.getKey();
            int historicoAntes = item.getHistorico().size();
            item.registrarSaida(pedido.getValue(), motivo);
            MovimentacaoEstoque movimento = item.getHistorico().get(historicoAntes);
            movimentacoes.save(movimento);
        }
    }

    @Override
    @Transactional
    public void estornar(List<Linha> linhas, String motivo) {
        Map<Long, BigDecimal> quantidades = agruparPorId(linhas);
        Map<ItemEstoque, BigDecimal> pedidos = carregarPedidos(quantidades, true);
        for (Map.Entry<ItemEstoque, BigDecimal> pedido : pedidos.entrySet()) {
            ItemEstoque item = pedido.getKey();
            int historicoAntes = item.getHistorico().size();
            item.registrarEntrada(pedido.getValue(), motivo);
            MovimentacaoEstoque movimento = item.getHistorico().get(historicoAntes);
            movimentacoes.save(movimento);
        }
    }

    private Map<ItemEstoque, BigDecimal> carregarPedidos(Map<Long, BigDecimal> quantidades,
                                                         boolean bloquear) {
        Map<ItemEstoque, BigDecimal> pedidos = new LinkedHashMap<>();
        for (Map.Entry<Long, BigDecimal> pedido : quantidades.entrySet()) {
            ItemEstoque item = (bloquear
                    ? itens.buscarPorIdParaAtualizacao(pedido.getKey())
                    : itens.findById(pedido.getKey()))
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Item de estoque", pedido.getKey()));
            pedidos.put(item, pedido.getValue());
        }
        return pedidos;
    }

    private static Map<Long, BigDecimal> agruparPorId(List<Linha> linhas) {
        if (linhas == null || linhas.isEmpty()) {
            throw new RegraNegocioException("a movimentacao deve conter ao menos uma linha");
        }
        Map<Long, BigDecimal> pedidos = new TreeMap<>();
        for (Linha linha : linhas) {
            if (linha == null || linha.item() == null || linha.quantidade() == null
                    || linha.quantidade().signum() <= 0 || linha.item().getId() == null) {
                throw new RegraNegocioException("linha de estoque invalida");
            }
            linha.item().validarQuantidadePositiva(linha.quantidade());
            pedidos.merge(linha.item().getId(), linha.quantidade(), BigDecimal::add);
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
