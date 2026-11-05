package br.com.atelie.aplicacao;

import br.com.atelie.encomenda.Encomenda;
import br.com.atelie.encomenda.SituacaoEncomenda;

import br.com.atelie.catalogo.ComposicaoPreco;
import br.com.atelie.catalogo.ItemComposicao;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.comum.TransicaoInvalidaException;
import br.com.atelie.estoque.ItemEstoque;
import br.com.atelie.estoque.ServicoEstoque;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

/** Coordenador da integracao I2, sem chamada direta Encomenda -> Estoque. */
@Service
@Profile("estoque-integrado")
public class ServicoEncomenda {
    private final ServicoEstoque estoque;

    public ServicoEncomenda(ServicoEstoque estoque) {
        this.estoque = Objects.requireNonNull(estoque, "servico de estoque e obrigatorio");
    }

    /**
     * Ao entrar em EM_PRODUCAO baixa os insumos da composicao. O contrato
     * ServicoEstoque garante baixa atomica; @Transactional protege a
     * persistencia quando A e B implementarem os repositorios reais.
     */
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
            List<ServicoEstoque.Linha> linhas = montarLinhas(composicao);
            estoque.baixar(linhas, "inicio da producao da encomenda");
        }
        encomenda.atualizarSituacao(destino);
    }

    /** Agrega itens repetidos do mesmo insumo antes da verificacao de saldo. */
    private List<ServicoEstoque.Linha> montarLinhas(ComposicaoPreco composicao) {
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
}
