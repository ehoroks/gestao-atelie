package br.com.atelie.fixtures;

import br.com.atelie.comum.EstoqueInsuficienteException;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.estoque.ItemEstoque;
import br.com.atelie.estoque.ServicoEstoque;
import java.math.BigDecimal;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Fake com prevalidacao de todas as linhas antes de qualquer baixa. */
public class ServicoEstoqueFake implements ServicoEstoque {
    private final Map<ItemEstoque, BigDecimal> saldos = new IdentityHashMap<>();
    private int chamadasBaixar;
    private List<Linha> ultimasLinhas = List.of();

    public void abastecer(ItemEstoque item, BigDecimal saldo) { saldos.put(item, saldo); }
    public BigDecimal saldo(ItemEstoque item) { return saldos.getOrDefault(item, item.getQuantidade()); }
    public int chamadasBaixar() { return chamadasBaixar; }
    public List<Linha> ultimasLinhas() { return ultimasLinhas; }

    private Map<ItemEstoque, BigDecimal> agrupar(List<Linha> linhas) {
        if (linhas == null || linhas.isEmpty()) throw new RegraNegocioException("sem linhas");
        Map<ItemEstoque, BigDecimal> pedidos = new IdentityHashMap<>();
        for (Linha linha : linhas) {
            if (linha == null || linha.item() == null || linha.quantidade() == null
                    || linha.quantidade().signum() <= 0) {
                throw new RegraNegocioException("linha invalida");
            }
            pedidos.merge(linha.item(), linha.quantidade(), BigDecimal::add);
        }
        return pedidos;
    }

    @Override public boolean haDisponibilidade(List<Linha> linhas) {
        for (var pedido : agrupar(linhas).entrySet()) {
            if (saldo(pedido.getKey()).compareTo(pedido.getValue()) < 0) return false;
        }
        return true;
    }

    @Override public void baixar(List<Linha> linhas, String motivo) {
        chamadasBaixar++;
        Map<ItemEstoque, BigDecimal> pedidos = agrupar(linhas);
        for (var pedido : pedidos.entrySet()) {
            if (saldo(pedido.getKey()).compareTo(pedido.getValue()) < 0) {
                throw new EstoqueInsuficienteException(pedido.getKey().getNome(),
                        saldo(pedido.getKey()), pedido.getValue());
            }
        }
        pedidos.forEach((item, qtd) -> saldos.put(item, saldo(item).subtract(qtd)));
        ultimasLinhas = List.copyOf(linhas);
    }

    @Override public void estornar(List<Linha> linhas, String motivo) {
        agrupar(linhas).forEach((item, qtd) -> saldos.put(item, saldo(item).add(qtd)));
    }
}
