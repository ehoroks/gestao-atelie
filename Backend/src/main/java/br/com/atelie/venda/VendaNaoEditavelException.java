package br.com.atelie.venda;

import br.com.atelie.comum.DominioException;

/** Tentativa de alterar itens de uma venda ja finalizada/cancelada. Mapeia para 409. */
public class VendaNaoEditavelException extends DominioException {

    private static final long serialVersionUID = 1L;

    public VendaNaoEditavelException(SituacaoVenda situacao) {
        super("Venda na situacao %s nao permite alterar itens".formatted(situacao));
    }
}
