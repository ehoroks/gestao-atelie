package br.com.atelie.encomenda;

import br.com.atelie.catalogo.Insumo;
import br.com.atelie.catalogo.InsumoRepository;
import br.com.atelie.catalogo.Produto;
import br.com.atelie.catalogo.ProdutoRepository;
import br.com.atelie.cliente.Cliente;
import br.com.atelie.cliente.ClienteRepository;
import br.com.atelie.estoque.MovimentacaoEstoqueRepository;
import br.com.atelie.estoque.UnidadeMedida;
import br.com.atelie.infra.teste.IntegrationTestBase;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EncomendaControllerIT extends IntegrationTestBase {

    @Autowired
    private ClienteRepository clientes;

    @Autowired
    private ProdutoRepository produtos;

    @Autowired
    private InsumoRepository insumos;

    @Autowired
    private EncomendaRepository encomendas;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacoes;

    @Test
    void iniciaProducaoBaixaInsumoEGravaMovimentacaoUmaUnicaVez() throws Exception {
        Cliente cliente = criarCliente();
        Produto produto = criarProduto();
        Insumo insumo = criarInsumo("Tecido", "10");
        jdbc.execute("DELETE FROM movimentacoes_estoque");
        assertThat(movimentacoes.count()).isZero();

        Long encomendaId = criarEncomenda(cliente, produto, insumo, "3");

        mockMvc.perform(patch("/api/encomendas/{id}/situacao", encomendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\":\"EM_PRODUCAO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("EM_PRODUCAO"))
                .andExpect(jsonPath("$.insumosConsumidos").value(true));

        var saidas = movimentacoes.listarPorItemId(insumo.getId());
        assertThat(saidas).hasSize(1);
        assertThat(saidas.get(0).getTipo()).isEqualTo(br.com.atelie.estoque.TipoMovimentacao.SAIDA);
        assertThat(saidas.get(0).getQuantidade()).isEqualByComparingTo("3.000");
        assertThat(saidas.get(0).getSaldoResultante()).isEqualByComparingTo("7.000");
        assertThat(insumos.findById(insumo.getId()).orElseThrow().getQuantidade())
                .isEqualByComparingTo("7.000");

        mockMvc.perform(patch("/api/encomendas/{id}/situacao", encomendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\":\"PRONTA\"}"))
                .andExpect(status().isOk());
        assertThat(movimentacoes.listarPorItemId(insumo.getId())).hasSize(1);
    }

    @Test
    void faltaDeEstoqueNaoPersisteTransicaoNemMovimentacoesParciais() throws Exception {
        Cliente cliente = criarCliente();
        Produto produto = criarProduto();
        Insumo suficiente = criarInsumo("Tecido", "10");
        Insumo insuficiente = criarInsumo("Linha", "2");
        jdbc.execute("DELETE FROM movimentacoes_estoque");
        Long encomendaId = criarEncomenda(cliente, produto, suficiente, "3", insuficiente, "5");

        mockMvc.perform(patch("/api/encomendas/{id}/situacao", encomendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\":\"EM_PRODUCAO\"}"))
                .andExpect(status().isConflict());

        assertThat(encomendas.findById(encomendaId).orElseThrow().getSituacao())
                .isEqualTo(SituacaoEncomenda.REGISTRADA);
        assertThat(insumos.findById(suficiente.getId()).orElseThrow().getQuantidade())
                .isEqualByComparingTo("10.000");
        assertThat(insumos.findById(insuficiente.getId()).orElseThrow().getQuantidade())
                .isEqualByComparingTo("2.000");
        assertThat(movimentacoes.count()).isZero();
    }

    @Test
    void clienteComEncomendaNaoPodeSerExcluido() throws Exception {
        Cliente cliente = criarCliente();
        Produto produto = criarProduto();
        Insumo insumo = criarInsumo("Linha", "10");
        Long encomendaId = criarEncomenda(cliente, produto, insumo, "1");

        mockMvc.perform(delete("/api/clientes/{id}", cliente.getId()))
                .andExpect(status().isUnprocessableEntity());
        assertThat(encomendas.findById(encomendaId)).isPresent();
        assertThat(clientes.findById(cliente.getId())).isPresent();
    }

    @Test
    void situacaoInvalidaDevolve409SemMudarEncomenda() throws Exception {
        Cliente cliente = criarCliente();
        Produto produto = criarProduto();
        Insumo insumo = criarInsumo("Linha", "10");
        jdbc.execute("DELETE FROM movimentacoes_estoque");
        Long encomendaId = criarEncomenda(cliente, produto, insumo, "1");

        mockMvc.perform(patch("/api/encomendas/{id}/situacao", encomendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\":\"ENTREGUE\"}"))
                .andExpect(status().isConflict());

        assertThat(encomendas.findById(encomendaId).orElseThrow().getSituacao())
                .isEqualTo(SituacaoEncomenda.REGISTRADA);
        assertThat(movimentacoes.count()).isZero();
    }

    @Test
    void buscaPorIdIncluiClienteEInsumos() throws Exception {
        Cliente cliente = criarCliente();
        Produto produto = criarProduto();
        Insumo insumo = criarInsumo("Linha", "10");
        Long encomendaId = criarEncomenda(cliente, produto, insumo, "2");

        mockMvc.perform(get("/api/encomendas/{id}", encomendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteNome").value(cliente.getNome()))
                .andExpect(jsonPath("$.insumos.length()").value(1))
                .andExpect(jsonPath("$.insumos[0].insumoId").value(insumo.getId().intValue()))
                .andExpect(jsonPath("$.insumos[0].quantidade").value(2));
    }

    @Test
    void cancelarAposProducaoDevolveInsumoAoEstoque() throws Exception {
        Cliente cliente = criarCliente();
        Produto produto = criarProduto();
        Insumo insumo = criarInsumo("Tecido", "10");
        jdbc.execute("DELETE FROM movimentacoes_estoque");
        Long encomendaId = criarEncomenda(cliente, produto, insumo, "3");

        mockMvc.perform(patch("/api/encomendas/{id}/situacao", encomendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\":\"EM_PRODUCAO\"}"))
                .andExpect(status().isOk());
        assertThat(insumos.findById(insumo.getId()).orElseThrow().getQuantidade())
                .isEqualByComparingTo("7.000");

        mockMvc.perform(patch("/api/encomendas/{id}/situacao", encomendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\":\"CANCELADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CANCELADA"))
                .andExpect(jsonPath("$.insumosConsumidos").value(false));

        assertThat(insumos.findById(insumo.getId()).orElseThrow().getQuantidade())
                .isEqualByComparingTo("10.000");
        var movimentos = movimentacoes.listarPorItemId(insumo.getId());
        assertThat(movimentos).hasSize(2);
        assertThat(movimentos.get(1).getTipo()).isEqualTo(br.com.atelie.estoque.TipoMovimentacao.ENTRADA);
        assertThat(movimentos.get(1).getSaldoResultante()).isEqualByComparingTo("10.000");
    }

    @Test
    void cancelarAntesDaProducaoNaoMexeNoEstoque() throws Exception {
        Cliente cliente = criarCliente();
        Produto produto = criarProduto();
        Insumo insumo = criarInsumo("Tecido", "10");
        jdbc.execute("DELETE FROM movimentacoes_estoque");
        Long encomendaId = criarEncomenda(cliente, produto, insumo, "3");

        mockMvc.perform(patch("/api/encomendas/{id}/situacao", encomendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\":\"CANCELADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insumosConsumidos").value(false));

        assertThat(insumos.findById(insumo.getId()).orElseThrow().getQuantidade())
                .isEqualByComparingTo("10.000");
        assertThat(movimentacoes.count()).isZero();
    }

    private Cliente criarCliente() {
        return clientes.saveAndFlush(new Cliente("Cliente D", "11999990000", null));
    }

    private Produto criarProduto() {
        return produtos.saveAndFlush(new Produto("Produto D", UnidadeMedida.UN,
                BigDecimal.ZERO, null, new BigDecimal("100.00")));
    }

    private Insumo criarInsumo(String nome, String saldo) {
        Insumo insumo = new Insumo(nome, UnidadeMedida.M, BigDecimal.ZERO, new BigDecimal("1.00"));
        insumo.registrarEntrada(new BigDecimal(saldo));
        return insumos.saveAndFlush(insumo);
    }

    private Long criarEncomenda(Cliente cliente, Produto produto, Insumo primeiro, String quantidade)
            throws Exception {
        return criarEncomenda(cliente, produto, primeiro, quantidade, null, null);
    }

    private Long criarEncomenda(Cliente cliente, Produto produto, Insumo primeiro, String quantidadePrimeiro,
                                Insumo segundo, String quantidadeSegundo) throws Exception {
        String insumosJson = segundo == null
                ? "\"insumos\":[{\"insumoId\":%d,\"quantidade\":%s}]"
                    .formatted(primeiro.getId(), quantidadePrimeiro)
                : "\"insumos\":[{\"insumoId\":%d,\"quantidade\":%s},{\"insumoId\":%d,\"quantidade\":%s}]"
                    .formatted(primeiro.getId(), quantidadePrimeiro, segundo.getId(), quantidadeSegundo);
        String corpo = """
                {"clienteId":%d,"produtoId":%d,"descricao":"Encomenda de teste",
                 "valor":100.00,"dataPrevistaEntrega":"%s",%s}
                """.formatted(cliente.getId(), produto.getId(), LocalDate.now().plusDays(10), insumosJson);
        String resposta = mockMvc.perform(post("/api/encomendas")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.insumos.length()").value(segundo == null ? 1 : 2))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }
}
