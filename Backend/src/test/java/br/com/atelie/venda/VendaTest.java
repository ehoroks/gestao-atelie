package br.com.atelie.venda;

import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.comum.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Test
    void aplicaDescontoAbsolutoMenorQueSubtotal() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("100.00"));

        venda.aplicarDesconto(new BigDecimal("15.55"));

        assertThat(venda.getDesconto()).isEqualByComparingTo("15.55");
    }

    @Test
    void descontoNaoPodeSerNegativo() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("100.00"));

        assertThatThrownBy(() -> venda.aplicarDesconto(new BigDecimal("-1.00")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("desconto");
    }

    @Test
    void descontoNaoPodeExcederSubtotal() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("100.00"));

        assertThatThrownBy(() -> venda.aplicarDesconto(new BigDecimal("100.01")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("subtotal");
    }

    @Test
    void descontoEhArredondadoParaDuasCasas() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("100.00"));

        venda.aplicarDesconto(new BigDecimal("10.555"));

        assertThat(venda.getDesconto()).isEqualByComparingTo("10.56");
    }

    @Test
    void naoAplicaDescontoAposFinalizar() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("100.00"));
        venda.alterarSituacao(SituacaoVenda.CONCLUIDA);

        assertThatThrownBy(() -> venda.aplicarDesconto(BigDecimal.TEN))
                .isInstanceOf(VendaNaoEditavelException.class);
    }

    @Test
    void defineFormaPagamento() {
        Venda venda = new Venda(null);

        venda.definirFormaPagamento(FormaPagamento.PIX);

        assertThat(venda.getFormaPagamento()).isEqualTo(FormaPagamento.PIX);
    }

    @Test
    void formaDePagamentoEhObrigatoria() {
        Venda venda = new Venda(null);

        assertThatThrownBy(() -> venda.definirFormaPagamento(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void naoDefineFormaPagamentoAposFinalizar() {
        Venda venda = new Venda(null);
        venda.alterarSituacao(SituacaoVenda.CONCLUIDA);

        assertThatThrownBy(() -> venda.definirFormaPagamento(FormaPagamento.PIX))
                .isInstanceOf(VendaNaoEditavelException.class);
    }

    // --- B6: subtotal e valor final ---

    @Test
    void subtotalSomaOsItens() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, new BigDecimal("2"), new BigDecimal("25.00"));
        venda.adicionarItem(20L, BigDecimal.ONE, new BigDecimal("10.00"));

        assertThat(venda.calcularSubtotal()).isEqualByComparingTo("60.00");
    }

    @Test
    void subtotalSemItensEhZero() {
        assertThat(new Venda(null).calcularSubtotal()).isEqualByComparingTo("0.00");
    }

    @Test
    void valorFinalSubtraiDesconto() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("100.00"));
        venda.aplicarDesconto(new BigDecimal("15.50"));

        assertThat(venda.calcularValorFinal()).isEqualByComparingTo("84.50");
    }

    @Test
    void valorFinalSemDescontoIgualaSubtotal() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("50.00"));

        assertThat(venda.calcularValorFinal()).isEqualByComparingTo("50.00");
    }

    // --- B7: finalizar ---

    @Test
    void finalizaVendaComItensEPagamento() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("50.00"));
        venda.definirFormaPagamento(FormaPagamento.PIX);

        venda.finalizar();

        assertThat(venda.getSituacao()).isEqualTo(SituacaoVenda.CONCLUIDA);
    }

    @Test
    void naoFinalizaVendaSemItens() {
        Venda venda = new Venda(null);
        venda.definirFormaPagamento(FormaPagamento.PIX);

        assertThatThrownBy(venda::finalizar)
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("itens");
    }

    @Test
    void naoFinalizaVendaSemFormaPagamento() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("50.00"));

        assertThatThrownBy(venda::finalizar)
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("forma de pagamento");
    }

    @Test
    void naoFinalizaVendaDuasVezes() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("50.00"));
        venda.definirFormaPagamento(FormaPagamento.DINHEIRO);
        venda.finalizar();

        assertThatThrownBy(venda::finalizar)
                .isInstanceOf(TransicaoInvalidaException.class);
    }

    @Test
    void naoFinalizaVendaJaCancelada() {
        Venda venda = new Venda(null);
        venda.alterarSituacao(SituacaoVenda.CANCELADA);

        assertThatThrownBy(venda::finalizar)
                .isInstanceOf(TransicaoInvalidaException.class);
    }

    // --- B8: cancelar ---

    @Test
    void cancelaVendaConcluidaERegistraDataEMotivo() {
        Venda venda = vendaConcluida();
        LocalDateTime data = LocalDateTime.of(2026, 11, 6, 10, 30);

        venda.cancelar(data, "cliente desistiu");

        assertThat(venda.getSituacao()).isEqualTo(SituacaoVenda.CANCELADA);
        assertThat(venda.getCanceladaEm()).isEqualTo(data);
        assertThat(venda.getMotivoCancelamento()).isEqualTo("cliente desistiu");
    }

    @Test
    void naoCancelaVendaEmAberto() {
        Venda venda = new Venda(null);

        assertThatThrownBy(() -> venda.cancelar(LocalDateTime.now(), "motivo"))
                .isInstanceOf(TransicaoInvalidaException.class);
    }

    @Test
    void naoCancelaVendaDuasVezes() {
        Venda venda = vendaConcluida();
        venda.cancelar(LocalDateTime.now(), "primeiro cancelamento");

        assertThatThrownBy(() -> venda.cancelar(LocalDateTime.now(), "segundo cancelamento"))
                .isInstanceOf(TransicaoInvalidaException.class);
    }

    @Test
    void motivoDoCancelamentoEhObrigatorio() {
        Venda venda = vendaConcluida();

        assertThatThrownBy(() -> venda.cancelar(LocalDateTime.now(), "  "))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("motivo");
    }

    @Test
    void comprovanteListaItensTotaisPagamentoEEstado() {
        String comprovante = vendaConcluida().gerarComprovante();

        assertThat(comprovante)
                .contains("COMPROVANTE DE VENDA", "Produto #10", "Subtotal:",
                        "Desconto:", "VALOR FINAL:", "Forma de pagamento: PIX", "Situacao: CONCLUIDA")
                .contains("50,00");
    }

    @Test
    void comprovanteDeVendaCanceladaIncluiDataEMotivo() {
        Venda venda = vendaConcluida();
        venda.cancelar(LocalDateTime.of(2026, 11, 6, 10, 30), "cliente desistiu");

        String comprovante = venda.gerarComprovante();

        assertThat(comprovante)
                .contains("Situacao: CANCELADA", "Cancelada em: 06/11/2026 10:30:00",
                        "Motivo do cancelamento: cliente desistiu");
    }

    private static Venda vendaConcluida() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("50.00"));
        venda.definirFormaPagamento(FormaPagamento.PIX);
        venda.finalizar();
        return venda;
    }
}
