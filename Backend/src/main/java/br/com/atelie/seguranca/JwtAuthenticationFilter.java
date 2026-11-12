package br.com.atelie.seguranca;

import br.com.atelie.usuario.PerfilUsuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Le o cabecalho {@code Authorization: Bearer <token>} e popula o
 * {@link SecurityContextHolder} quando o JWT e valido.
 *
 * <p>Um token invalido ou expirado apenas nao autentica a requisicao; a decisao
 * de responder 401 fica a cargo do {@link SecurityConfig}. Isso permite que
 * rotas publicas (login e healthcheck) continuem acessiveis.</p>
 *
 * <p>Nao e um bean: e instanciado dentro da cadeia do Spring Security em
 * {@link SecurityConfig}, evitando registro duplicado no servlet container.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");

        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            String token = cabecalho.substring(PREFIXO.length()).trim();
            try {
                Jwt jwt = jwtService.decodificar(token);
                String perfil = jwt.getClaimAsString("perfil");
                if (jwt.getSubject() == null || perfil == null) {
                    throw new IllegalArgumentException("Token sem subject ou perfil");
                }
                PerfilUsuario.valueOf(perfil); // rejeita perfis desconhecidos

                var autorizacao = new SimpleGrantedAuthority("ROLE_" + perfil);
                var autenticacao = new UsernamePasswordAuthenticationToken(
                        jwt.getSubject(), null, List.of(autorizacao));
                autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
