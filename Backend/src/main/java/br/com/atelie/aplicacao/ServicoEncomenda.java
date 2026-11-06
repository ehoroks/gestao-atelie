package br.com.atelie.aplicacao;

import br.com.atelie.catalogo.ComposicaoPreco;
import br.com.atelie.catalogo.Insumo;
import br.com.atelie.catalogo.InsumoRepository;
import br.com.atelie.catalogo.ItemComposicao;
import br.com.atelie.catalogo.Produto;
import br.com.atelie.catalogo.ProdutoRepository;
import br.com.atelie.cliente.Cliente;
import br.com.atelie.cliente.ClienteRepository;
import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.comum.TransicaoInvalidaException;
import br.com.atelie.encomenda.Encomenda;
import br.com.atelie.encomenda.EncomendaRepository;
import br.com.atelie.encomenda.EncomendaRequest;
import br.com.atelie.encomenda.EncomendaResponse;
import br.com.atelie.encomenda.SituacaoEncomenda;
import br.com.atelie.estoque.ItemEstoque;
import br.com.atelie.estoque.ServicoEstoque;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Casos de uso de encomendas e integracao transacional com o estoque. */
@Service
public class ServicoEncomenda {

    private final ServicoEstoque estoque;
    private final EncomendaRepository encomendas;
    private final ClienteRepository clientes;
    private final ProdutoRepository produtos;
    private final InsumoRepository insumos;

    @Autowired
    public ServicoEncomenda(EncomendaRepository encomendas, ClienteRepository clientes,
                            ProdutoRepository produtos, InsumoRepository insumos,
                            ServicoEstoque estoque) {
        this.encomendas = Objects.requireNonNull(encomendas, "repositorio de encomendas obrigatorio");
        this.clientes = Objects.requireNonNull(clientes, "repositorio de clientes obrigatorio");
        this.produtos = Objects.requireNonNull(produtos, "repositorio de produtos obrigatorio");
        this.insumos = Objects.requireNonNull(insumos, "repositorio de insumos obrigatorio");
        this.estoque = Objects.requireNonNull(estoque, "servico de estoque obrigatorio");
    }

    /** Construtor para os testes de dominio isolados da persistencia. */
    ServicoEncomenda(ServicoEstoque estoque) {
        this.encomendas = null;
        this.clientes = null;
        this.produtos = null;
        this.insumos = null;
        this.estoque = Objects.requireNonNull(estoque, "servico de estoque obrigatorio");
    }

