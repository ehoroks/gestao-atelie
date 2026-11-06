package br.com.atelie.aplicacao;

import br.com.atelie.comum.RecursoJaExistenteException;
import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.usuario.CredenciaisInvalidasException;
import br.com.atelie.usuario.PerfilUsuario;
import br.com.atelie.usuario.Usuario;
import br.com.atelie.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Casos de uso da Trilha B (B1): cadastro, consulta e autenticacao de Usuario.
 *
 * A senha em texto puro so existe dentro deste metodo; o repositorio recebe
 * apenas o hash BCrypt.
 */
@Service
public class ServicoUsuario {

    private static final int TAMANHO_MINIMO_SENHA = 8;
    private static final int TAMANHO_MAXIMO_SENHA = 72;

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;

    public ServicoUsuario(UsuarioRepository usuarios, PasswordEncoder codificador) {
        this.usuarios = Objects.requireNonNull(usuarios, "repositorio de usuarios e obrigatorio");
        this.codificador = Objects.requireNonNull(codificador, "codificador de senha e obrigatorio");
    }

    @Transactional
    public Usuario cadastrar(String nome, String email, String senha, PerfilUsuario perfil) {
        validarSenha(senha);
        String emailNormalizado = Usuario.normalizarEmail(email);

        if (usuarios.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new RecursoJaExistenteException("Usuario", emailNormalizado);
        }

        String hash = codificador.encode(senha);
        return usuarios.save(new Usuario(nome, emailNormalizado, hash, perfil));
    }

    /** Valida email + senha. Mensagem generica para nao revelar qual campo falhou. */
    @Transactional(readOnly = true)
    public Usuario autenticar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new CredenciaisInvalidasException();
        }

        Usuario usuario = usuarios.findByEmailIgnoreCase(email.trim())
                .filter(Usuario::isAtivo)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!codificador.matches(senha, usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", id));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarios.findAll();
    }

    private static void validarSenha(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new RegraNegocioException(
                    "senha deve ter ao menos %d caracteres".formatted(TAMANHO_MINIMO_SENHA));
        }
        if (senha.length() > TAMANHO_MAXIMO_SENHA) {
            throw new RegraNegocioException(
                    "senha deve ter no maximo %d caracteres".formatted(TAMANHO_MAXIMO_SENHA));
        }
    }
}
