package br.com.atelie.seguranca;

import br.com.atelie.aplicacao.ServicoUsuario;
import br.com.atelie.usuario.PerfilUsuario;
import br.com.atelie.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro usuario ADMIN a partir de variaveis de ambiente.
 *
 * <p>Sem isso nao ha como entrar no sistema em um banco novo, porque todas as
 * rotas de gestao de usuarios exigem autenticacao. O processo e idempotente:
 * se o email ja existir, nada e alterado.</p>
 *
 * <p>Variaveis: {@code ADMIN_EMAIL}, {@code ADMIN_SENHA} (minimo 8 caracteres)
 * e, opcionalmente, {@code ADMIN_NOME}. A senha nunca e registrada em log.</p>
 */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final ServicoUsuario servico;
    private final UsuarioRepository usuarios;
    private final String email;
    private final String senha;
    private final String nome;

    public AdminInicial(ServicoUsuario servico,
                        UsuarioRepository usuarios,
                        @Value("${app.admin.email:}") String email,
                        @Value("${app.admin.senha:}") String senha,
                        @Value("${app.admin.nome:Administrador}") String nome) {
        this.servico = servico;
        this.usuarios = usuarios;
        this.email = email;
        this.senha = senha;
        this.nome = nome;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            log.info("Bootstrap de admin desativado: defina ADMIN_EMAIL e ADMIN_SENHA para criar o primeiro usuario.");
            return;
        }

        if (usuarios.existsByEmailIgnoreCase(email.trim())) {
            log.info("Usuario admin inicial ja existe; bootstrap ignorado.");
            return;
        }

        servico.cadastrar(nome, email, senha, PerfilUsuario.ADMIN);
        log.info("Usuario admin inicial criado para {}.", email.trim());
    }
}
