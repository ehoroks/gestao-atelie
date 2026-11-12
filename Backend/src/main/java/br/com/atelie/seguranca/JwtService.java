package br.com.atelie.seguranca;

import br.com.atelie.usuario.Usuario;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Emissao e leitura dos tokens JWT da API.
 *
 * <p>O segredo vem de {@code JWT_SECRET} e precisa ter ao menos 32 caracteres
 * (HS256). Tokens carregam o email (subject) e o perfil do usuario. A aplicacao
 * e stateless: o token e a unica credencial enviada nas requisicoes seguintes.</p>
 */
@Service
public class JwtService {

    private static final String EMISSOR = "cirinha-atelie";
    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    private final JwtEncoder codificador;
    private final JwtDecoder decodificador;
    private final long expiracaoMinutos;

    public JwtService(
            @Value("${app.security.jwt.secret:}") String segredo,
            @Value("${app.security.jwt.expiration-minutes:120}") long expiracaoMinutos) {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException(
                    "JWT_SECRET nao configurado ou curto demais: informe ao menos 32 caracteres "
                            + "(recomendado 64) em uma variavel de ambiente.");
        }

        SecretKey chave = new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.codificador = new NimbusJwtEncoder(new ImmutableSecret<>(chave));

        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withSecretKey(chave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decodificador.setJwtValidator(JwtValidators.createDefaultWithIssuer(EMISSOR));
        this.decodificador = decodificador;
        this.expiracaoMinutos = expiracaoMinutos;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(usuario.getEmail())
                .issuedAt(agora)
                .expiresAt(agora.plus(expiracaoMinutos, ChronoUnit.MINUTES))
                .claim("nome", usuario.getNome())
                .claim("perfil", usuario.getPerfil().name());

        // Usuarios recem-criados (ainda sem id, por exemplo em testes) nao
        // precisam do claim; o subject (email) ja identifica a conta.
        if (usuario.getId() != null) {
            claims.claim("uid", usuario.getId());
        }

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return codificador.encode(JwtEncoderParameters.from(cabecalho, claims.build())).getTokenValue();
    }

    public Jwt decodificar(String token) {
        return decodificador.decode(token);
    }

    public long expiracaoEmSegundos() {
        return expiracaoMinutos * 60;
    }
}
