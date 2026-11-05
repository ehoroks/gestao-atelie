package br.com.atelie.fixtures;

import br.com.atelie.catalogo.Categoria;
import br.com.atelie.catalogo.Produto;
import br.com.atelie.estoque.UnidadeMedida;
import java.math.BigDecimal;

/** Fake minimo da Trilha A. Nao substitui testes reais do estoque. */
public class ProdutoFake extends Produto {
    public ProdutoFake(String nome, BigDecimal preco) {
        super(nome, UnidadeMedida.UN, BigDecimal.ONE, new Categoria("Roupas"), preco);
    }

    public void comSaldo(int unidades) {
        if (unidades < 0) throw new IllegalArgumentException("saldo negativo");
        this.quantidade = BigDecimal.valueOf(unidades);
    }
}
