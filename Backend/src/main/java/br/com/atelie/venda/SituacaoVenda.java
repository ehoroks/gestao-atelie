package br.com.atelie.venda;

import java.util.Set;

/**
 * ATENCAO - DIVERGENCIA DO DIAGRAMA.
 * O diagrama lista apenas CONCLUIDA e CANCELADA. Sem um estado inicial,
 * adicionarItem/removerItem nao tem como ser bloqueado apos finalizar().
 * EM_ABERTO foi acrescentado. Confirmar no Sprint 0 - ambiguidade 10.
 */
public enum SituacaoVenda {

    EM_ABERTO,
    CONCLUIDA,
    CANCELADA;

    private static final Set<SituacaoVenda> TERMINAIS = Set.of(CANCELADA);

    public boolean podeTransicionarPara(SituacaoVenda destino) {
        return switch (this) {
            case EM_ABERTO -> destino == CONCLUIDA || destino == CANCELADA;
            case CONCLUIDA -> destino == CANCELADA;
            case CANCELADA -> false;
        };
    }

    public boolean isTerminal()  { return TERMINAIS.contains(this); }
    public boolean permiteEdicaoDeItens() { return this == EM_ABERTO; }
}
