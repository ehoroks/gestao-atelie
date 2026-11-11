package br.com.atelie.catalogo;

import br.com.atelie.infra.teste.IntegrationTestBase;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Fluxos reais via PostgreSQL + Flyway + MockMvc (mvn verify, Docker ativo). */
class TrilhaAControllerIT extends IntegrationTestBase {

    @Test
    void cadastroBuscaEdicaoEDesativacaoDeProduto() throws Exception {
        Long categoria = categoria("Vestidos");
        Long produto = produto("Vestido Aurora", categoria);

        mockMvc.perform(get("/api/produtos").param("nome", "aurora").param("categoriaId", categoria.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoriaNome").value("Vestidos"))
                .andExpect(jsonPath("$[0].estoqueBaixo").value(true));

        mockMvc.perform(put("/api/produtos/{id}", produto).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Vestido Aurora Novo","categoriaId":%d,"preco":139.90,
                                 "tamanho":"M","cor":"Azul","descricao":"Feito a mao","foto":null}
                                """.formatted(categoria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preco").value(139.90))
                .andExpect(jsonPath("$.cor").value("Azul"));

        mockMvc.perform(patch("/api/produtos/{id}/desativar", produto))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mockMvc.perform(get("/api/produtos").param("ativo", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void categoriaDuplicadaEExclusaoComProdutoSaoRejeitadas() throws Exception {
        Long categoria = categoria("Saias");
        mockMvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"saias\"}"))
                .andExpect(status().isConflict());
        produto("Saia longa", categoria);
        mockMvc.perform(delete("/api/categorias/{id}", categoria))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void entradaSaidaAjusteAlertaEHistoricoPersistido() throws Exception {
        Long insumo = insumo("Tecido algodao");
        mockMvc.perform(post("/api/estoque/{id}/entradas", insumo).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":5,\"motivo\":\"compra\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(5));

        mockMvc.perform(get("/api/estoque/baixo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/api/estoque/{id}/saidas", insumo).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":4,\"motivo\":\"uso\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estoqueBaixo").value(true));

        mockMvc.perform(get("/api/estoque/baixo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").value("Tecido algodao"));

        mockMvc.perform(post("/api/estoque/{id}/ajustes", insumo).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novaQuantidade\":8,\"motivo\":\"inventario\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(8));

        mockMvc.perform(get("/api/estoque/{id}/movimentacoes", insumo))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].tipo").value("ENTRADA"))
                .andExpect(jsonPath("$[1].tipo").value("SAIDA"))
                .andExpect(jsonPath("$[2].tipo").value("AJUSTE"))
                .andExpect(jsonPath("$[2].motivo").value("inventario"));
    }

    @Test
    void saidaInsuficienteNaoAlteraSaldo() throws Exception {
        Long insumo = insumo("Linha branca");
        mockMvc.perform(post("/api/estoque/{id}/saidas", insumo).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":9}"))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/estoque/{id}", insumo))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(0));
        mockMvc.perform(get("/api/estoque/{id}/movimentacoes", insumo))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void validaPayloadPrecoFiltroEUnidadeDeMedida() throws Exception {
        mockMvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"\",\"estoqueMinimo\":-1,\"preco\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists());
        mockMvc.perform(get("/api/produtos").param("precoMin", "20").param("precoMax", "5"))
                .andExpect(status().isUnprocessableEntity());
        Long insumo = insumo("Botao");
        mockMvc.perform(post("/api/estoque/{id}/entradas", insumo).contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantidade\":1.5}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void atualizaCustoDeInsumo() throws Exception {
        Long id = insumo("Elastico");
        mockMvc.perform(patch("/api/insumos/{id}/custo", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"custoUnitario\":3.7500}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.custoUnitario").value(3.75));
    }

    private Long categoria(String nome) throws Exception {
        String json = mockMvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    private Long produto(String nome, Long categoriaId) throws Exception {
        String json = mockMvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"%s","unidadeMedida":"UN","estoqueMinimo":2,
                                 "categoriaId":%d,"preco":99.90,"tamanho":"P","cor":"Rosa"}
                                """.formatted(nome, categoriaId)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    private Long insumo(String nome) throws Exception {
        // UN para validar que nao aceita quantidade fracionaria.
        String json = mockMvc.perform(post("/api/insumos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"%s","unidadeMedida":"UN","estoqueMinimo":2,
                                 "custoUnitario":1.50}
                                """.formatted(nome)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }
}
