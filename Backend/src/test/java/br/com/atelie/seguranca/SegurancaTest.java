package br.com.atelie.seguranca;

import br.com.atelie.aplicacao.ServicoUsuario;
import br.com.atelie.usuario.PerfilUsuario;
import br.com.atelie.usuario.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fatia vertical H: autenticacao JWT, autorizacao por perfil e rotas publicas.
 *
 * <p>Roda com H2 no perfil de testes, sem Docker, e cobre o fluxo real de login
 * com token. Os testes de integracao das trilhas anteriores continuam usando
 * PostgreSQL via Testcontainers.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SegurancaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServicoUsuario servico;

    @Autowired
    private UsuarioRepository usuarios;

    @BeforeEach
    void preparar() {
        SecurityContextHolder.clearContext();
        usuarios.deleteAll();
    }

    @Test
    void rotaProtegidaSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").value("Autenticacao obrigatoria"));
    }

    @Test
    void healthcheckPermanecePublico() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void loginEmiteTokenQueAcessaRotaProtegida() throws Exception {
        servico.cadastrar("Admin", "admin@exemplo.com", "senha12345", PerfilUsuario.ADMIN);

        String token = login("admin@exemplo.com", "senha12345");

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@exemplo.com"));
    }

    @Test
    void atendenteNaoAcessaGestaoDeUsuarios() throws Exception {
        servico.cadastrar("Atendente", "atendente@exemplo.com", "senha12345", PerfilUsuario.ATENDENTE);

        String token = login("atendente@exemplo.com", "senha12345");

        mockMvc.perform(get("/api/produtos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem").value("Acesso negado para este perfil"));
    }

    @Test
    void adminAcessaRelatorios() throws Exception {
        servico.cadastrar("Admin", "admin@exemplo.com", "senha12345", PerfilUsuario.ADMIN);

        String token = login("admin@exemplo.com", "senha12345");
        String hoje = LocalDate.now().toString();

        mockMvc.perform(get("/api/relatorios/vendas")
                        .param("inicio", hoje)
                        .param("fim", hoje)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void tokenInvalidoRetorna401() throws Exception {
        mockMvc.perform(get("/api/produtos")
                        .header("Authorization", "Bearer token-que-nao-e-um-jwt"))
                .andExpect(status().isUnauthorized());
    }

    private String login(String email, String senha) throws Exception {
        String corpo = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s"}
                                """.formatted(email, senha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(corpo, "$.token");
    }
}
