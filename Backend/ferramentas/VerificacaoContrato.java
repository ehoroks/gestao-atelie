import br.com.atelie.catalogo.*;
import br.com.atelie.comum.*;
import br.com.atelie.encomenda.SituacaoEncomenda;
import br.com.atelie.estoque.*;
import br.com.atelie.venda.SituacaoVenda;
import java.math.BigDecimal;

public class VerificacaoContrato {
    static int ok = 0, fail = 0;
    static void check(String nome, boolean cond) {
        if (cond) { ok++; } else { fail++; System.out.println("FALHOU: " + nome); }
    }
    static void esperaErro(String nome, Runnable r) {
        try { r.run(); fail++; System.out.println("FALHOU (nao lancou): " + nome); }
        catch (DominioException e) { ok++; }
    }

    public static void main(String[] a) {
        // unidade nao fracionavel
        check("UN rejeita 2.5",  !UnidadeMedida.UN.aceita(new BigDecimal("2.5")));
        check("UN aceita 3",      UnidadeMedida.UN.aceita(new BigDecimal("3.00")));
        check("M aceita 2.5",     UnidadeMedida.M.aceita(new BigDecimal("2.5")));

        // maquina de estados da encomenda
        check("REGISTRADA->EM_PRODUCAO", SituacaoEncomenda.REGISTRADA.podeTransicionarPara(SituacaoEncomenda.EM_PRODUCAO));
        check("REGISTRADA->ENTREGUE bloq", !SituacaoEncomenda.REGISTRADA.podeTransicionarPara(SituacaoEncomenda.ENTREGUE));
        check("PRONTA->CANCELADA",  SituacaoEncomenda.PRONTA.podeTransicionarPara(SituacaoEncomenda.CANCELADA));
        check("ENTREGUE terminal", !SituacaoEncomenda.ENTREGUE.podeTransicionarPara(SituacaoEncomenda.CANCELADA));
        check("EM_PRODUCAO consome", SituacaoEncomenda.EM_PRODUCAO.consomeInsumos());

        // venda
        check("EM_ABERTO edita",   SituacaoVenda.EM_ABERTO.permiteEdicaoDeItens());
        check("CONCLUIDA nao edita", !SituacaoVenda.CONCLUIDA.permiteEdicaoDeItens());
        check("CONCLUIDA->CANCELADA", SituacaoVenda.CONCLUIDA.podeTransicionarPara(SituacaoVenda.CANCELADA));

        // validacoes do construtor
        esperaErro("nome vazio",      () -> new Insumo("  ", UnidadeMedida.G, BigDecimal.ZERO, BigDecimal.ONE));
        esperaErro("custo negativo",  () -> new Insumo("Linha", UnidadeMedida.G, BigDecimal.ZERO, new BigDecimal("-1")));
        esperaErro("minimo negativo", () -> new Insumo("Linha", UnidadeMedida.G, new BigDecimal("-5"), BigDecimal.ONE));

        Insumo tecido = new Insumo("Tecido", UnidadeMedida.M, new BigDecimal("10"), new BigDecimal("12.50"));
        check("historico vazio e imutavel", tecido.getHistorico().isEmpty());
        esperaErro("saida de zero",      () -> tecido.registrarSaida(BigDecimal.ZERO));
        esperaErro("ajuste sem motivo",  () -> tecido.ajustarQuantidade(new BigDecimal("5"), " "));

        Produto p = new Produto("Vestido", UnidadeMedida.UN, BigDecimal.ONE, new Categoria("Festa"), new BigDecimal("199.90"));
        esperaErro("UN fracionada", () -> p.registrarEntrada(new BigDecimal("1.5")));

        // TODOs ainda nao implementados
        try { p.estaDisponivel(); fail++; System.out.println("FALHOU: estaDisponivel devia ser TODO"); }
        catch (UnsupportedOperationException e) { ok++; }

        System.out.println("\n" + ok + " ok, " + fail + " falhas");
        if (fail > 0) System.exit(1);
    }
}
