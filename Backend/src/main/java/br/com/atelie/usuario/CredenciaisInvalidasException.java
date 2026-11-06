package br.com.atelie.usuario;

import br.com.atelie.comum.DominioException;

/** Credenciais invalidas no login. Mapeia para 401. */
public class CredenciaisInvalidasException extends DominioException {

    private static final long serialVersionUID = 1L;

    public CredenciaisInvalidasException() {
        super("Email ou senha invalidos");
    }
}
