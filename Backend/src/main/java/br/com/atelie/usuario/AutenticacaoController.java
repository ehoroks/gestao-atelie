package br.com.atelie.usuario;

import br.com.atelie.aplicacao.ServicoUsuario;
import br.com.atelie.usuario.dto.LoginRequest;
import br.com.atelie.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * B1: autenticacao por email + senha.
 *
 * Esta fatia devolve os dados do usuario autenticado. A emissao de token
 * (JWT/sessao) e a protecao das demais rotas ficam para a etapa de
 * Spring Security completa, alinhada com a Trilha D.
 */
@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final ServicoUsuario servico;

    public AutenticacaoController(ServicoUsuario servico) {
        this.servico = servico;
    }

    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest request) {
        return UsuarioResponse.de(servico.autenticar(request.email(), request.senha()));
    }
}
