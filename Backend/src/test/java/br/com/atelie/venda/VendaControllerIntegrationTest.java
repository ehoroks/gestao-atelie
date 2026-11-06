package br.com.atelie.venda;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fatia vertical B4: abrir venda, adicionar/remover itens e bloqueio apos finalizar. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VendaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VendaRepository vendas;

    @Test
    void abreVendaSemClienteEAdicionaItem() throws Exception {
        Long vendaId = abrirVenda();

        mockMvc.perform(post("/api/vendas/{id}/itens", vendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":10,"quantidade":2,"precoUnitario":25.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.produtoId").value(10))
                .andExpect(jsonPath("$.precoUnitario").value(25.00))
                .andExpect(jsonPath("$.subtotal").value(50.00));

        mockMvc.perform(get("/api/vendas/{id}", vendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("EM_ABERTO"))
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].produtoId").value(10));
    }

    @Test
    void removeItemEnquantoVendaEstaEmAberto() throws Exception {
        Long vendaId = abrirVenda();
        Long itemId = adicionarItem(vendaId, 10, "1", "12.50");

        mockMvc.perform(delete("/api/vendas/{id}/itens/{itemId}", vendaId, itemId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/vendas/{id}", vendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens.length()").value(0));
    }

    @Test
    void naoPermiteAdicionarItemEmVendaFinalizada() throws Exception {
        Long vendaId = abrirVenda();

        Venda venda = vendas.findById(vendaId).orElseThrow();
        venda.alterarSituacao(SituacaoVenda.CONCLUIDA);
        vendas.saveAndFlush(venda);

        mockMvc.perform(post("/api/vendas/{id}/itens", vendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":10,"quantidade":1,"precoUnitario":10.00}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void abreVendaComClienteInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/api/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":999999}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void adicionarItemEmVendaInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/api/vendas/999999/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":10,"quantidade":1,"precoUnitario":10.00}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void itemInvalidoRetorna400() throws Exception {
        Long vendaId = abrirVenda();

        mockMvc.perform(post("/api/vendas/{id}/itens", vendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":0,"quantidade":0,"precoUnitario":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.produtoId").exists())
                .andExpect(jsonPath("$.campos.quantidade").exists())
                .andExpect(jsonPath("$.campos.precoUnitario").exists());
    }

    @Test
    void aplicaDescontoEDefineFormaDePagamento() throws Exception {
        Long vendaId = abrirVenda();
        adicionarItem(vendaId, 10, "2", "50.00"); // subtotal = 100.00

        mockMvc.perform(put("/api/vendas/{id}/desconto", vendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":15.50}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.desconto").value(15.50));

        mockMvc.perform(put("/api/vendas/{id}/forma-pagamento", vendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formaPagamento":"PIX"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formaPagamento").value("PIX"));

        mockMvc.perform(get("/api/vendas/{id}", vendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.desconto").value(15.50))
                .andExpect(jsonPath("$.formaPagamento").value("PIX"));
    }

    @Test
    void descontoMaiorQueSubtotalRetorna422() throws Exception {
        Long vendaId = abrirVenda();
        adicionarItem(vendaId, 10, "1", "10.00");

        mockMvc.perform(put("/api/vendas/{id}/desconto", vendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":10.01}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void descontoInvalidoRetorna400() throws Exception {
        Long vendaId = abrirVenda();

        mockMvc.perform(put("/api/vendas/{id}/desconto", vendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valor").exists());
    }

    @Test
    void naoAplicaDescontoEmVendaFinalizada() throws Exception {
        Long vendaId = abrirVenda();
        adicionarItem(vendaId, 10, "1", "10.00");

        Venda venda = vendas.findById(vendaId).orElseThrow();
        venda.alterarSituacao(SituacaoVenda.CONCLUIDA);
        vendas.saveAndFlush(venda);

        mockMvc.perform(put("/api/vendas/{id}/desconto", vendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":1.00}
                                """))
                .andExpect(status().isConflict());
    }

    private Long abrirVenda() throws Exception {
        return idDe(mockMvc.perform(post("/api/vendas"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private Long adicionarItem(Long vendaId, long produtoId, String quantidade, String preco) throws Exception {
        String corpo = """
                {"produtoId":%d,"quantidade":%s,"precoUnitario":%s}
                """.formatted(produtoId, quantidade, preco);

        return idDe(mockMvc.perform(post("/api/vendas/{id}/itens", vendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private static Long idDe(String json) {
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }
}
