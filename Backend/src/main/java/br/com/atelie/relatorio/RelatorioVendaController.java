package br.com.atelie.relatorio;

import br.com.atelie.aplicacao.ServicoRelatorioVenda;
import br.com.atelie.relatorio.dto.FaturamentoPeriodoResponse;
import br.com.atelie.relatorio.dto.ProdutoMaisVendidoResponse;
import br.com.atelie.venda.dto.VendaResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios/vendas")
public class RelatorioVendaController {

    private final ServicoRelatorioVenda servico;

    public RelatorioVendaController(ServicoRelatorioVenda servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<VendaResponse> listarVendas(@RequestParam LocalDate inicio,
                                            @RequestParam LocalDate fim) {
        return servico.listarVendas(inicio, fim).stream().map(VendaResponse::de).toList();
    }

    @GetMapping("/faturamento")
    public List<FaturamentoPeriodoResponse> faturamento(
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim,
            @RequestParam AgrupamentoFaturamento agruparPor) {
        return servico.faturamento(inicio, fim, agruparPor);
    }

    @GetMapping("/produtos-mais-vendidos")
    public List<ProdutoMaisVendidoResponse> produtosMaisVendidos(
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim,
            @RequestParam(defaultValue = "10") int limite) {
        return servico.produtosMaisVendidos(inicio, fim, limite);
    }
}
