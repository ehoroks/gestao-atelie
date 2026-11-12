package br.com.atelie.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Libera o frontend para consumir a API quando ele nao esta atras do proxy do Vite.
 *
 * <p>As origens vem da variavel de ambiente {@code CORS_ALLOWED_ORIGINS}, em lista
 * separada por virgula. Em desenvolvimento, o proxy do Vite dispensa CORS, mas as
 * origens locais ficam liberadas por padrao para quem abrir o frontend sem proxy.</p>
 *
 * <p>Em producao, informe exatamente o dominio do frontend (por exemplo,
 * {@code https://atelie.exemplo.com}). Evite {@code *} quando a API estiver publica.</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final List<String> ORIGENS_PADRAO = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173"
    );

    private final List<String> origensPermitidas;

    public CorsConfig(@Value("${app.cors.allowed-origins:}") String origensConfiguradas) {
        this.origensPermitidas = interpretarOrigens(origensConfiguradas);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var registro = registry.addMapping("/api/**")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Accept", "Authorization", "Content-Type")
                .maxAge(3600);

        if (origensPermitidas.contains("*")) {
            registro.allowedOriginPatterns("*");
        } else {
            registro.allowedOrigins(origensPermitidas.toArray(String[]::new));
        }
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
