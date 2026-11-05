package br.com.atelie.catalogo;

import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.estoque.UnidadeMedida;
import br.com.atelie.fixtures.InsumoFake;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemComposicaoTest {
    @Test void custoEhMultiplicacaoExata() {
        var tecido = new InsumoFake("Tecido", UnidadeMedida.M, new BigDecimal("12.50"), BigDecimal.TEN);
        var item = new ItemComposicao(tecido, new BigDecimal("1.5"));
        assertEquals(0, new BigDecimal("18.750").compareTo(item.calcularCusto()));
    }

    @Test void rejeitaQuantidadeInvalidaOuFracionadaEmUnidade() {
        var botao = new InsumoFake("Botao", UnidadeMedida.UN, BigDecimal.ONE, BigDecimal.TEN);
        assertThrows(RegraNegocioException.class, () -> new ItemComposicao(botao, BigDecimal.ZERO));
        assertThrows(RegraNegocioException.class, () -> new ItemComposicao(botao, new BigDecimal("-2")));
        assertThrows(RegraNegocioException.class, () -> new ItemComposicao(botao, new BigDecimal("1.5")));
    }
}
