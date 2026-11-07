package br.com.atelie.estoque;

public enum TipoMovimentacao {

    ENTRADA(true),
    SAIDA  (false),
    AJUSTE (true);

    /** AJUSTE pode somar ou subtrair; o sinal vem na quantidade registrada. */
    private final boolean somaAoSaldo;

    TipoMovimentacao(boolean somaAoSaldo) {
        this.somaAoSaldo = somaAoSaldo;
    }

    public boolean isSomaAoSaldo() { return somaAoSaldo; }
}
