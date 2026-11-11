package br.com.atelie.estoque;

import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.estoque.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** RF08-RF11 e RF28: saldo, alertas, entradas, ajustes e trilha auditavel. */
@Service
public class ServicoGestaoEstoque {
    private final ItemEstoqueRepository itens;
    private final MovimentacaoEstoqueRepository movimentacoes;

    public ServicoGestaoEstoque(ItemEstoqueRepository itens, MovimentacaoEstoqueRepository movimentacoes) {
        this.itens = itens;
        this.movimentacoes = movimentacoes;
    }

    @Transactional(readOnly = true)
    public List<EstoqueItemResponse> listar(String tipo, Boolean baixo, String nome) {
        if (tipo != null && !tipo.isBlank() && !tipo.equalsIgnoreCase("PRODUTO")
                && !tipo.equalsIgnoreCase("INSUMO")) {
            throw new RegraNegocioException("tipo deve ser PRODUTO ou INSUMO");
        }
        return itens.findAll().stream()
                .filter(i -> tipo == null || tipo.isBlank()
                        || (tipo.equalsIgnoreCase("PRODUTO") == (i instanceof br.com.atelie.catalogo.Produto)))
                .filter(i -> baixo == null || i.estaComEstoqueBaixo() == baixo)
                .filter(i -> nome == null || nome.isBlank()
                        || i.getNome().toLowerCase(Locale.ROOT).contains(nome.trim().toLowerCase(Locale.ROOT)))
                .sorted(Comparator.comparing(ItemEstoque::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(EstoqueItemResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public EstoqueItemResponse buscar(Long id) { return EstoqueItemResponse.de(obter(id)); }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponse> historico(Long id) {
        obter(id); // 404 para id invalido em vez de resposta vazia.
        return movimentacoes.listarPorItemId(id).stream().map(MovimentacaoResponse::de).toList();
    }

    @Transactional
    public EstoqueItemResponse entrada(Long id, MovimentarEstoqueRequest request) {
        ItemEstoque item = bloquear(id);
        int anterior = item.getHistorico().size();
        item.registrarEntrada(request.quantidade(), request.motivo());
        persistirUltimaMovimentacao(item, anterior);
        return EstoqueItemResponse.de(item);
    }

    @Transactional
    public EstoqueItemResponse saida(Long id, MovimentarEstoqueRequest request) {
        ItemEstoque item = bloquear(id);
        int anterior = item.getHistorico().size();
        item.registrarSaida(request.quantidade(), request.motivo());
        persistirUltimaMovimentacao(item, anterior);
        return EstoqueItemResponse.de(item);
    }

    @Transactional
    public EstoqueItemResponse ajustar(Long id, AjustarEstoqueRequest request) {
        ItemEstoque item = bloquear(id);
        int anterior = item.getHistorico().size();
        item.ajustarQuantidade(request.novaQuantidade(), request.motivo());
        persistirUltimaMovimentacao(item, anterior);
        return EstoqueItemResponse.de(item);
    }

    private void persistirUltimaMovimentacao(ItemEstoque item, int tamanhoAnterior) {
        movimentacoes.saveAndFlush(item.getHistorico().get(tamanhoAnterior));
        itens.flush();
    }

    private ItemEstoque obter(Long id) {
        return itens.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Item de estoque", id));
    }

    private ItemEstoque bloquear(Long id) {
        return itens.buscarPorIdParaAtualizacao(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de estoque", id));
    }
}
