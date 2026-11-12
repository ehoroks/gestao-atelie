package br.com.atelie.catalogo.dto;

import br.com.atelie.catalogo.Categoria;
import br.com.atelie.catalogo.Produto;
import br.com.atelie.estoque.UnidadeMedida;
import java.math.BigDecimal;

public record ProdutoResponse(
        Long id, String nome, UnidadeMedida unidadeMedida,
        BigDecimal quantidade, BigDecimal estoqueMinimo, boolean estoqueBaixo,
        Long categoriaId, String categoriaNome,
        String tamanho, String cor, BigDecimal preco,
        String descricao, String foto, boolean ativo
) {
    public static ProdutoResponse de(Produto produto) {
        Categoria categoria = produto.getCategoria();
        return new ProdutoResponse(
                produto.getId(), produto.getNome(), produto.getUnidadeMedida(),
                produto.getQuantidade(), produto.getEstoqueMinimo(), produto.estaComEstoqueBaixo(),
                categoria == null ? null : categoria.getId(),
                categoria == null ? null : categoria.getNome(),
                produto.getTamanho(), produto.getCor(), produto.getPreco(),
                produto.getDescricao(), produto.getFoto(), produto.isAtivo());
    }
}
