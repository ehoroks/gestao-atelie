package br.com.atelie.seguranca;

import br.com.atelie.usuario.PerfilUsuario;
import br.com.atelie.usuario.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testes unitarios da emissao/leitura do JWT, sem subir o Spring. */
class JwtServiceTest {

    private static final String SEGREDO =
            "segredo-de-teste-com-mais-de-32-caracteres-para-hs256";

    private final JwtService servico = new JwtService(SEGREDO, 30);

    @Test
    void gerarEDecodificarTokenPreservaEmailEPerfil() {
        Usuario usuario = new Usuario("Maria", "maria@exemplo.com", "hash", PerfilUsuario.ATENDENTE);

        String token = servico.gerarToken(usuario);
        Jwt jwt = servico.decodificar(token);

        assertThat(token).isNotBlank();
        assertThat(jwt.getSubject()).isEqualTo("maria@exemplo.com");
        assertThat(jwt.getClaimAsString("nome")).isEqualTo("Maria");
        assertThat(jwt.getClaimAsString("perfil")).isEqualTo("ATENDENTE");
        assertThat(jwt.getExpiresAt()).isNotNull();
    }

    @Test
    void tokenAdulteradoERejeitado() {
        Usuario usuario = new Usuario("Maria", "maria@exemplo.com", "hash", PerfilUsuario.ADMIN);
        String token = servico.gerarToken(usuario);
        String adulterado = token.substring(0, token.length() - 2) + "xy";

        assertThatThrownBy(() -> servico.decodificar(adulterado))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void segredoAusenteFalhaRapido() {
        assertThatThrownBy(() -> new JwtService("", 30))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void segredoCurtoFalhaRapido() {
        assertThatThrownBy(() -> new JwtService("curto", 30))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void expiracaoEmSegundosDerivaDosMinutos() {
        assertThat(servico.expiracaoEmSegundos()).isEqualTo(30 * 60);
    }
}
