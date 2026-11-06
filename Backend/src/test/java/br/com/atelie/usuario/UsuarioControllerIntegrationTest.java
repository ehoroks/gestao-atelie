package br.com.atelie.usuario;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fatia vertical B1: cadastro -> login ponta a ponta sobre H2 (perfil test). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
                .andExpect(jsonPath("$.email").value("maria@exemplo.com"));
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
