package br.com.atelie.venda;

import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.comum.TransicaoInvalidaException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Trilha B - B4. Agregado de venda.
 *
 * Regra central: so e permitido adicionar/remover itens enquanto a venda esta
 * em {@link SituacaoVenda#EM_ABERTO}. Apos finalizar/cancelar, qualquer tentativa
 * lanca {@link VendaNaoEditavelException}.
 *
 * A venda pode existir sem cliente (venda de balcao), conforme a multiplicidade
 * 0..1 do modelo; por isso `clienteId` e anulavel.
 */
@Entity
@Table(name = "vendas")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 20)
    private SituacaoVenda situacao = SituacaoVenda.EM_ABERTO;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVenda> itens = new ArrayList<>();

    /** Exigido pelo mapeamento ORM. */
    protected Venda() {
    }

    public Venda(Long clienteId) {
        if (clienteId != null && clienteId <= 0) {
            throw new RegraNegocioException("clienteId deve ser positivo quando informado");
        }
        this.clienteId = clienteId;
    }

    @PrePersist
    void aoPersistir() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    // --- operacoes do agregado (B4) ---

    public ItemVenda adicionarItem(Long produtoId, BigDecimal quantidade,
                                   BigDecimal precoAtualDoProduto) {
        exigirEdicaoDeItens();
        ItemVenda item = ItemVenda.comPrecoCongelado(this, produtoId, quantidade, precoAtualDoProduto);
        itens.add(item);
        return item;
    }

    public void removerItem(Long itemId) {
        exigirEdicaoDeItens();
        boolean removido = itens.removeIf(item -> Objects.equals(item.getId(), itemId));
        if (!removido) {
            throw new RecursoNaoEncontradoException("ItemVenda", itemId);
        }
    }

    private void exigirEdicaoDeItens() {
        if (!situacao.permiteEdicaoDeItens()) {
            throw new VendaNaoEditavelException(situacao);
        }
    }

    /**
     * Transicao de estado interna. As operacoes publicas `finalizar()` e
     * `cancelar(...)` entram na B7/B8; aqui a maquina de estados ja e respeitada.
     */
    void alterarSituacao(SituacaoVenda destino) {
        if (!situacao.podeTransicionarPara(destino)) {
            throw new TransicaoInvalidaException(situacao, destino);
        }
        situacao = destino;
    }

    // --- acesso ---

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public SituacaoVenda getSituacao() { return situacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }

    public List<ItemVenda> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
