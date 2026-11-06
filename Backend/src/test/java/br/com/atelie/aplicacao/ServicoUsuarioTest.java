package br.com.atelie.aplicacao;

import br.com.atelie.comum.RecursoJaExistenteException;
import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.usuario.CredenciaisInvalidasException;
import br.com.atelie.usuario.PerfilUsuario;
import br.com.atelie.usuario.Usuario;
import br.com.atelie.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoUsuarioTest {

    @Mock
    private UsuarioRepository repositorio;

    private PasswordEncoder codificador;
    private ServicoUsuario servico;

    @BeforeEach
    void setUp() {
        // custo baixo para o teste nao ficar lento; a configuracao de producao usa o custo padrao
        codificador = new BCryptPasswordEncoder(4);
        servico = new ServicoUsuario(repositorio, codificador);
    }

    @Test
    void cadastrarNormalizaEmailEArmazenaSomenteHash() {
        when(repositorio.existsByEmailIgnoreCase("ana@exemplo.com")).thenReturn(false);
        when(repositorio.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario usuario = servico.cadastrar(
                "Ana", "  Ana@Exemplo.com  ", "senha12345", PerfilUsuario.ATENDENTE);

        assertThat(usuario.getEmail()).isEqualTo("ana@exemplo.com");
        assertThat(usuario.getSenhaHash()).isNotEqualTo("senha12345");
        assertThat(codificador.matches("senha12345", usuario.getSenhaHash())).isTrue();
    }

    @Test
    void cadastrarEmailDuplicadoLancaConflito() {
        when(repositorio.existsByEmailIgnoreCase("ana@exemplo.com")).thenReturn(true);

        assertThatThrownBy(() -> servico.cadastrar(
                "Ana", "ana@exemplo.com", "senha12345", PerfilUsuario.ATENDENTE))
                .isInstanceOf(RecursoJaExistenteException.class);
    }

    @Test
    void cadastrarSenhaCurtaLancaRegraDeNegocio() {
        assertThatThrownBy(() -> servico.cadastrar(
                "Ana", "ana@exemplo.com", "curta", PerfilUsuario.ATENDENTE))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("senha");
    }

    @Test
    void autenticarRetornaUsuarioQuandoSenhaConfere() {
        Usuario usuario = new Usuario(
                "Ana", "ana@exemplo.com", codificador.encode("senha12345"), PerfilUsuario.ADMIN);
        when(repositorio.findByEmailIgnoreCase("ana@exemplo.com")).thenReturn(Optional.of(usuario));

        Usuario autenticado = servico.autenticar("ana@exemplo.com", "senha12345");

        assertThat(autenticado).isSameAs(usuario);
    }

    @Test
    void autenticarSenhaErradaLancaCredenciaisInvalidas() {
        Usuario usuario = new Usuario(
                "Ana", "ana@exemplo.com", codificador.encode("senha12345"), PerfilUsuario.ADMIN);
        when(repositorio.findByEmailIgnoreCase("ana@exemplo.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> servico.autenticar("ana@exemplo.com", "outraSenha"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void autenticarUsuarioInativoLancaCredenciaisInvalidas() {
        Usuario usuario = new Usuario(
                "Ana", "ana@exemplo.com", codificador.encode("senha12345"), PerfilUsuario.ADMIN);
        usuario.desativar();
        when(repositorio.findByEmailIgnoreCase("ana@exemplo.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> servico.autenticar("ana@exemplo.com", "senha12345"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void buscarPorIdInexistenteLancaNaoEncontrado() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
