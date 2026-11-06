package br.com.atelie.aplicacao;

import br.com.atelie.cliente.Cliente;
import br.com.atelie.cliente.ClienteRepository;
import br.com.atelie.comum.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoClienteTest {

    @Mock
    private ClienteRepository repositorio;

    private ServicoCliente servico;

    @BeforeEach
    void setUp() {
        servico = new ServicoCliente(repositorio);
    }

    @Test
    void cadastrarNormalizaEDelegaAoRepositorio() {
        when(repositorio.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente cliente = servico.cadastrar("Ana", "(11) 98765-4321", "ANA@EXEMPLO.COM");

        assertThat(cliente.getNome()).isEqualTo("Ana");
        assertThat(cliente.getTelefone()).isEqualTo("11987654321");
        assertThat(cliente.getEmail()).isEqualTo("ana@exemplo.com");
    }

    @Test
    void buscaSemTermoListaTodos() {
        when(repositorio.findAll(any(Sort.class))).thenReturn(List.of());

        servico.buscar("   ");

        verify(repositorio).findAll(any(Sort.class));
    }

    @Test
    void buscaComTelefoneExtraiSomenteDigitos() {
        when(repositorio.buscarPorNomeOuTelefone("(11) 98765-4321", "11987654321"))
                .thenReturn(List.of());

        servico.buscar("(11) 98765-4321");

        verify(repositorio).buscarPorNomeOuTelefone("(11) 98765-4321", "11987654321");
    }

    @Test
    void atualizarClienteInexistenteLancaNaoEncontrado() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.atualizar(99L, "Ana", "1133334444", null))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void excluirClienteInexistenteLancaNaoEncontrado() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void excluirClienteExistenteRemoveDoRepositorio() {
        Cliente cliente = new Cliente("Ana", "1133334444", null);
        when(repositorio.findById(1L)).thenReturn(Optional.of(cliente));

        servico.excluir(1L);

        verify(repositorio).delete(cliente);
    }
}
