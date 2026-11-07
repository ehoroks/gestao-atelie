package br.com.atelie.usuario.dto;

import br.com.atelie.usuario.PerfilUsuario;
import br.com.atelie.usuario.Usuario;

import java.time.LocalDateTime;

/** Representacao publica de Usuario: nunca expoe o hash da senha. */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        boolean ativo,
        LocalDateTime criadoEm
) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.isAtivo(),
                usuario.getCriadoEm());
    }
}
