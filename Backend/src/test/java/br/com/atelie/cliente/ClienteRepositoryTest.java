package br.com.atelie.cliente;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository repositorio;

    @Test
    void buscaPorTrechoDoNomeIgnorandoCaixa() {
        repositorio.saveAndFlush(new Cliente("Maria Silva", "(11) 98765-4321", null));
        repositorio.saveAndFlush(new Cliente("Bruno Souza", "(21) 3333-4444", null));

        List<Cliente> encontrados = repositorio.buscarPorNomeOuTelefone("mari", "");

        assertThat(encontrados).extracting(Cliente::getNome).containsExactly("Maria Silva");
    }

    @Test
    void buscaPorTrechoDoTelefone() {
        repositorio.saveAndFlush(new Cliente("Maria Silva", "(11) 98765-4321", null));
        repositorio.saveAndFlush(new Cliente("Bruno Souza", "(21) 3333-4444", null));

        List<Cliente> encontrados = repositorio.buscarPorNomeOuTelefone("98765", "98765");

        assertThat(encontrados).extracting(Cliente::getNome).containsExactly("Maria Silva");
    }

    @Test
    void buscaSemDigitosNaoCasaComTodosOsTelefones() {
        repositorio.saveAndFlush(new Cliente("Maria Silva", "(11) 98765-4321", null));

        List<Cliente> encontrados = repositorio.buscarPorNomeOuTelefone("inexistente", "");

        assertThat(encontrados).isEmpty();
    }
}
