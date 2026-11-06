package br.com.atelie.cliente;

import br.com.atelie.aplicacao.ServicoCliente;
import br.com.atelie.cliente.dto.ClienteRequest;
import br.com.atelie.cliente.dto.ClienteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ServicoCliente servico;

    public ClienteController(ServicoCliente servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse cadastrar(@Valid @RequestBody ClienteRequest request) {
        return ClienteResponse.de(
                servico.cadastrar(request.nome(), request.telefone(), request.email()));
    }

    /** `termo` opcional: busca por nome ou telefone; sem termo, lista todos. */
    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(required = false) String termo) {
        return servico.buscar(termo).stream().map(ClienteResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id) {
        return ClienteResponse.de(servico.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Long id,
                                     @Valid @RequestBody ClienteRequest request) {
        return ClienteResponse.de(
                servico.atualizar(id, request.nome(), request.telefone(), request.email()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        servico.excluir(id);
    }
}
