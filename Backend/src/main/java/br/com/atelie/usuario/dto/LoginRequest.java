package br.com.atelie.usuario.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "email e obrigatorio")
        String email,

        @NotBlank(message = "senha e obrigatoria")
        String senha
) {
}
