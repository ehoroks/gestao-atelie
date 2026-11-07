package br.com.atelie.venda;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class VendaRepositoryTest {

    @Autowired
    private VendaRepository vendas;

    @Autowired
    private ItemVendaRepository itens;

    @Autowired
    private EntityManager entityManager;

    @Test
    void salvaVendaComItensEmCascata() {
        Venda venda = new Venda(null);
        venda.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("10.00"));
        venda.adicionarItem(20L, new BigDecimal("2"), new BigDecimal("5.00"));

        Venda salva = vendas.saveAndFlush(venda);
        entityManager.clear();

        Venda encontrada = vendas.findById(salva.getId()).orElseThrow();
        assertThat(encontrada.getItens()).hasSize(2);
    }

    @Test
    void removerItemAplicaOrphanRemoval() {
        Venda venda = new Venda(null);
        ItemVenda item = venda.adicionarItem(10L, BigDecimal.ONE, BigDecimal.TEN);
        Venda salva = vendas.saveAndFlush(venda);
        Long itemId = item.getId();

        salva.removerItem(itemId);
        vendas.saveAndFlush(salva);
        entityManager.clear();

        assertThat(itens.findById(itemId)).isEmpty();
    }

    @Test
    void valorFinalTemPisoEmZeroQuandoItensRemovidosAbaixoDoDesconto() {
        Venda venda = new Venda(null);
        ItemVenda item1 = venda.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("100.00"));
        venda.adicionarItem(20L, BigDecimal.ONE, new BigDecimal("50.00"));
        Venda salva = vendas.saveAndFlush(venda);

        // Desconto igual ao subtotal (150.00) e permitido no momento da aplicacao.
        salva.aplicarDesconto(new BigDecimal("150.00"));
        vendas.saveAndFlush(salva);

        // Depois remove um item: subtotal cai para 50.00, abaixo do desconto.
        salva.removerItem(item1.getId());
        vendas.saveAndFlush(salva);
        entityManager.clear();

        Venda encontrada = vendas.findById(salva.getId()).orElseThrow();
        assertThat(encontrada.calcularSubtotal()).isEqualByComparingTo("50.00");
        assertThat(encontrada.calcularValorFinal()).isEqualByComparingTo("0.00");
    }
}
