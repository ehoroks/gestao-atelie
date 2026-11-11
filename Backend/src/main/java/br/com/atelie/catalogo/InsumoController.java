package br.com.atelie.catalogo;

import br.com.atelie.catalogo.dto.AtualizarCustoRequest;
import br.com.atelie.catalogo.dto.InsumoCadastroRequest;
import br.com.atelie.catalogo.dto.InsumoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/insumos")
public class InsumoController {
    private final ServicoCatalogo servico;
    public InsumoController(ServicoCatalogo servico) { this.servico = servico; }

    @GetMapping public List<InsumoResponse> listar(@RequestParam(required = false) String nome) {
        return servico.listarInsumos(nome);
    }

    @GetMapping("/{id}") public InsumoResponse buscar(@PathVariable Long id) { return servico.buscarInsumo(id); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public InsumoResponse criar(@Valid @RequestBody InsumoCadastroRequest request) { return servico.criarInsumo(request); }

    @PatchMapping("/{id}/custo")
    public InsumoResponse atualizarCusto(@PathVariable Long id, @Valid @RequestBody AtualizarCustoRequest request) {
        return servico.atualizarCusto(id, request);
    }
}
