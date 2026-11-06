package br.com.atelie.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Codificacao de senha da Trilha B (B1).
 *
 * Usamos apenas o modulo `spring-security-crypto` (BCrypt) nesta fatia.
 * O `spring-boot-starter-security` completo (filtros, JWT, autorizacao por
 * perfil) fica para uma etapa posterior, para nao bloquear os endpoints atuais.
 */
@Configuration
public class ConfiguracaoSenha {

    @Bean
    public PasswordEncoder codificadorDeSenha() {
        return new BCryptPasswordEncoder();
    }
}
