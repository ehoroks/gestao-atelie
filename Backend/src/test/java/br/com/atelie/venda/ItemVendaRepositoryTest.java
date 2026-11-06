package br.com.atelie.venda;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ItemVendaRepositoryTest {

    @Autowired
    private ItemVendaRepository repositorio;

    @Autowired
    private VendaRepository vendas;

    @Test
    void buscaItensDaVendaNaOrdemDeCriacao() {
        Venda venda1 = vendas.saveAndFlush(new Venda(null));
        Venda venda2 = vendas.saveAndFlush(new Venda(null));

        repositorio.saveAndFlush(new ItemVenda(venda1, 10L, BigDecimal.ONE, new BigDecimal("10.00")));
        repositorio.saveAndFlush(new ItemVenda(venda1, 20L, new BigDecimal("2"), new BigDecimal("5.00")));
        repositorio.saveAndFlush(new ItemVenda(venda2, 30L, BigDecimal.ONE, new BigDecimal("9.00")));

        List<ItemVenda> itens = repositorio.findByVendaIdOrderByIdAsc(venda1.getId());

        assertThat(itens).extracting(ItemVenda::getProdutoId).containsExactly(10L, 20L);
    }

    @Test
    void persisteQuantidadeEPrecoComEscala() {
        Venda venda = vendas.saveAndFlush(new Venda(null));

        ItemVenda salvo = repositorio.saveAndFlush(
                new ItemVenda(venda, 10L, new BigDecimal("1.500"), new BigDecimal("12.345")));

        ItemVenda encontrado = repositorio.findById(salvo.getId()).orElseThrow();

        assertThat(encontrado.getQuantidade()).isEqualByComparingTo("1.500");
        assertThat(encontrado.getPrecoUnitario()).isEqualByComparingTo("12.35");
        assertThat(encontrado.calcularSubtotal()).isEqualByComparingTo("18.53");
    }
}
