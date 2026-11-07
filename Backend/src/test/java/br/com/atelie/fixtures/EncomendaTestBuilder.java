package br.com.atelie.fixtures;

import br.com.atelie.catalogo.Insumo;
import br.com.atelie.catalogo.Produto;
import br.com.atelie.cliente.Cliente;
import br.com.atelie.encomenda.Encomenda;
import br.com.atelie.encomenda.EncomendaInsumo;
import br.com.atelie.encomenda.EncomendaRepository;
import br.com.atelie.encomenda.EncomendaRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EncomendaTestBuilder {

    private Cliente cliente;
    private Produto produto;
    private String descricao = "Encomenda de teste";
    private BigDecimal valor = new BigDecimal("150.00");
    private LocalDate dataPedido = LocalDate.now();
    private LocalDate previsaoEntrega = LocalDate.now().plusDays(10);
    private String observacoes;
    private final List<InsumoLinha> linhas = new ArrayList<>();

    public static EncomendaTestBuilder umaEncomenda() {
        return new EncomendaTestBuilder();
    }

    public EncomendaTestBuilder doCliente(Cliente cliente) {
        this.cliente = cliente;
        return this;
    }

    public EncomendaTestBuilder doProduto(Produto produto) {
        this.produto = produto;
        return this;
    }

    public EncomendaTestBuilder comDescricao(String descricao) {
        this.descricao = descricao;
        return this;
    }

    public EncomendaTestBuilder comValor(String valor) {
        this.valor = new BigDecimal(valor);
        return this;
    }

    public EncomendaTestBuilder comDatas(LocalDate dataPedido, LocalDate previsaoEntrega) {
        this.dataPedido = dataPedido;
        this.previsaoEntrega = previsaoEntrega;
        return this;
    }

    public EncomendaTestBuilder comObservacoes(String observacoes) {
        this.observacoes = observacoes;
        return this;
    }

    public EncomendaTestBuilder consumindo(Insumo insumo, String quantidade) {
        linhas.add(new InsumoLinha(insumo, new BigDecimal(quantidade)));
        return this;
    }

    public Encomenda build() {
        Encomenda encomenda = new Encomenda(cliente, produto, descricao, valor,
                dataPedido, previsaoEntrega, observacoes);
        linhas.forEach(linha -> encomenda.adicionarInsumo(linha.insumo(), linha.quantidade()));
        return encomenda;
    }

    public Encomenda persistir(EncomendaRepository encomendas) {
        return encomendas.saveAndFlush(build());
    }

    public EncomendaRequest comoRequest() {
        return new EncomendaRequest(cliente.getId(), produto.getId(), descricao, valor,
                dataPedido, previsaoEntrega, observacoes,
                linhas.stream().map(linha -> new EncomendaRequest.InsumoConsumo(
                        linha.insumo().getId(), linha.quantidade())).toList());
    }

    private record InsumoLinha(Insumo insumo, BigDecimal quantidade) { }
}
