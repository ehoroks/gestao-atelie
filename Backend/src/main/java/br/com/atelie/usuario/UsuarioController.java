package br.com.atelie.usuario;

import br.com.atelie.aplicacao.ServicoUsuario;
import br.com.atelie.usuario.dto.CadastroUsuarioRequest;
import br.com.atelie.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final ServicoUsuario servico;

    public UsuarioController(ServicoUsuario servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroUsuarioRequest request) {
        Usuario usuario = servico.cadastrar(
                request.nome(), request.email(), request.senha(), request.perfil());
        return UsuarioResponse.de(usuario);
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return UsuarioResponse.de(servico.buscarPorId(id));
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return servico.listar().stream().map(UsuarioResponse::de).toList();
    }
}
