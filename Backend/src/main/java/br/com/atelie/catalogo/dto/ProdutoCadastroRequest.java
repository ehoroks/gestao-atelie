package br.com.atelie.catalogo.dto;

import br.com.atelie.estoque.UnidadeMedida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProdutoCadastroRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotNull UnidadeMedida unidadeMedida,
        @NotNull @DecimalMin("0.000") @Digits(integer = 9, fraction = 3) BigDecimal estoqueMinimo,
        @Positive Long categoriaId,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal preco,
        @Size(max = 40) String tamanho,
        @Size(max = 60) String cor,
        @Size(max = 1000) String descricao,
        @Size(max = 500) String foto
) { }