    @Transactional
    public EncomendaResponse criar(EncomendaRequest request) {
        Cliente cliente = clientes.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", request.clienteId()));
        Produto produto = request.produtoId() == null ? null : produtos.findById(request.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", request.produtoId()));
        BigDecimal valor = request.valor();
        if (valor == null) {
            if (produto == null) {
                throw new RegraNegocioException("Informe o valor ou um produto para derivar o valor da encomenda");
            }
            valor = produto.getPreco();
        }
        LocalDate dataPedido = request.dataPedido() == null ? LocalDate.now() : request.dataPedido();

        Encomenda encomenda = new Encomenda(cliente, produto, request.descricao(), valor,
                dataPedido, request.dataPrevistaEntrega(), request.observacoes());
        if (request.insumos() != null) {
            for (EncomendaRequest.InsumoConsumo linha : request.insumos()) {
                Insumo insumo = insumos.findById(linha.insumoId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Insumo", linha.insumoId()));
                encomenda.adicionarInsumo(insumo, linha.quantidade());
            }
        }
        return EncomendaResponse.de(encomendas.saveAndFlush(encomenda));
    }

    @Transactional(readOnly = true)
    public EncomendaResponse buscarPorId(Long id) {
        return EncomendaResponse.de(obter(id));
    }

    @Transactional(readOnly = true)
    public List<EncomendaResponse> listar(SituacaoEncomenda situacao, boolean apenasPendentes) {
        List<Encomenda> resultado;
        if (situacao != null) {
            resultado = encomendas.findBySituacaoOrderByIdDesc(situacao);
        } else if (apenasPendentes) {
            List<SituacaoEncomenda> pendentes = Arrays.stream(SituacaoEncomenda.values())
                    .filter(SituacaoEncomenda::isPendente).toList();
            resultado = encomendas.findBySituacaoInOrderByIdDesc(pendentes);
        } else {
            resultado = encomendas.findAllByOrderByIdDesc();
        }
        return resultado.stream().map(EncomendaResponse::de).toList();
    }

    /** D5/D6: transicao e consumo de insumos participam da mesma transacao. */
    @Transactional
    public EncomendaResponse atualizarSituacao(Long id, SituacaoEncomenda destino) {
        Encomenda encomenda = encomendas.buscarPorIdParaAtualizacao(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda", id));
        encomenda.atualizarSituacao(destino);

        if (destino.consomeInsumos() && !encomenda.isInsumosConsumidos()) {
            consumirInsumosPersistidos(encomenda);
            encomenda.marcarInsumosConsumidos();
        }
        return EncomendaResponse.de(encomenda);
    }

    /** Integracao de dominio C mantida para os testes com composicoes em memoria. */
    @Transactional
    public synchronized void atualizarSituacao(Encomenda encomenda, SituacaoEncomenda destino,
                                               ComposicaoPreco composicao) {
        Objects.requireNonNull(encomenda, "encomenda e obrigatoria");
        if (!encomenda.getSituacao().podeTransicionarPara(destino)) {
            throw new TransicaoInvalidaException(encomenda.getSituacao(), destino);
        }
        if (destino == SituacaoEncomenda.EM_PRODUCAO) {
            if (composicao == null || composicao.getProduto() != encomenda.getProduto()) {
                throw new RegraNegocioException("composicao da encomenda e obrigatoria");
            }
            estoque.baixar(montarLinhas(composicao), "inicio da producao da encomenda");
        }
        encomenda.atualizarSituacao(destino);
    }

    private void consumirInsumosPersistidos(Encomenda encomenda) {
        Map<ItemEstoque, BigDecimal> agrupados = new IdentityHashMap<>();
        encomenda.getInsumos().stream()
                .sorted(Comparator.comparing(item -> item.getInsumo().getId()))
                .forEach(item -> {
                    Insumo insumo = insumos.buscarPorIdParaAtualizacao(item.getInsumo().getId())
                            .orElseThrow(() -> new RecursoNaoEncontradoException(
                                    "Insumo", item.getInsumo().getId()));
                    agrupados.merge(insumo, item.getQuantidade(), BigDecimal::add);
                });

        List<ServicoEstoque.Linha> linhas = new ArrayList<>();
        agrupados.forEach((insumo, quantidade) -> linhas.add(new ServicoEstoque.Linha(insumo, quantidade)));
        if (!linhas.isEmpty()) {
            estoque.baixar(linhas, "consumo pela encomenda #" + encomenda.getId());
        }
    }

    private static List<ServicoEstoque.Linha> montarLinhas(ComposicaoPreco composicao) {
        if (composicao.getItens().isEmpty()) {
            throw new RegraNegocioException("composicao sem materiais");
        }
        Map<ItemEstoque, BigDecimal> agrupados = new IdentityHashMap<>();
        for (ItemComposicao item : composicao.getItens()) {
            agrupados.merge(item.getInsumo(), item.getQuantidadeUsada(), BigDecimal::add);
        }
        List<ServicoEstoque.Linha> linhas = new ArrayList<>();
        agrupados.forEach((insumo, quantidade) -> linhas.add(new ServicoEstoque.Linha(insumo, quantidade)));
        return List.copyOf(linhas);
    }

    private Encomenda obter(Long id) {
        return encomendas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda", id));
    }
}
