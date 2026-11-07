package br.com.atelie.cliente.dto;

import br.com.atelie.cliente.Cliente;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String nome,
        String telefone,
        String email,
        LocalDateTime criadoEm
) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getCriadoEm());
    }
}
