package br.com.atelie.comum;

/**
 * Raiz de toda excecao de regra de negocio.
 * O tratador global (Trilha 4) traduz esta hierarquia em HTTP.
 */
public abstract class DominioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected DominioException(String mensagem) {
        super(mensagem);
    }
}
