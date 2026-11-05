package br.com.atelie.catalogo;

import br.com.atelie.comum.RegraNegocioException;
import java.math.BigDecimal;
import java.util.Objects;

/** Um insumo e a quantidade necessaria para confeccionar uma peca. */
public final class ItemComposicao {
    private final Insumo insumo;
    private final BigDecimal quantidadeUsada;

    public ItemComposicao(Insumo insumo, BigDecimal quantidadeUsada) {
        this.insumo = Objects.requireNonNull(insumo, "insumo e obrigatorio");
        if (quantidadeUsada == null || quantidadeUsada.signum() <= 0) {
            throw new RegraNegocioException("quantidadeUsada deve ser maior que zero");
        }
        if (!insumo.getUnidadeMedida().aceita(quantidadeUsada)) {
            throw new RegraNegocioException("quantidadeUsada incompatível com a unidade do insumo");
        }
        this.quantidadeUsada = quantidadeUsada;
    }

    /** Custo exato, sem arredondamento antecipado. */
    public BigDecimal calcularCusto() {
        return insumo.getCustoUnitario().multiply(quantidadeUsada);
    }

    public Insumo getInsumo() { return insumo; }
    public BigDecimal getQuantidadeUsada() { return quantidadeUsada; }
}
