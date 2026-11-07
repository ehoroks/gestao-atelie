package br.com.atelie.usuario;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repositorio;

    @Test
    void salvarEBuscarPorEmailIgnorandoCaixa() {
        repositorio.saveAndFlush(new Usuario(
                "Ana", "ana@exemplo.com", "{bcrypt}fake", PerfilUsuario.ATENDENTE));

        Optional<Usuario> encontrado = repositorio.findByEmailIgnoreCase("ANA@EXEMPLO.COM");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Ana");
    }

    @Test
    void emailDeveSerUnico() {
        repositorio.saveAndFlush(new Usuario(
                "Ana", "ana@exemplo.com", "{bcrypt}fake", PerfilUsuario.ATENDENTE));

        assertThatThrownBy(() -> repositorio.saveAndFlush(new Usuario(
                "Outra", "ana@exemplo.com", "{bcrypt}fake", PerfilUsuario.ADMIN)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
