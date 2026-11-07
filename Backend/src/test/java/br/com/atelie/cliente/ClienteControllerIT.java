package br.com.atelie.cliente;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import br.com.atelie.infra.teste.IntegrationTestBase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fatia vertical B2: CRUD e busca de Cliente com PostgreSQL Testcontainers. */
class ClienteControllerIT extends IntegrationTestBase {

    @Test
    void cadastrarEBuscarPorNomeETelefone() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Maria Silva",
                                  "telefone": "(11) 98765-4321",
                                  "email": "Maria@Exemplo.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.telefone").value("11987654321"))
                .andExpect(jsonPath("$.email").value("maria@exemplo.com"));

        mockMvc.perform(get("/api/clientes").param("termo", "mari"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Maria Silva"));

        mockMvc.perform(get("/api/clientes").param("termo", "98765"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].telefone").value("11987654321"));
    }

    @Test
    void atualizarCliente() throws Exception {
        Long id = cadastrar("Carlos", "(21) 3333-4444", null);

        mockMvc.perform(put("/api/clientes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Carlos Eduardo","telefone":"(21) 99888-7766","email":"carlos@exemplo.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Carlos Eduardo"))
                .andExpect(jsonPath("$.telefone").value("21998887766"))
                .andExpect(jsonPath("$.email").value("carlos@exemplo.com"));
    }

    @Test
    void excluirClienteEDepoisRetornar404() throws Exception {
        Long id = cadastrar("Ana", "(31) 3222-1111", null);

        mockMvc.perform(delete("/api/clientes/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/clientes/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void payloadInvalidoRetorna400ComCampos() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","telefone":"","email":"invalido"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.telefone").exists())
                .andExpect(jsonPath("$.campos.email").exists());
    }

    @Test
    void telefoneComDigitosInvalidosRetorna422() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","telefone":"1234","email":null}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void clienteInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/clientes/999999"))
                .andExpect(status().isNotFound());
    }

    private Long cadastrar(String nome, String telefone, String email) throws Exception {
        String corpo = """
                {"nome":"%s","telefone":"%s","email":%s}
                """.formatted(nome, telefone, email == null ? "null" : "\"" + email + "\"");

        String resposta = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }
}
