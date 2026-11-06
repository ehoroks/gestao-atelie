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
}
