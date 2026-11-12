package br.com.atelie.seguranca;

import br.com.atelie.usuario.Usuario;
import br.com.atelie.usuario.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Regras de acesso da API.
 *
 * <ul>
 *   <li>login e healthcheck sao publicos;</li>
 *   <li>gestao de usuarios e relatorios exigem perfil ADMIN;</li>
 *   <li>demais rotas exigem autenticacao (ADMIN ou ATENDENTE).</li>
 * </ul>
 *
 * <p>A API e stateless: nao usa sessao nem CSRF, pois o token vai no cabecalho
 * {@code Authorization}. Use sempre HTTPS em producao.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   TratadorDeSeguranca tratador) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sessao ->
                        sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(autorizacao -> autorizacao
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/relatorios/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint(tratador)
                        .accessDeniedHandler(tratador))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Evita que o Spring Boot crie um usuario padrao em memoria e deixa a
     * consulta pronta caso o projeto passe a usar autenticacao gerenciada.
     */
    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return email -> {
            Usuario usuario = usuarios.findByEmailIgnoreCase(email)
                    .filter(Usuario::isAtivo)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
            return User.withUsername(usuario.getEmail())
                    .password(usuario.getSenhaHash())
                    .roles(usuario.getPerfil().name())
                    .build();
        };
    }
}
