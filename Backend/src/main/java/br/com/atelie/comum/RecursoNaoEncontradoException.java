package br.com.atelie.comum;

/** Mapeia para 404. */
public class RecursoNaoEncontradoException extends DominioException {

    private static final long serialVersionUID = 1L;

    public RecursoNaoEncontradoException(String tipo, Object id) {
        super("%s nao encontrado: %s".formatted(tipo, id));
    }
}
