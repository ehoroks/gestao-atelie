package br.com.atelie.aplicacao;

import br.com.atelie.cliente.ClienteRepository;
import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.venda.FormaPagamento;
import br.com.atelie.venda.ItemVenda;
import br.com.atelie.venda.Venda;
import br.com.atelie.venda.VendaRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Casos de uso da Trilha B (B4-B7): agregado Venda, manipulacao de itens,
 * desconto/pagamento e finalizacao.
 *
 * Toda alteracao de item passa por `Venda.adicionarItem`/`removerItem`, que
 * bloqueiam a operacao quando a venda nao esta em aberto.
 *
 * A finalizacao (B7) orquestra a integracao I1 atraves da porta
 * {@link EstoqueDaVenda}. Sem implementacao da porta, a operacao falha com
 * HTTP 503 em vez de concluir a venda sem baixar o estoque.
 */
@Service
public class ServicoVenda {

    private final VendaRepository vendas;
    private final ClienteRepository clientes;
    private final EstoqueDaVenda estoqueDaVenda;

    @Autowired
    public ServicoVenda(VendaRepository vendas, ClienteRepository clientes,
                        ObjectProvider<EstoqueDaVenda> estoqueDaVenda) {
        this(vendas, clientes, Objects.requireNonNull(
                estoqueDaVenda, "provedor da porta de estoque e obrigatorio").getIfAvailable());
    }

    private ServicoVenda(VendaRepository vendas, ClienteRepository clientes,
                         EstoqueDaVenda estoqueDaVenda) {
        this.vendas = Objects.requireNonNull(vendas, "repositorio de vendas e obrigatorio");
        this.clientes = Objects.requireNonNull(clientes, "repositorio de clientes e obrigatorio");
        this.estoqueDaVenda = estoqueDaVenda;
    }

    /** Construtor mantido para testes de unidade e usos sem contexto Spring. */
    ServicoVenda(VendaRepository vendas, ClienteRepository clientes) {
        this(vendas, clientes, (EstoqueDaVenda) null);
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

    /**
     * B7: finaliza a venda (validacoes no dominio) e dispara a baixa de
     * estoque pela porta {@link EstoqueDaVenda}.
     *
     * Tudo roda na mesma transacao: se a baixa falhar com
     * `EstoqueInsuficienteException`, o rollback desfaz a transicao da venda.
     */
    @Transactional
    public Venda finalizar(Long vendaId) {
        Venda venda = buscarPorId(vendaId);
        venda.validarFinalizacao();
        if (estoqueDaVenda == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Integracao com estoque indisponivel; venda nao foi finalizada");
        }

        estoqueDaVenda.baixarItensDaVenda(venda);
        venda.finalizar();

        vendas.flush();
        return venda;
    }
}
