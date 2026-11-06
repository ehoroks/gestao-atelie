package br.com.atelie.venda;

import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.comum.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VendaTest {

    @Test
    void vendaPodeSerAbertaSemCliente() {
        Venda venda = new Venda(null);

        assertThat(venda.getClienteId()).isNull();
        assertThat(venda.getSituacao()).isEqualTo(SituacaoVenda.EM_ABERTO);
        assertThat(venda.getItens()).isEmpty();
    }

    @Test
    void clienteIdQuandoInformadoDeveSerPositivo() {
        assertThatThrownBy(() -> new Venda(0L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("clienteId");
    }

    @Test
    void adicionaItemEnquantoVendaEstaEmAberto() {
        Venda venda = new Venda(1L);

        ItemVenda item = venda.adicionarItem(10L, new BigDecimal("2"), new BigDecimal("25.00"));

        assertThat(venda.getItens()).containsExactly(item);
        assertThat(item.getVenda()).isSameAs(venda);
        assertThat(item.getPrecoUnitario()).isEqualByComparingTo("25.00");
    }

    @Test
    void naoAdicionaItemAposVendaConcluida() {
        Venda venda = new Venda(null);
        venda.alterarSituacao(SituacaoVenda.CONCLUIDA);

        assertThatThrownBy(() -> venda.adicionarItem(10L, BigDecimal.ONE, BigDecimal.TEN))
                .isInstanceOf(VendaNaoEditavelException.class)
                .hasMessageContaining("CONCLUIDA");
    }

    @Test
    void naoAdicionaItemAposVendaCancelada() {
        Venda venda = new Venda(null);
        venda.alterarSituacao(SituacaoVenda.CANCELADA);

        assertThatThrownBy(() -> venda.adicionarItem(10L, BigDecimal.ONE, BigDecimal.TEN))
                .isInstanceOf(VendaNaoEditavelException.class)
                .hasMessageContaining("CANCELADA");
    }

    @Test
    void naoRemoveItemAposVendaConcluida() {
        Venda venda = new Venda(null);
        ItemVenda item = venda.adicionarItem(10L, BigDecimal.ONE, BigDecimal.TEN);
        venda.alterarSituacao(SituacaoVenda.CONCLUIDA);

        assertThatThrownBy(() -> venda.removerItem(item.getId()))
                .isInstanceOf(VendaNaoEditavelException.class);
    }

    @Test
    void removerItemInexistenteLancaNaoEncontrado() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, BigDecimal.ONE, BigDecimal.TEN);

        assertThatThrownBy(() -> venda.removerItem(999L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("ItemVenda");
    }

    @Test
    void transicaoDeEstadoInvalidaEhRejeitada() {
        Venda venda = new Venda(null);
        venda.alterarSituacao(SituacaoVenda.CONCLUIDA);

        assertThatThrownBy(() -> venda.alterarSituacao(SituacaoVenda.EM_ABERTO))
                .isInstanceOf(TransicaoInvalidaException.class);
    }
}
