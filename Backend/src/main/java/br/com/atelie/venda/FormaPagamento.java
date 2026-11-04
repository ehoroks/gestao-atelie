package br.com.atelie.venda;

public enum FormaPagamento {

    PIX     ("PIX"),
    DINHEIRO("Dinheiro"),
    CARTAO  ("Cartao"),
    OUTROS  ("Outros");

    private final String descricao;

    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
}
