package br.com.atelie.estoque;

import br.com.atelie.estoque.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {
    private final ServicoGestaoEstoque servico;
    public EstoqueController(ServicoGestaoEstoque servico) { this.servico = servico; }

    @GetMapping
    public List<EstoqueItemResponse> listar(@RequestParam(required = false) String tipo,
                                             @RequestParam(required = false) Boolean baixo,
                                             @RequestParam(required = false) String nome) {
        return servico.listar(tipo, baixo, nome);
    }

    @GetMapping("/baixo")
    public List<EstoqueItemResponse> alertas() { return servico.listar(null, true, null); }

    @GetMapping("/{id}")
    public EstoqueItemResponse buscar(@PathVariable Long id) { return servico.buscar(id); }

    @GetMapping("/{id}/movimentacoes")
    public List<MovimentacaoResponse> historico(@PathVariable Long id) { return servico.historico(id); }

    @PostMapping("/{id}/entradas")
    public EstoqueItemResponse entrada(@PathVariable Long id, @Valid @RequestBody MovimentarEstoqueRequest request) {
        return servico.entrada(id, request);
    }

    @PostMapping("/{id}/saidas")
    public EstoqueItemResponse saida(@PathVariable Long id, @Valid @RequestBody MovimentarEstoqueRequest request) {
        return servico.saida(id, request);
    }

    @PostMapping("/{id}/ajustes")
    public EstoqueItemResponse ajustar(@PathVariable Long id, @Valid @RequestBody AjustarEstoqueRequest request) {
        return servico.ajustar(id, request);
    }
}
