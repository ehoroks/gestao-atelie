package br.com.atelie.comum;

/** Invariante de dominio violada. Mapeia para 422. */
public class RegraNegocioException extends DominioException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
