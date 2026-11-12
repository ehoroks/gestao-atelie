package br.com.atelie.seguranca;

import br.com.atelie.aplicacao.ServicoUsuario;
import br.com.atelie.usuario.PerfilUsuario;
import br.com.atelie.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testes unitarios do bootstrap do primeiro ADMIN. */
class AdminInicialTest {

    private final ServicoUsuario servico = mock(ServicoUsuario.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);

    @Test
    void criaAdminQuandoConfiguradoENaoExiste() {
        when(usuarios.existsByEmailIgnoreCase("admin@exemplo.com")).thenReturn(false);

        new AdminInicial(servico, usuarios, "admin@exemplo.com", "senha12345", "Administrador")
                .run(null);

        verify(servico).cadastrar("Administrador", "admin@exemplo.com", "senha12345", PerfilUsuario.ADMIN);
    }

    @Test
    void naoCriaNadaSemVariaveis() {
        new AdminInicial(servico, usuarios, "", "", "Administrador").run(null);

        verify(usuarios, never()).existsByEmailIgnoreCase(anyString());
        verify(servico, never()).cadastrar(anyString(), anyString(), anyString(), any());
    }

    @Test
    void eIdempotenteQuandoAdminJaExiste() {
        when(usuarios.existsByEmailIgnoreCase("admin@exemplo.com")).thenReturn(true);

        new AdminInicial(servico, usuarios, "admin@exemplo.com", "senha12345", "Administrador")
                .run(null);

        verify(servico, never()).cadastrar(anyString(), anyString(), anyString(), any());
    }
}
