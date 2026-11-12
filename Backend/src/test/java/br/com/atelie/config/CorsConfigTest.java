package br.com.atelie.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Valida o CORS sem depender de Docker; roda junto com o perfil de testes H2. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void preflightDeOrigemPermitidaRetornaCabecalhosCors() throws Exception {
        mockMvc.perform(options("/api/produtos")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("GET")));
    }

    @Test
    void requisicaoDeOrigemPermitidaRecebeAllowOrigin() throws Exception {
        mockMvc.perform(get("/api/produtos")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void origemNaoPermitidaERejeitada() throws Exception {
        mockMvc.perform(options("/api/produtos")
                        .header("Origin", "http://malicioso.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
