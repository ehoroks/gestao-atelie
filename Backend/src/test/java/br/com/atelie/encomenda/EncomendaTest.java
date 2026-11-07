package br.com.atelie.encomenda;

import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.comum.TransicaoInvalidaException;
import br.com.atelie.fixtures.ProdutoFake;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EncomendaTest {
    private Encomenda nova() {
        return new Encomenda(42L, new ProdutoFake("Vestido", BigDecimal.TEN), "Sob medida",
                new BigDecimal("99.90"), LocalDate.of(2026, 11, 4),
                LocalDate.of(2026, 11, 15), "Cor azul");
    }

    @Test void percorreEstadosValidosEEncerra() {
        var e = nova();
        assertTrue(e.estaPendente());
        e.atualizarSituacao(SituacaoEncomenda.EM_PRODUCAO);
        e.atualizarSituacao(SituacaoEncomenda.PRONTA);
        e.atualizarSituacao(SituacaoEncomenda.ENTREGUE);
        assertFalse(e.estaPendente());
        assertThrows(TransicaoInvalidaException.class,
                () -> e.atualizarSituacao(SituacaoEncomenda.CANCELADA));
    }

    @Test void naoPermiteSaltarEtapas() {
        var e = nova();
        assertThrows(TransicaoInvalidaException.class,
                () -> e.atualizarSituacao(SituacaoEncomenda.ENTREGUE));
        assertEquals(SituacaoEncomenda.REGISTRADA, e.getSituacao());
    }

    @Test void podeCancelarDeQualquerEstadoNaoTerminal() {
        for (int etapa = 0; etapa < 3; etapa++) {
            var e = nova();
            if (etapa >= 1) e.atualizarSituacao(SituacaoEncomenda.EM_PRODUCAO);
            if (etapa >= 2) e.atualizarSituacao(SituacaoEncomenda.PRONTA);
            e.atualizarSituacao(SituacaoEncomenda.CANCELADA);
            assertFalse(e.estaPendente());
            assertThrows(TransicaoInvalidaException.class,
                    () -> e.atualizarSituacao(SituacaoEncomenda.EM_PRODUCAO));
        }
    }

    @Test void bloqueiaDadosInvalidos() {
        assertThrows(RegraNegocioException.class,
                () -> new Encomenda(0L, new ProdutoFake("V", BigDecimal.TEN), "Vestido", BigDecimal.TEN,
                        LocalDate.now(), LocalDate.now(), null));
        assertThrows(RegraNegocioException.class,
                () -> new Encomenda(1L, new ProdutoFake("V", BigDecimal.TEN), "Vestido", new BigDecimal("-1"),
                        LocalDate.now(), LocalDate.now(), null));
        assertThrows(RegraNegocioException.class,
                () -> new Encomenda(1L, new ProdutoFake("V", BigDecimal.TEN), "Vestido", BigDecimal.TEN,
                        LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 3), null));
    }
}
