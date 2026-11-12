package br.com.atelie.config;

import br.com.atelie.infra.teste.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Confere o CORS contra o PostgreSQL real usado no CI (Testcontainers). */
class CorsConfigIT extends IntegrationTestBase {

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
