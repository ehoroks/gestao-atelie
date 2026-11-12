package br.com.atelie.usuario;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import br.com.atelie.infra.teste.IntegrationTestBase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fatia vertical B1: cadastro -> login ponta a ponta sobre PostgreSQL Testcontainers. */
class UsuarioControllerIT extends IntegrationTestBase {

    @Test
    void cadastrarEAutenticar() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Maria",
                                  "email": "Maria@Exemplo.com",
                                  "senha": "senha12345",
                                  "perfil": "ATENDENTE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria@exemplo.com"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "maria@exemplo.com",
                                  "senha": "senha12345"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEmSegundos").isNumber())
                .andExpect(jsonPath("$.usuario.email").value("maria@exemplo.com"))
                .andExpect(jsonPath("$.usuario.senhaHash").doesNotExist());
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Joao","email":"joao@exemplo.com","senha":"senha12345","perfil":"ADMIN"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"joao@exemplo.com","senha":"senhaErrada"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cadastroDuplicadoRetorna409() throws Exception {
        String corpo = """
                {"nome":"Duplicado","email":"duplicado@exemplo.com","senha":"senha12345","perfil":"ATENDENTE"}
                """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isConflict());
    }

    @Test
    void payloadInvalidoRetorna400ComCampos() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","email":"invalido","senha":"curta","perfil":"ATENDENTE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    void usuarioInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/usuarios/999999"))
                .andExpect(status().isNotFound());
    }
}
