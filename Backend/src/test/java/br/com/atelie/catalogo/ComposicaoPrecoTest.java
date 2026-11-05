package br.com.atelie.catalogo;

import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.estoque.UnidadeMedida;
import br.com.atelie.fixtures.InsumoFake;
import br.com.atelie.fixtures.ProdutoFake;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComposicaoPrecoTest {
    private static BigDecimal d(String n) { return new BigDecimal(n); }

    @Test void calculaMateriaisIndiretosELucro() {
        var p = new ProdutoFake("Vestido", d("120"));
        var c = new ComposicaoPreco(p, d("10"), d("20"));
        c.adicionarMaterial(new InsumoFake("Tecido", UnidadeMedida.M, d("10"), d("20")), d("1.5"));
        c.adicionarMaterial(new InsumoFake("Linha", UnidadeMedida.G, d("0.50"), d("20")), d("2"));
        assertEquals(0, d("16.00").compareTo(c.calcularCustoMateriais()));
        assertEquals(d("21.12"), c.calcularPrecoSugerido());
    }

    @Test void percentualZeroEhAceito() {
        var c = new ComposicaoPreco(new ProdutoFake("Blusa", d("50")), BigDecimal.ZERO, BigDecimal.ZERO);
        c.adicionarMaterial(new InsumoFake("Tecido", UnidadeMedida.M, d("15"), d("10")), BigDecimal.ONE);
        assertEquals(d("15.00"), c.calcularPrecoSugerido());
    }

    @Test void rejeitaMargemNegativa() {
        assertThrows(RegraNegocioException.class,
                () -> new ComposicaoPreco(new ProdutoFake("Saia", d("50")), d("5"), d("-1")));
        assertThrows(RegraNegocioException.class,
                () -> new ComposicaoPreco(new ProdutoFake("Saia", d("50")), d("-1"), d("5")));
    }

    @Test void composicaoSemMateriaisNaoCalculaNemAjusta() {
        var c = new ComposicaoPreco(new ProdutoFake("Saia", d("50")), d("5"), d("10"));
        assertEquals(BigDecimal.ZERO, c.calcularCustoMateriais());
        assertThrows(RegraNegocioException.class, c::calcularPrecoSugerido);
        assertThrows(RegraNegocioException.class, () -> c.ajustarPreco(d("25")));
    }

    @Test void ajusteManualPodeSerAplicadoAoProdutoCerto() {
        var p = new ProdutoFake("Saia", d("100"));
        var c = new ComposicaoPreco(p, BigDecimal.ZERO, BigDecimal.ZERO);
        var tecido = new InsumoFake("Tecido", UnidadeMedida.M, d("20"), d("10"));
        c.adicionarMaterial(tecido, BigDecimal.ONE);
        c.ajustarPreco(d("31.99"));
        c.aplicarComoPrecoDo(p);
        assertEquals(d("31.99"), p.getPreco());
        assertThrows(RegraNegocioException.class, () -> c.aplicarComoPrecoDo(new ProdutoFake("Outra", d("10"))));
        c.adicionarMaterial(tecido, BigDecimal.ONE);
        assertNull(c.getPrecoAjustado());
        c.aplicarComoPrecoDo(p);
        assertEquals(d("40.00"), p.getPreco());
    }

    @Test void listaDeMateriaisNaoPermiteEdicaoExterna() {
        var c = new ComposicaoPreco(new ProdutoFake("Saia", d("50")), BigDecimal.ZERO, BigDecimal.ZERO);
        assertThrows(UnsupportedOperationException.class,
                () -> c.getItens().add(new ItemComposicao(
                        new InsumoFake("Linha", UnidadeMedida.G, BigDecimal.ONE, BigDecimal.TEN), BigDecimal.ONE)));
    }
}
