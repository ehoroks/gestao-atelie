package br.com.atelie.aplicacao;

import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.venda.ItemVenda;
import br.com.atelie.venda.ItemVendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Casos de uso da Trilha B (B3): itens de venda com preco congelado.
 *
 * A API REST de itens nasce na B4, junto com o agregado `Venda` e as regras de
 * adicionar/remover apenas enquanto a venda nao estiver finalizada. Aqui fica a
 * base de dominio/persistencia que a B4 vai orquestrar.
 */
@Service
public class ServicoItemVenda {

    private final ItemVendaRepository itens;

    public ServicoItemVenda(ItemVendaRepository itens) {
        this.itens = Objects.requireNonNull(itens, "repositorio de itens de venda e obrigatorio");
    }

    /**
     * Cria o item copiando o preco atual do produto. A partir daqui o valor e
     * imutavel, mesmo que `Produto.preco` mude depois.
     */
    @Transactional
    public ItemVenda adicionar(Long vendaId, Long produtoId,
                               BigDecimal quantidade, BigDecimal precoAtualDoProduto) {
        return itens.save(ItemVenda.comPrecoCongelado(
                vendaId, produtoId, quantidade, precoAtualDoProduto));
    }

    @Transactional(readOnly = true)
    public ItemVenda buscarPorId(Long id) {
        return itens.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("ItemVenda", id));
    }

    @Transactional(readOnly = true)
    public List<ItemVenda> listarPorVenda(Long vendaId) {
        return itens.findByVendaIdOrderByIdAsc(vendaId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularSubtotalDaVenda(Long vendaId) {
        return itens.findByVendaIdOrderByIdAsc(vendaId).stream()
                .map(ItemVenda::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void remover(Long id) {
        itens.delete(buscarPorId(id));
    }
}
