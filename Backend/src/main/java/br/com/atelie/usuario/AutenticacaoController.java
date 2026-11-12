package br.com.atelie.usuario;

import br.com.atelie.aplicacao.ServicoUsuario;
import br.com.atelie.seguranca.JwtService;
import br.com.atelie.usuario.dto.LoginRequest;
import br.com.atelie.usuario.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * B1: autenticacao por email + senha.
 *
 * <p>Valida as credenciais com BCrypt e devolve um token JWT para ser enviado
 * nas demais requisicoes como {@code Authorization: Bearer <token>}.
 * A protecao das rotas fica em {@code br.com.atelie.seguranca.SecurityConfig}.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final ServicoUsuario servico;
    private final JwtService jwtService;

    public AutenticacaoController(ServicoUsuario servico, JwtService jwtService) {
        this.servico = servico;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = servico.autenticar(request.email(), request.senha());
        String token = jwtService.gerarToken(usuario);
        return LoginResponse.de(usuario, token, jwtService.expiracaoEmSegundos());
    }
}
