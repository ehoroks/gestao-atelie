package br.com.atelie.catalogo;

import br.com.atelie.catalogo.dto.ProdutoCadastroRequest;
import br.com.atelie.catalogo.dto.ProdutoEdicaoRequest;
import br.com.atelie.catalogo.dto.ProdutoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ServicoCatalogo servico;
    public ProdutoController(ServicoCatalogo servico) { this.servico = servico; }

    @GetMapping
    public List<ProdutoResponse> listar(@RequestParam(required = false) String nome,
                                       @RequestParam(required = false) Long categoriaId,
                                       @RequestParam(required = false) Boolean ativo,
                                       @RequestParam(required = false) String cor,
                                       @RequestParam(required = false) String tamanho,
                                       @RequestParam(required = false) BigDecimal precoMin,
                                       @RequestParam(required = false) BigDecimal precoMax) {
        return servico.listarProdutos(nome, categoriaId, ativo, cor, tamanho, precoMin, precoMax);
    }

    @GetMapping("/{id}") public ProdutoResponse buscar(@PathVariable Long id) { return servico.buscarProduto(id); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse criar(@Valid @RequestBody ProdutoCadastroRequest request) { return servico.criarProduto(request); }

    @PutMapping("/{id}")
    public ProdutoResponse editar(@PathVariable Long id, @Valid @RequestBody ProdutoEdicaoRequest request) {
        return servico.editarProduto(id, request);
    }

    @PatchMapping("/{id}/desativar")
    public ProdutoResponse desativar(@PathVariable Long id) { return servico.desativarProduto(id); }
}
