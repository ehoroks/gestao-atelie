package br.com.atelie.aplicacao;

import br.com.atelie.encomenda.Encomenda;
import br.com.atelie.encomenda.SituacaoEncomenda;

import br.com.atelie.catalogo.ComposicaoPreco;
import br.com.atelie.comum.EstoqueInsuficienteException;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.comum.TransicaoInvalidaException;
import br.com.atelie.estoque.UnidadeMedida;
import br.com.atelie.fixtures.InsumoFake;
import br.com.atelie.fixtures.ProdutoFake;
import br.com.atelie.fixtures.ServicoEstoqueFake;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServicoEncomendaTest {
    private static BigDecimal d(String x) { return new BigDecimal(x); }

    private Encomenda encomenda(ProdutoFake produto) {
        return new Encomenda(1L, produto, "Pedido especial", d("50"),
                LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 8), null);
    }

    @Test void consomeInsumosAoEntrarNaProducaoEAgrupaRepetidos() {
        var p = new ProdutoFake("Vestido", d("50"));
        var tecido = new InsumoFake("Tecido", UnidadeMedida.M, d("10"), d("5"));
        var comp = new ComposicaoPreco(p, BigDecimal.ZERO, BigDecimal.ZERO);
        comp.adicionarMaterial(tecido, d("1.5"));
        comp.adicionarMaterial(tecido, d("1.0"));
        var estoque = new ServicoEstoqueFake();
        var servico = new ServicoEncomenda(estoque);
        var e = encomenda(p);
        servico.atualizarSituacao(e, SituacaoEncomenda.EM_PRODUCAO, comp);
        assertEquals(SituacaoEncomenda.EM_PRODUCAO, e.getSituacao());
        assertEquals(1, estoque.chamadasBaixar());
        assertEquals(1, estoque.ultimasLinhas().size());
        assertEquals(0, d("2.5").compareTo(estoque.ultimasLinhas().get(0).quantidade()));
        assertEquals(0, d("2.5").compareTo(estoque.saldo(tecido)));
        servico.atualizarSituacao(e, SituacaoEncomenda.PRONTA, null);
        assertEquals(1, estoque.chamadasBaixar());
    }

    @Test void faltaDeEstoqueNaoAlteraSituacao() {
        var p = new ProdutoFake("Vestido", d("50"));
        var tecido = new InsumoFake("Tecido", UnidadeMedida.M, d("10"), d("1"));
        var comp = new ComposicaoPreco(p, BigDecimal.ZERO, BigDecimal.ZERO);
        comp.adicionarMaterial(tecido, d("2"));
        var estoque = new ServicoEstoqueFake();
        var e = encomenda(p);
        assertThrows(EstoqueInsuficienteException.class,
                () -> new ServicoEncomenda(estoque)
                        .atualizarSituacao(e, SituacaoEncomenda.EM_PRODUCAO, comp));
        assertEquals(SituacaoEncomenda.REGISTRADA, e.getSituacao());
        assertEquals(d("1"), estoque.saldo(tecido));
    }

    @Test void transicaoInvalidaNaoInvocaEstoque() {
        var estoque = new ServicoEstoqueFake();
        var e = encomenda(new ProdutoFake("Vestido", d("50")));
        assertThrows(TransicaoInvalidaException.class,
                () -> new ServicoEncomenda(estoque)
                        .atualizarSituacao(e, SituacaoEncomenda.ENTREGUE, null));
        assertEquals(0, estoque.chamadasBaixar());
    }

    @Test void composicaoInvalidaNaoConsomeMateriais() {
        var p = new ProdutoFake("Vestido", d("50"));
        var estoque = new ServicoEstoqueFake();
        var servico = new ServicoEncomenda(estoque);
        var e = encomenda(p);
        assertThrows(RegraNegocioException.class,
                () -> servico.atualizarSituacao(e, SituacaoEncomenda.EM_PRODUCAO, null));
        assertThrows(RegraNegocioException.class,
                () -> servico.atualizarSituacao(e, SituacaoEncomenda.EM_PRODUCAO,
                        new ComposicaoPreco(p, BigDecimal.ZERO, BigDecimal.ZERO)));
        assertEquals(0, estoque.chamadasBaixar());
    }
    @Test void baixaDeVariosMateriaisNaoPodeSerParcial() {
        var p = new ProdutoFake("Vestido", d("50"));
        var tecido = new InsumoFake("Tecido", UnidadeMedida.M, d("10"), d("5"));
        var linha = new InsumoFake("Linha", UnidadeMedida.G, d("1"), d("1"));
        var comp = new ComposicaoPreco(p, BigDecimal.ZERO, BigDecimal.ZERO);
        comp.adicionarMaterial(tecido, d("2"));
        comp.adicionarMaterial(linha, d("3"));
        var estoque = new ServicoEstoqueFake();
        var e = encomenda(p);
        assertThrows(EstoqueInsuficienteException.class,
                () -> new ServicoEncomenda(estoque).atualizarSituacao(e, SituacaoEncomenda.EM_PRODUCAO, comp));
        assertEquals(d("5"), estoque.saldo(tecido));
        assertEquals(d("1"), estoque.saldo(linha));
        assertEquals(SituacaoEncomenda.REGISTRADA, e.getSituacao());
    }

}
