package br.com.atelie.estoque;

import br.com.atelie.comum.RegraNegocioException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro imutavel. Historico e append-only: nao existe setter nem delete.
 * Criado exclusivamente por ItemEstoque.
 */
public final class MovimentacaoEstoque {

    private Long                   id;
    private final ItemEstoque      item;
    private final TipoMovimentacao tipo;
    private final BigDecimal       quantidade;
    private final BigDecimal       saldoResultante;
    private final LocalDateTime    data;
    private final String           motivo;

    MovimentacaoEstoque(ItemEstoque item,
                        TipoMovimentacao tipo,
                        BigDecimal quantidade,
                        BigDecimal saldoResultante,
                        String motivo) {
        if (tipo == TipoMovimentacao.AJUSTE && (motivo == null || motivo.isBlank())) {
            throw new RegraNegocioException("Ajuste de estoque exige motivo");
        }
        this.item            = Objects.requireNonNull(item);
        this.tipo            = Objects.requireNonNull(tipo);
        this.quantidade      = Objects.requireNonNull(quantidade);
        this.saldoResultante = Objects.requireNonNull(saldoResultante);
        this.motivo          = motivo;
        this.data            = LocalDateTime.now();
    }

    public Long             getId()              { return id; }
    public ItemEstoque      getItem()            { return item; }
    public TipoMovimentacao getTipo()            { return tipo; }
    public BigDecimal       getQuantidade()      { return quantidade; }
    public BigDecimal       getSaldoResultante() { return saldoResultante; }
    public LocalDateTime    getData()            { return data; }
    public String           getMotivo()          { return motivo; }
}
