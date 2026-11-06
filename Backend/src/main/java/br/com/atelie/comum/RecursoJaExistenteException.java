package br.com.atelie.comum;

/** Conflito de unicidade (ex.: e-mail ja cadastrado). Mapeia para 409. */
public class RecursoJaExistenteException extends DominioException {

    private static final long serialVersionUID = 1L;

    public RecursoJaExistenteException(String tipo, Object chave) {
        super("%s ja cadastrado: %s".formatted(tipo, chave));
    }
}
