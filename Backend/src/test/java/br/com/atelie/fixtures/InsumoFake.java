package br.com.atelie.fixtures;

import br.com.atelie.catalogo.Insumo;
import br.com.atelie.estoque.UnidadeMedida;
import java.math.BigDecimal;

/** Fake minimo da Trilha A; injeta saldo sem chamar metodos TODO-T1. */
public class InsumoFake extends Insumo {
    public InsumoFake(String nome, UnidadeMedida unidade, BigDecimal custo, BigDecimal saldo) {
        super(nome, unidade, BigDecimal.ZERO, custo);
        if (saldo == null || saldo.signum() < 0 || !unidade.aceita(saldo)) {
            throw new IllegalArgumentException("saldo invalido");
        }
        this.quantidade = saldo;
    }
}
