package br.com.atelie.encomenda;

import java.util.Set;

/**
 * REGISTRADA -> EM_PRODUCAO -> PRONTA -> ENTREGUE
 * Qualquer estado nao-terminal pode ir para CANCELADA.
 * ENTREGUE e CANCELADA sao terminais.
 */
public enum SituacaoEncomenda {

    REGISTRADA,
    EM_PRODUCAO,
    PRONTA,
    ENTREGUE,
    CANCELADA;

    private static final Set<SituacaoEncomenda> TERMINAIS = Set.of(ENTREGUE, CANCELADA);

    public boolean podeTransicionarPara(SituacaoEncomenda destino) {
        if (destino == null || isTerminal()) return false;
        if (destino == CANCELADA)            return true;
        return switch (this) {
            case REGISTRADA  -> destino == EM_PRODUCAO;
            case EM_PRODUCAO -> destino == PRONTA;
            case PRONTA      -> destino == ENTREGUE;
            default          -> false;
        };
    }

    public boolean isTerminal()  { return TERMINAIS.contains(this); }
    public boolean isPendente()  { return !isTerminal(); }

    /** Momento em que os insumos sao consumidos (integracao com ServicoEstoque). */
    public boolean consomeInsumos() { return this == EM_PRODUCAO; }
}
