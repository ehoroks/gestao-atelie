package br.com.atelie.aplicacao;

import br.com.atelie.cliente.ClienteRepository;
import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.venda.FormaPagamento;
import br.com.atelie.venda.ItemVenda;
import br.com.atelie.venda.Venda;
import br.com.atelie.venda.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Casos de uso da Trilha B (B4): agregado Venda e manipulacao de itens.
 *
 * Toda alteracao de item passa por `Venda.adicionarItem`/`removerItem`, que
 * bloqueiam a operacao quando a venda nao esta em aberto.
 */
@Service
public class ServicoVenda {

    private final VendaRepository vendas;
    private final ClienteRepository clientes;

    public ServicoVenda(VendaRepository vendas, ClienteRepository clientes) {
        this.vendas = Objects.requireNonNull(vendas, "repositorio de vendas e obrigatorio");
        this.clientes = Objects.requireNonNull(clientes, "repositorio de clientes e obrigatorio");
    }

    @Transactional
    public Venda abrir(Long clienteId) {
        if (clienteId != null && !clientes.existsById(clienteId)) {
            throw new RecursoNaoEncontradoException("Cliente", clienteId);
        }
        return vendas.save(new Venda(clienteId));
    }

    @Transactional(readOnly = true)
    public Venda buscarPorId(Long id) {
        return vendas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda", id));
    }

    @Transactional(readOnly = true)
    public List<Venda> listar() {
        return vendas.findAllByOrderByIdAsc();
    }

    @Transactional
    public ItemVenda adicionarItem(Long vendaId, Long produtoId,
                                   BigDecimal quantidade, BigDecimal precoAtualDoProduto) {
        Venda venda = buscarPorId(vendaId);
        ItemVenda item = venda.adicionarItem(produtoId, quantidade, precoAtualDoProduto);
        // A venda ja esta gerenciada; flush cascateia o PERSIST e devolve o id ao item.
        vendas.flush();
        return item;
    }

    @Transactional
    public void removerItem(Long vendaId, Long itemId) {
        Venda venda = buscarPorId(vendaId);
        venda.removerItem(itemId);
        vendas.flush();
    }

    @Transactional
    public Venda aplicarDesconto(Long vendaId, BigDecimal valor) {
        Venda venda = buscarPorId(vendaId);
        venda.aplicarDesconto(valor);
        vendas.flush();
        return venda;
    }

    @Transactional
    public Venda definirFormaPagamento(Long vendaId, FormaPagamento formaPagamento) {
        Venda venda = buscarPorId(vendaId);
        venda.definirFormaPagamento(formaPagamento);
        vendas.flush();
        return venda;
    }
}
