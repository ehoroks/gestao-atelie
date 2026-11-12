package br.com.atelie.catalogo;

import br.com.atelie.catalogo.dto.CategoriaRequest;
import br.com.atelie.catalogo.dto.CategoriaResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final ServicoCatalogo servico;
    public CategoriaController(ServicoCatalogo servico) { this.servico = servico; }

    @GetMapping public List<CategoriaResponse> listar() { return servico.listarCategorias(); }
    @GetMapping("/{id}") public CategoriaResponse buscar(@PathVariable Long id) { return servico.buscarCategoria(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse criar(@Valid @RequestBody CategoriaRequest request) { return servico.criarCategoria(request); }
    @PutMapping("/{id}")
    public CategoriaResponse editar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return servico.editarCategoria(id, request);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) { servico.excluirCategoria(id); }
}
