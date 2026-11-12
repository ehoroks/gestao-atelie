package br.com.atelie.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS para o frontend Vue consumir a API quando ele nao esta atras do proxy do Vite.
 *
 * <p>As origens vem da variavel de ambiente {@code CORS_ALLOWED_ORIGINS}, em lista
 * separada por virgula. Em desenvolvimento, o proxy do Vite dispensa CORS, mas as
 * origens locais ficam liberadas por padrao para quem abrir o frontend sem proxy.</p>
 *
 * <p>Em producao, informe exatamente o dominio do frontend (por exemplo,
 * {@code https://atelie.exemplo.com}). Evite {@code *} quando a API estiver publica.</p>
 *
 * <p>O bean {@link CorsConfigurationSource} e usado tanto pelo Spring MVC quanto
 * pelo filtro de CORS do Spring Security (Configuracao em {@code SecurityConfig}).</p>
 */
@Configuration
public class CorsConfig {

    private static final List<String> ORIGENS_PADRAO = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173"
    );

    private static final List<String> METODOS = List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    private static final List<String> CABECALHOS = List.of(
            "Accept", "Authorization", "Content-Type");

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:}") String origensConfiguradas) {
        List<String> origens = interpretarOrigens(origensConfiguradas);

        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedMethods(METODOS);
        configuracao.setAllowedHeaders(CABECALHOS);
        configuracao.setMaxAge(3600L);

        if (origens.contains("*")) {
            configuracao.setAllowedOriginPatterns(List.of("*"));
        } else {
            configuracao.setAllowedOrigins(origens);
        }

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", configuracao);
        return fonte;
    }

    private static List<String> interpretarOrigens(String valor) {
        if (valor == null || valor.isBlank()) {
            return ORIGENS_PADRAO;
        }
        return Arrays.stream(valor.split(","))
                .map(String::trim)
                .filter(origem -> !origem.isEmpty())
                .toList();
    }
}
