package br.com.atelie.api;

import java.time.Instant;
import java.util.Map;

public record ErroResposta(
        Instant momento,
        int status,
        String erro,
        String mensagem,
        Map<String, String> campos
) {

    public static ErroResposta de(int status, String erro, String mensagem) {
        return new ErroResposta(Instant.now(), status, erro, mensagem, Map.of());
    }

    public static ErroResposta de(int status, String erro, String mensagem, Map<String, String> campos) {
        return new ErroResposta(Instant.now(), status, erro, mensagem, campos);
    }
}
