package br.com.atelie.venda;

import br.com.atelie.aplicacao.ServicoVenda;
import br.com.atelie.venda.dto.AberturaVendaRequest;
import br.com.atelie.venda.dto.AdicionarItemRequest;
import br.com.atelie.venda.dto.AplicarDescontoRequest;
import br.com.atelie.venda.dto.DefinirFormaPagamentoRequest;
import br.com.atelie.venda.dto.ItemVendaResponse;
import br.com.atelie.venda.dto.VendaResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {

    private final ServicoVenda servico;

    public VendaController(ServicoVenda servico) {
        this.servico = servico;
    }

    /** Corpo opcional: sem `clienteId` a venda e de balcao. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendaResponse abrir(@RequestBody(required = false) @Valid AberturaVendaRequest request) {
        return VendaResponse.de(servico.abrir(request == null ? null : request.clienteId()));
    }

    @GetMapping
    public List<VendaResponse> listar() {
        return servico.listar().stream().map(VendaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public VendaResponse buscar(@PathVariable Long id) {
        return VendaResponse.de(servico.buscarPorId(id));
    }

    @PostMapping("/{id}/itens")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemVendaResponse adicionarItem(@PathVariable Long id,
                                           @Valid @RequestBody AdicionarItemRequest request) {
        return ItemVendaResponse.de(servico.adicionarItem(
                id, request.produtoId(), request.quantidade(), request.precoUnitario()));
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerItem(@PathVariable Long id, @PathVariable Long itemId) {
        servico.removerItem(id, itemId);
    }

    @PutMapping("/{id}/desconto")
    public VendaResponse aplicarDesconto(@PathVariable Long id,
                                         @Valid @RequestBody AplicarDescontoRequest request) {
        return VendaResponse.de(servico.aplicarDesconto(id, request.valor()));
    }

    @PutMapping("/{id}/forma-pagamento")
    public VendaResponse definirFormaPagamento(
            @PathVariable Long id,
            @Valid @RequestBody DefinirFormaPagamentoRequest request) {
        return VendaResponse.de(servico.definirFormaPagamento(id, request.formaPagamento()));
    }

    @PostMapping("/{id}/finalizar")
    public VendaResponse finalizar(@PathVariable Long id) {
        return VendaResponse.de(servico.finalizar(id));
    }
}
