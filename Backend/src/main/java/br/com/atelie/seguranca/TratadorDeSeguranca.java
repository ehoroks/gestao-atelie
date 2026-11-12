package br.com.atelie.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Respostas JSON consistentes com o restante da API quando o Spring Security
 * barra a requisicao: 401 sem token valido e 403 sem permissao de perfil.
 */
@Component
public class TratadorDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        escrever(response, 401, "Unauthorized", "Autenticacao obrigatoria");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        escrever(response, 403, "Forbidden", "Acesso negado para este perfil");
    }

    private static void escrever(HttpServletResponse response, int status,
                                 String erro, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("""
                {"momento":"%s","status":%d,"erro":"%s","mensagem":"%s","campos":{}}
                """.formatted(Instant.now(), status, erro, mensagem));
    }
}
