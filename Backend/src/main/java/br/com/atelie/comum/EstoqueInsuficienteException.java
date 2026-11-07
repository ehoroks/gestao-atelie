package br.com.atelie.comum;

import java.math.BigDecimal;

/** Mapeia para 409. */
public class EstoqueInsuficienteException extends DominioException {

    private static final long serialVersionUID = 1L;

    private final String     nomeItem;
    private final BigDecimal disponivel;
    private final BigDecimal solicitado;

    public EstoqueInsuficienteException(String nomeItem, BigDecimal disponivel, BigDecimal solicitado) {
        super("Estoque insuficiente de '%s': disponivel %s, solicitado %s"
                .formatted(nomeItem, disponivel, solicitado));
        this.nomeItem   = nomeItem;
        this.disponivel = disponivel;
        this.solicitado = solicitado;
    }

    public String     getNomeItem()   { return nomeItem; }
    public BigDecimal getDisponivel() { return disponivel; }
    public BigDecimal getSolicitado() { return solicitado; }
}
