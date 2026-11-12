package br.com.atelie.usuario.dto;

import br.com.atelie.usuario.Usuario;

/** Resposta do login: token JWT + dados publicos do usuario autenticado. */
public record LoginResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        UsuarioResponse usuario
) {

    public static LoginResponse de(Usuario usuario, String token, long expiraEmSegundos) {
        return new LoginResponse(token, "Bearer", expiraEmSegundos, UsuarioResponse.de(usuario));
    }
}
