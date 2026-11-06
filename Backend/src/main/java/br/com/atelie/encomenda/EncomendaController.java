package br.com.atelie.encomenda;

import br.com.atelie.aplicacao.ServicoEncomenda;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/encomendas")
public class EncomendaController {

    private final ServicoEncomenda servico;

    public EncomendaController(ServicoEncomenda servico) {
        this.servico = servico;
    }

    @PostMapping
    public ResponseEntity<EncomendaResponse> criar(@Valid @RequestBody EncomendaRequest request) {
        EncomendaResponse criada = servico.criar(request);
        return ResponseEntity.created(URI.create("/api/encomendas/" + criada.id())).body(criada);
    }

    @GetMapping
    public List<EncomendaResponse> listar(
            @RequestParam(required = false) SituacaoEncomenda situacao,
            @RequestParam(defaultValue = "false") boolean pendentes) {
        return servico.listar(situacao, pendentes);
    }

    @GetMapping("/{id}")
    public EncomendaResponse buscar(@PathVariable Long id) {
        return servico.buscarPorId(id);
    }

    @PatchMapping("/{id}/situacao")
    public EncomendaResponse atualizarSituacao(@PathVariable Long id,
                                                @Valid @RequestBody AtualizarSituacaoRequest request) {
        return servico.atualizarSituacao(id, request.situacao());
    }
}
