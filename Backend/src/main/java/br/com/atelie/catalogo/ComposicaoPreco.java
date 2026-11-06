package br.com.atelie.catalogo;

import br.com.atelie.comum.RegraNegocioException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Formula acordada para a Trilha C (ambiguidade 3):
 * preco = custoMateriais * (1 + custosIndiretos/100) * (1 + lucro/100).
 * Aqui "margem" significa ACRESCIMO sobre o custo (markup), e nao margem
 * liquida sobre o preco final. Revisar com a cliente antes da homologacao.
 */
public class ComposicaoPreco {
    private static final BigDecimal CEM = new BigDecimal("100");

    private final Produto produto;
    private final List<ItemComposicao> itens = new ArrayList<>();
    private final BigDecimal percentualCustosIndiretos;
    private final BigDecimal margemLucro;
    private BigDecimal precoAjustado;

    public ComposicaoPreco(Produto produto, BigDecimal percentualCustosIndiretos,
                          BigDecimal margemLucro) {
        this.produto = Objects.requireNonNull(produto, "produto e obrigatorio");
        this.percentualCustosIndiretos = exigirPercentual(percentualCustosIndiretos, "custos indiretos");
        this.margemLucro = exigirPercentual(margemLucro, "margem de lucro");
    }

    private static BigDecimal exigirPercentual(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() < 0) {
            throw new RegraNegocioException(campo + " deve ser maior ou igual a zero");
        }
        return valor;
    }

    public void adicionarMaterial(Insumo insumo, BigDecimal quantidadeUsada) {
        itens.add(new ItemComposicao(insumo, quantidadeUsada));
        // Um novo material torna o ajuste anterior desatualizado.
        precoAjustado = null;
    }

    public BigDecimal calcularCustoMateriais() {
        return itens.stream().map(ItemComposicao::calcularCusto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularPrecoSugerido() {
        if (itens.isEmpty()) {
            throw new RegraNegocioException("a composicao deve conter ao menos um material");
        }
        BigDecimal base = calcularCustoMateriais();
        BigDecimal fatorIndiretos = BigDecimal.ONE.add(percentualCustosIndiretos.divide(CEM));
        BigDecimal fatorLucro = BigDecimal.ONE.add(margemLucro.divide(CEM));
        return base.multiply(fatorIndiretos).multiply(fatorLucro)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Sobrescreve o preco sugerido. Zero e permitido pelo contrato atual de Produto. */
    public void ajustarPreco(BigDecimal valor) {
        if (itens.isEmpty()) {
            throw new RegraNegocioException("nao e possivel ajustar uma composicao vazia");
        }
        if (valor == null || valor.signum() < 0) {
            throw new RegraNegocioException("preco ajustado nao pode ser negativo");
        }
        precoAjustado = valor.setScale(2, RoundingMode.HALF_UP);
    }

    /** Integra I3 sem expor publicamente o setter de Produto. */
    public void aplicarComoPrecoDo(Produto destino) {
        if (destino != produto) {
            throw new RegraNegocioException("composicao pertence a outro produto");
        }
        BigDecimal precoFinal = precoAjustado != null ? precoAjustado : calcularPrecoSugerido();
        destino.definirPreco(precoFinal);
    }

    public Produto getProduto() { return produto; }
    public List<ItemComposicao> getItens() { return List.copyOf(itens); }
    public BigDecimal getPercentualCustosIndiretos() { return percentualCustosIndiretos; }
    public BigDecimal getMargemLucro() { return margemLucro; }
    public BigDecimal getPrecoAjustado() { return precoAjustado; }
}
