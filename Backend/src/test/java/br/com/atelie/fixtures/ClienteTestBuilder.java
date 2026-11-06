package br.com.atelie.fixtures;

import br.com.atelie.cliente.Cliente;
import br.com.atelie.cliente.ClienteRepository;

public class ClienteTestBuilder {

    private String nome = "Cliente de teste";
    private String telefone = "11999990000";
    private String email;

    public static ClienteTestBuilder umCliente() {
        return new ClienteTestBuilder();
    }

    public ClienteTestBuilder comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public ClienteTestBuilder comTelefone(String telefone) {
        this.telefone = telefone;
        return this;
    }

    public ClienteTestBuilder comEmail(String email) {
        this.email = email;
        return this;
    }

    public Cliente build() {
        return new Cliente(nome, telefone, email);
    }

    public Cliente persistir(ClienteRepository clientes) {
        return clientes.saveAndFlush(build());
    }
}
