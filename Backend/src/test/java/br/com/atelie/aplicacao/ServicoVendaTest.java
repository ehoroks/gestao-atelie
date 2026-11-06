package br.com.atelie.aplicacao;

import br.com.atelie.cliente.ClienteRepository;
import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.venda.ItemVenda;
import br.com.atelie.venda.Venda;
import br.com.atelie.venda.VendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ServicoVendaTest {

    @Mock
    private VendaRepository vendas;

    @Mock
    private ClienteRepository clientes;

    private ServicoVenda servico;

    @BeforeEach
    void setUp() {
        servico = new ServicoVenda(vendas, clientes);
    }

    @Test
    void abreVendaSemCliente() {
        when(vendas.save(any(Venda.class))).thenAnswer(inv -> inv.getArgument(0));

        Venda venda = servico.abrir(null);

        assertThat(venda.getClienteId()).isNull();
    }

    @Test
    void abreVendaComClienteExistente() {
        when(clientes.existsById(1L)).thenReturn(true);
        when(vendas.save(any(Venda.class))).thenAnswer(inv -> inv.getArgument(0));

        Venda venda = servico.abrir(1L);

        assertThat(venda.getClienteId()).isEqualTo(1L);
    }

    @Test
    void rejeitaClienteInexistente() {
        when(clientes.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> servico.abrir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Cliente");
    }

    @Test
    void adicionaItemNaVendaCarregada() {
        Venda venda = new Venda(null);
        when(vendas.findById(1L)).thenReturn(Optional.of(venda));

        ItemVenda item = servico.adicionarItem(1L, 10L, BigDecimal.ONE, new BigDecimal("12.50"));

        assertThat(item.getProdutoId()).isEqualTo(10L);
        assertThat(item.getPrecoUnitario()).isEqualByComparingTo("12.50");
        verify(vendas).flush();
    }

    @Test
    void removerItemInexistenteLancaNaoEncontrado() {
        Venda venda = new Venda(null);
        when(vendas.findById(1L)).thenReturn(Optional.of(venda));

        assertThatThrownBy(() -> servico.removerItem(1L, 999L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void buscarVendaInexistenteLancaNaoEncontrado() {
        when(vendas.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void listarDelegaAoRepositorio() {
        when(vendas.findAllByOrderByIdAsc()).thenReturn(List.of());

        servico.listar();

        verify(vendas).findAllByOrderByIdAsc();
    }
}
