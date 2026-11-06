package br.com.atelie.comum;

/** Transicao de estado nao permitida (venda ou encomenda). Mapeia para 409. */
public class TransicaoInvalidaException extends DominioException {

    private static final long serialVersionUID = 1L;

    public TransicaoInvalidaException(Enum<?> origem, Enum<?> destino) {
        super("Transicao invalida: %s -> %s".formatted(origem, destino));
    }
}
