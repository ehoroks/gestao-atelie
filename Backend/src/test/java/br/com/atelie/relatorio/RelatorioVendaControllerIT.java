package br.com.atelie.relatorio;

import br.com.atelie.catalogo.Produto;
import br.com.atelie.catalogo.ProdutoRepository;
import br.com.atelie.estoque.UnidadeMedida;
import br.com.atelie.infra.teste.IntegrationTestBase;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RelatorioVendaControllerIT extends IntegrationTestBase {

    @Autowired
    private ProdutoRepository produtos;

    @Test
    void relatoriosConsideramVendasConcluidasFaturamentoELimiteDeProdutos() throws Exception {
        LocalDate hoje = LocalDate.now();
        Long produtoA = criarProduto("Produto A");
        Long produtoB = criarProduto("Produto B");
        Long vendaA = criarVendaConcluida(produtoA, "2", "10.00", "5.00");
        criarVendaConcluida(produtoB, "3", "5.00", null);
        Long vendaCancelada = criarVendaConcluida(produtoB, "4", "5.00", null);
        mockMvc.perform(post("/api/vendas/{id}/cancelar", vendaCancelada)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"data":"%sT12:00:00","motivo":"cancelada no teste"}
                                """.formatted(hoje)))
                .andExpect(status().isOk());
        criarVendaAberta(produtoA);

        String vendasConcluidas = mockMvc.perform(get("/api/relatorios/vendas")
                        .param("inicio", hoje.toString()).param("fim", hoje.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        List<?> ids = JsonPath.read(vendasConcluidas, "$[*].id");
        assertThat(ids.stream().map(id -> ((Number) id).longValue()).toList()).contains(vendaA);

        mockMvc.perform(get("/api/relatorios/vendas/faturamento")
                        .param("inicio", hoje.toString()).param("fim", hoje.toString())
                        .param("agruparPor", "PERIODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].faturamento").value(30.00))
                .andExpect(jsonPath("$[0].quantidadeVendas").value(2));

        mockMvc.perform(get("/api/relatorios/vendas/produtos-mais-vendidos")
                        .param("inicio", hoje.toString()).param("fim", hoje.toString())
                        .param("limite", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].produtoId").value(produtoB.intValue()))
                .andExpect(jsonPath("$[0].quantidadeVendida").value(3.000))
                .andExpect(jsonPath("$[0].valorVendido").value(15.00));
    }

    @Test
    void faturamentoDiarioIncluiDiasSemVendasEAgrupaPorSemanaEMes() throws Exception {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusDays(2);
        Long produto = criarProduto("Produto do periodo");
        criarVendaConcluida(produto, "1", "12.00", null);

        mockMvc.perform(get("/api/relatorios/vendas/faturamento")
                        .param("inicio", inicio.toString()).param("fim", hoje.toString())
                        .param("agruparPor", "DIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].faturamento").value(0))
                .andExpect(jsonPath("$[1].faturamento").value(0))
                .andExpect(jsonPath("$[2].faturamento").value(12.00));

        mockMvc.perform(get("/api/relatorios/vendas/faturamento")
                        .param("inicio", inicio.toString()).param("fim", hoje.toString())
                        .param("agruparPor", "SEMANA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].faturamento").value(12.00));

        mockMvc.perform(get("/api/relatorios/vendas/faturamento")
                        .param("inicio", inicio.toString()).param("fim", hoje.toString())
                        .param("agruparPor", "MES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].faturamento").value(12.00));
    }

    @Test
    void rejeitaPeriodoInvertidoELimiteDeProdutosInvalido() throws Exception {
        LocalDate hoje = LocalDate.now();
        LocalDate ontem = hoje.minusDays(1);

        mockMvc.perform(get("/api/relatorios/vendas/faturamento")
                        .param("inicio", hoje.toString()).param("fim", ontem.toString())
                        .param("agruparPor", "DIA"))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/api/relatorios/vendas/produtos-mais-vendidos")
                        .param("inicio", hoje.toString()).param("fim", hoje.toString())
                        .param("limite", "0"))
                .andExpect(status().isUnprocessableEntity());
    }

    private Long criarProduto(String nome) {
        Produto produto = new Produto(nome, UnidadeMedida.UN, BigDecimal.ZERO,
                null, new BigDecimal("10.00"));
        produto.registrarEntrada(new BigDecimal("100"));
        return produtos.saveAndFlush(produto).getId();
    }

    private Long criarVendaConcluida(Long produtoId, String quantidade, String preco, String desconto)
            throws Exception {
        Long vendaId = abrirVenda();
        mockMvc.perform(post("/api/vendas/{id}/itens", vendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":%d,"quantidade":%s,"precoUnitario":%s}
                                """.formatted(produtoId, quantidade, preco)))
                .andExpect(status().isCreated());
        if (desconto != null) {
            mockMvc.perform(put("/api/vendas/{id}/desconto", vendaId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"valor\":%s}".formatted(desconto)))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(put("/api/vendas/{id}/forma-pagamento", vendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formaPagamento":"PIX"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/vendas/{id}/finalizar", vendaId))
                .andExpect(status().isOk());
        return vendaId;
    }

    private void criarVendaAberta(Long produtoId) throws Exception {
        Long vendaId = abrirVenda();
        mockMvc.perform(post("/api/vendas/{id}/itens", vendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":%d,"quantidade":1,"precoUnitario":10.00}
                                """.formatted(produtoId)))
                .andExpect(status().isCreated());
    }

    private Long abrirVenda() throws Exception {
        String json = mockMvc.perform(post("/api/vendas"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }
}
