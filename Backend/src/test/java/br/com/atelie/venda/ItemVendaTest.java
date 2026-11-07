package br.com.atelie.venda;

import br.com.atelie.catalogo.ComposicaoPreco;
import br.com.atelie.catalogo.Insumo;
import br.com.atelie.catalogo.Produto;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.estoque.UnidadeMedida;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemVendaTest {

    private final Venda venda = new Venda(null);

    @Test
    void subtotalMultiplicaPrecoCongeladoPelaQuantidade() {
        ItemVenda item = new ItemVenda(
                venda, 2L, new BigDecimal("2.5"), new BigDecimal("19.90"));

        assertThat(item.calcularSubtotal()).isEqualByComparingTo("49.75");
    }

    @Test
    void precoCongeladoNaoAcompanhaMudancaPosteriorDoProduto() {
        Produto produto = new Produto(
                "Bolsa", UnidadeMedida.UN, BigDecimal.ZERO, null, new BigDecimal("50.00"));

        ItemVenda item = ItemVenda.comPrecoCongelado(
                venda, 1L, BigDecimal.ONE, produto.getPreco());

        // Trilha C altera o preco do produto depois da venda do item.
        Insumo tecido = new Insumo("Tecido", UnidadeMedida.M, BigDecimal.ZERO, new BigDecimal("10.00"));
        ComposicaoPreco composicao = new ComposicaoPreco(produto, BigDecimal.ZERO, BigDecimal.ZERO);
        composicao.adicionarMaterial(tecido, BigDecimal.ONE);
        composicao.ajustarPreco(new BigDecimal("70.00"));
        composicao.aplicarComoPrecoDo(produto);

        assertThat(produto.getPreco()).isEqualByComparingTo("70.00");
        assertThat(item.getPrecoUnitario()).isEqualByComparingTo("50.00");
    }

    @Test
    void quantidadeDeveSerPositiva() {
        assertThatThrownBy(() -> new ItemVenda(
                venda, 2L, BigDecimal.ZERO, new BigDecimal("10.00")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("quantidade");
    }

    @Test
    void precoNaoPodeSerNegativo() {
        assertThatThrownBy(() -> new ItemVenda(
                venda, 2L, BigDecimal.ONE, new BigDecimal("-0.01")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("preco");
    }

    @Test
    void vendaEProdutoSaoObrigatorios() {
        assertThatThrownBy(() -> new ItemVenda(
                null, 2L, BigDecimal.ONE, BigDecimal.TEN))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("venda");

        assertThatThrownBy(() -> new ItemVenda(
                venda, null, BigDecimal.ONE, BigDecimal.TEN))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("produtoId");
    }

    @Test
    void subtotalArredondaParaDuasCasas() {
        ItemVenda item = new ItemVenda(
                venda, 2L, new BigDecimal("3"), new BigDecimal("3.333"));

        assertThat(item.getPrecoUnitario()).isEqualByComparingTo("3.33");
        assertThat(item.calcularSubtotal()).isEqualByComparingTo("9.99");
    }
}
