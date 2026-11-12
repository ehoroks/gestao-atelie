package br.com.atelie.catalogo.dto;

import br.com.atelie.catalogo.Categoria;

public record CategoriaResponse(Long id, String nome) {
    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }
}
