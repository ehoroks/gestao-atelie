package br.com.atelie.aplicacao;

import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.venda.ItemVenda;
import br.com.atelie.venda.ItemVendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoItemVendaTest {

    @Mock
    private ItemVendaRepository repositorio;

    private ServicoItemVenda servico;

    @BeforeEach
    void setUp() {
        servico = new ServicoItemVenda(repositorio);
    }

    @Test
    void adicionarCongelaPrecoAtualDoProduto() {
        when(repositorio.save(any(ItemVenda.class))).thenAnswer(inv -> inv.getArgument(0));

        servico.adicionar(1L, 2L, BigDecimal.ONE, new BigDecimal("45.90"));

        ArgumentCaptor<ItemVenda> captor = ArgumentCaptor.forClass(ItemVenda.class);
        verify(repositorio).save(captor.capture());
        assertThat(captor.getValue().getPrecoUnitario()).isEqualByComparingTo("45.90");
    }

    @Test
    void listarPorVendaDelegaAoRepositorio() {
        when(repositorio.findByVendaIdOrderByIdAsc(7L)).thenReturn(List.of());

        servico.listarPorVenda(7L);

        verify(repositorio).findByVendaIdOrderByIdAsc(7L);
    }

    @Test
    void subtotalDaVendaSomaItens() {
        ItemVenda a = new ItemVenda(1L, 10L, new BigDecimal("2"), new BigDecimal("10.00"));
        ItemVenda b = new ItemVenda(1L, 20L, new BigDecimal("1"), new BigDecimal("5.50"));
        when(repositorio.findByVendaIdOrderByIdAsc(1L)).thenReturn(List.of(a, b));

        BigDecimal subtotal = servico.calcularSubtotalDaVenda(1L);

        assertThat(subtotal).isEqualByComparingTo("25.50");
    }

    @Test
    void buscarInexistenteLancaNaoEncontrado() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void removerItemExistente() {
        ItemVenda item = new ItemVenda(1L, 2L, BigDecimal.ONE, BigDecimal.TEN);
        when(repositorio.findById(1L)).thenReturn(Optional.of(item));

        servico.remover(1L);

        verify(repositorio).delete(item);
    }
}
