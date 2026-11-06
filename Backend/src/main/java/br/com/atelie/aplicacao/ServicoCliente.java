package br.com.atelie.aplicacao;

import br.com.atelie.cliente.Cliente;
import br.com.atelie.cliente.ClienteRepository;
import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.encomenda.EncomendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/** Casos de uso da Trilha B (B2): CRUD e busca de Cliente. */
@Service
public class ServicoCliente {

    private final ClienteRepository clientes;
    private final EncomendaRepository encomendas;

    @Autowired
    public ServicoCliente(ClienteRepository clientes, EncomendaRepository encomendas) {
        this.clientes = Objects.requireNonNull(clientes, "repositorio de clientes e obrigatorio");
        this.encomendas = Objects.requireNonNull(encomendas, "repositorio de encomendas e obrigatorio");
    }

    /** Construtor para testes do caso de uso isolado. */
    public ServicoCliente(ClienteRepository clientes) {
        this.clientes = Objects.requireNonNull(clientes, "repositorio de clientes e obrigatorio");
        this.encomendas = null;
    }

    @Transactional
    public Cliente cadastrar(String nome, String telefone, String email) {
        return clientes.save(new Cliente(nome, telefone, email));
    }

    @Transactional
    public Cliente atualizar(Long id, String nome, String telefone, String email) {
        Cliente cliente = buscarPorId(id);
        cliente.atualizarDados(nome, telefone, email);
        return clientes.save(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clientes.findAll(Sort.by(Sort.Direction.ASC, "nome"));
    }

    /**
     * Com termo vazio devolve todos os clientes. Caso contrario busca por
     * trecho do nome ou por trecho dos digitos do telefone.
     */
    @Transactional(readOnly = true)
    public List<Cliente> buscar(String termo) {
        if (termo == null || termo.isBlank()) {
            return listar();
        }
        String termoNormalizado = termo.trim();
        String digitos = termoNormalizado.replaceAll("\\D", "");
        return clientes.buscarPorNomeOuTelefone(termoNormalizado, digitos);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarPorId(id);
        if (encomendas != null && encomendas.existsByCliente_Id(id)) {
            throw new RegraNegocioException("Nao e possivel excluir cliente com encomendas");
        }
        clientes.delete(cliente);
    }
}
