package br.com.atelie.encomenda;

import br.com.atelie.catalogo.Insumo;
import br.com.atelie.catalogo.InsumoRepository;
import br.com.atelie.catalogo.Produto;
import br.com.atelie.catalogo.ProdutoRepository;
import br.com.atelie.cliente.Cliente;
import br.com.atelie.cliente.ClienteRepository;
import br.com.atelie.estoque.MovimentacaoEstoqueRepository;
import br.com.atelie.estoque.UnidadeMedida;
import br.com.atelie.fixtures.ClienteTestBuilder;
import br.com.atelie.fixtures.EncomendaTestBuilder;
import br.com.atelie.fixtures.InsumoTestBuilder;
import br.com.atelie.fixtures.ProdutoTestBuilder;
import br.com.atelie.infra.teste.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EncomendaIT extends IntegrationTestBase {

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
    void criaEncomendaEConsomeEstoqueNaTransicaoParaProducao() throws Exception {
        Cliente cliente = criarCliente();
        Produto produto = criarProduto();
        Insumo tecido = criarInsumo("Tecido", "100");
        Long encomendaId = criarEncomenda(cliente, produto,
                List.of(new EncomendaRequest.InsumoConsumo(tecido.getId(), new BigDecimal("30.5"))));
        movimentacoes.deleteAll();

        atualizar(encomendaId, SituacaoEncomenda.EM_PRODUCAO)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("EM_PRODUCAO"))
                .andExpect(jsonPath("$.insumosConsumidos").value(true));

        assertThat(insumos.findById(tecido.getId()).orElseThrow().getQuantidade())
                .isEqualByComparingTo("69.500");
        assertThat(movimentacoes.listarPorItemId(tecido.getId())).hasSize(1);

        atualizar(encomendaId, SituacaoEncomenda.PRONTA).andExpect(status().isOk());
        assertThat(movimentacoes.listarPorItemId(tecido.getId())).hasSize(1);
    }

    @Test
    void faltaDeEstoqueRolaDeVoltaEstadoSaldoEHistoricoCompleto() throws Exception {
        Cliente cliente = criarCliente();
        Produto produto = criarProduto();
        Insumo tecido = criarInsumo("Tecido", "100");
        Insumo botoes = criarInsumo("Botoes", "2");
        Long encomendaId = criarEncomenda(cliente, produto, List.of(
                new EncomendaRequest.InsumoConsumo(tecido.getId(), new BigDecimal("30")),
                new EncomendaRequest.InsumoConsumo(botoes.getId(), new BigDecimal("5"))));
        movimentacoes.deleteAll();

        atualizar(encomendaId, SituacaoEncomenda.EM_PRODUCAO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").exists());

        assertThat(encomendas.findById(encomendaId).orElseThrow().getSituacao())
                .isEqualTo(SituacaoEncomenda.REGISTRADA);
        assertThat(insumos.findById(tecido.getId()).orElseThrow().getQuantidade())
                .isEqualByComparingTo("100.000");
        assertThat(insumos.findById(botoes.getId()).orElseThrow().getQuantidade())
                .isEqualByComparingTo("2.000");
        assertThat(movimentacoes.count()).isZero();
    }

    @Test
    void transicaoInvalidaNaoMudaBancoEClienteComEncomendaNaoPodeSerExcluido() throws Exception {
        Cliente cliente = criarCliente();
        Produto produto = criarProduto();
        Insumo tecido = criarInsumo("Tecido", "10");
        Long encomendaId = criarEncomenda(cliente, produto,
                List.of(new EncomendaRequest.InsumoConsumo(tecido.getId(), BigDecimal.ONE)));

        atualizar(encomendaId, SituacaoEncomenda.ENTREGUE)
                .andExpect(status().isConflict());
        assertThat(encomendas.findById(encomendaId).orElseThrow().getSituacao())
                .isEqualTo(SituacaoEncomenda.REGISTRADA);

        mockMvc.perform(delete("/api/clientes/{id}", cliente.getId()))
                .andExpect(status().isUnprocessableEntity());
        assertThat(clientes.findById(cliente.getId())).isPresent();
    }

    @Test
    void aceitaPecaSobMedidaSemProdutoQuandoValorForInformado() throws Exception {
        Cliente cliente = criarCliente();
        String corpo = """
                {"clienteId":%d,"descricao":"Peça sob medida","valor":275.50,
                 "dataPrevistaEntrega":"%s"}
                """.formatted(cliente.getId(), LocalDate.now().plusDays(10));

        String resposta = mockMvc.perform(post("/api/encomendas")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.produtoNome").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.valor").value(275.50))
                .andReturn().getResponse().getContentAsString();
        Long id = ((Number) com.jayway.jsonpath.JsonPath.read(resposta, "$.id")).longValue();
        assertThat(encomendas.findById(id).orElseThrow().getProduto()).isNull();
    }

    @Test
    void derivaValorDoProdutoQuandoRequestNaoInformaValor() throws Exception {
        Cliente cliente = criarCliente();
        Produto produto = criarProduto();
        String corpo = """
                {"clienteId":%d,"produtoId":%d,"descricao":"Produto do catálogo",
                 "dataPrevistaEntrega":"%s"}
                """.formatted(cliente.getId(), produto.getId(), LocalDate.now().plusDays(10));

        mockMvc.perform(post("/api/encomendas")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(150.00));
    }

    @Test
    void rejeitaEncomendaSemProdutoENemValor() throws Exception {
        Cliente cliente = criarCliente();
        String corpo = """
                {"clienteId":%d,"descricao":"Peça sob medida",
                 "dataPrevistaEntrega":"%s"}
                """.formatted(cliente.getId(), LocalDate.now().plusDays(10));

        mockMvc.perform(post("/api/encomendas")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableEntity());
    }

    private Cliente criarCliente() {
        return ClienteTestBuilder.umCliente().comNome("Cliente de encomenda")
                .comTelefone("11999990000").persistir(clientes);
    }

    private Produto criarProduto() {
        return ProdutoTestBuilder.umProduto().comNome("Produto de encomenda")
                .comUnidade(UnidadeMedida.UN).comPreco("150.00").persistir(produtos);
    }

    private Insumo criarInsumo(String nome, String saldo) {
        return InsumoTestBuilder.umInsumo().comNome(nome).comUnidade(UnidadeMedida.M)
                .comCustoUnitario("0.50").comQuantidade(saldo).persistir(insumos);
    }

    private Long criarEncomenda(Cliente cliente, Produto produto,
                                List<EncomendaRequest.InsumoConsumo> linhas) throws Exception {
        EncomendaTestBuilder builder = EncomendaTestBuilder.umaEncomenda()
                .doCliente(cliente).doProduto(produto).comValor("200.00")
                .comDatas(LocalDate.now(), LocalDate.now().plusDays(10))
                .comDescricao("Bolsa sob medida").comObservacoes("teste D");
        for (EncomendaRequest.InsumoConsumo linha : linhas) {
            builder.consumindo(insumos.findById(linha.insumoId()).orElseThrow(),
                    linha.quantidade().toPlainString());
        }
        EncomendaRequest request = builder.comoRequest();
        String linhasJson = String.join(",", request.insumos().stream()
                .map(item -> "{\"insumoId\":%d,\"quantidade\":%s}"
                        .formatted(item.insumoId(), item.quantidade().toPlainString()))
                .toList());
        String corpo = """
                {"clienteId":%d,"produtoId":%d,"descricao":"Bolsa sob medida",
                 "valor":200.00,"dataPedido":"%s","dataPrevistaEntrega":"%s",
                 "observacoes":"teste D","insumos":[%s]}
                """.formatted(request.clienteId(), request.produtoId(), request.dataPedido(),
                        request.dataPrevistaEntrega(), linhasJson);
        String resposta = mockMvc.perform(post("/api/encomendas")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("REGISTRADA"))
                .andReturn().getResponse().getContentAsString();
        return ((Number) com.jayway.jsonpath.JsonPath.read(resposta, "$.id")).longValue();
    }

    private org.springframework.test.web.servlet.ResultActions atualizar(Long id, SituacaoEncomenda situacao)
            throws Exception {
        return mockMvc.perform(patch("/api/encomendas/{id}/situacao", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"situacao\":\"%s\"}".formatted(situacao)));
    }
}
