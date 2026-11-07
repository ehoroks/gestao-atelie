package br.com.atelie.estoque;

import java.math.BigDecimal;

public enum UnidadeMedida {

    UN("Unidade", false),
    M ("Metro",   true),
    G ("Grama",   true);

    private final String  descricao;
    private final boolean fracionavel;

    UnidadeMedida(String descricao, boolean fracionavel) {
        this.descricao   = descricao;
        this.fracionavel = fracionavel;
    }

    public String  getDescricao()   { return descricao; }
    public boolean isFracionavel()  { return fracionavel; }

    /** UN nao aceita 2,5 unidades. M e G aceitam. */
    public boolean aceita(BigDecimal quantidade) {
        if (quantidade == null) return false;
        return fracionavel || quantidade.stripTrailingZeros().scale() <= 0;
    }
}
