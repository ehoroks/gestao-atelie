package br.com.atelie.aplicacao;

import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.relatorio.AgrupamentoFaturamento;
import br.com.atelie.relatorio.dto.FaturamentoPeriodoResponse;
import br.com.atelie.relatorio.dto.ProdutoMaisVendidoResponse;
import br.com.atelie.venda.ItemVenda;
import br.com.atelie.venda.SituacaoVenda;
import br.com.atelie.venda.Venda;
import br.com.atelie.venda.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Consultas e agregacoes de relatorios sobre vendas concluidas. */
@Service
public class ServicoRelatorioVenda {

    private static final int LIMITE_MAXIMO_PRODUTOS = 100;
    private final VendaRepository vendas;

    public ServicoRelatorioVenda(VendaRepository vendas) {
        this.vendas = Objects.requireNonNull(vendas, "repositorio de vendas obrigatorio");
    }

    @Transactional(readOnly = true)
    public List<Venda> listarVendas(LocalDate inicio, LocalDate fim) {
        return buscarConcluidas(inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<FaturamentoPeriodoResponse> faturamento(
            LocalDate inicio, LocalDate fim, AgrupamentoFaturamento agrupamento) {
        validarPeriodo(inicio, fim);
        Objects.requireNonNull(agrupamento, "agrupamento obrigatorio");
        List<Venda> concluidas = buscarConcluidas(inicio, fim);

        Map<LocalDate, Totais> totaisPorGrupo = new HashMap<>();
        for (Venda venda : concluidas) {
            LocalDate dia = venda.getCriadoEm().toLocalDate();
            LocalDate chave = chaveDoGrupo(dia, inicio, agrupamento);
            totaisPorGrupo.merge(chave,
                    new Totais(venda.calcularValorFinal(), 1), Totais::somar);
        }

        return intervalos(inicio, fim, agrupamento).entrySet().stream()
                .map(intervalo -> {
                    Totais totais = totaisPorGrupo.getOrDefault(intervalo.getKey(), Totais.ZERO);
                    return new FaturamentoPeriodoResponse(
                            intervalo.getValue().inicio(), intervalo.getValue().fim(),
                            totais.valor(), totais.quantidadeVendas());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProdutoMaisVendidoResponse> produtosMaisVendidos(
            LocalDate inicio, LocalDate fim, int limite) {
        validarPeriodo(inicio, fim);
        if (limite < 1 || limite > LIMITE_MAXIMO_PRODUTOS) {
            throw new RegraNegocioException(
                    "limite deve estar entre 1 e %d".formatted(LIMITE_MAXIMO_PRODUTOS));
        }

        Map<Long, TotaisProduto> totaisPorProduto = new HashMap<>();
        for (Venda venda : buscarConcluidas(inicio, fim)) {
            for (ItemVenda item : venda.getItens()) {
                totaisPorProduto.merge(item.getProdutoId(),
                        new TotaisProduto(item.getQuantidade(), item.calcularSubtotal()),
                        TotaisProduto::somar);
            }
        }

        return totaisPorProduto.entrySet().stream()
                .map(entry -> new ProdutoMaisVendidoResponse(entry.getKey(),
                        entry.getValue().quantidade(), entry.getValue().valor()))
                .sorted(Comparator.comparing(ProdutoMaisVendidoResponse::quantidadeVendida)
                        .reversed()
                        .thenComparing(ProdutoMaisVendidoResponse::valorVendido, Comparator.reverseOrder())
                        .thenComparing(ProdutoMaisVendidoResponse::produtoId))
                .limit(limite)
                .toList();
    }

    private List<Venda> buscarConcluidas(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        LocalDateTime inicioDoDia = inicio.atStartOfDay();
        LocalDateTime inicioDoDiaSeguinteAoFim = fim.plusDays(1).atStartOfDay();
        return vendas.findAllBySituacaoAndCriadoEmGreaterThanEqualAndCriadoEmLessThanOrderByCriadoEmAscIdAsc(
                SituacaoVenda.CONCLUIDA, inicioDoDia, inicioDoDiaSeguinteAoFim);
    }

    private static void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new RegraNegocioException("inicio e fim do periodo sao obrigatorios");
        }
        if (fim.isBefore(inicio)) {
            throw new RegraNegocioException("fim do periodo nao pode ser anterior ao inicio");
        }
    }

    private static LocalDate chaveDoGrupo(LocalDate data, LocalDate inicio,
                                          AgrupamentoFaturamento agrupamento) {
        return switch (agrupamento) {
            case DIA -> data;
            case SEMANA -> data.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MES -> data.withDayOfMonth(1);
            case PERIODO -> inicio;
        };
    }

    /** Inclui grupos vazios e limita intervalos parciais as datas solicitadas. */
    private static TreeMap<LocalDate, Intervalo> intervalos(
            LocalDate inicio, LocalDate fim, AgrupamentoFaturamento agrupamento) {
        TreeMap<LocalDate, Intervalo> intervalos = new TreeMap<>();
        switch (agrupamento) {
            case PERIODO -> intervalos.put(inicio, new Intervalo(inicio, fim));
            case DIA -> {
                for (LocalDate data = inicio; !data.isAfter(fim); data = data.plusDays(1)) {
                    intervalos.put(data, new Intervalo(data, data));
                }
            }
            case SEMANA -> {
                LocalDate semana = inicio.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                while (!semana.isAfter(fim)) {
                    LocalDate comeco = max(semana, inicio);
                    LocalDate termino = min(semana.plusDays(6), fim);
                    intervalos.put(semana, new Intervalo(comeco, termino));
                    semana = semana.plusWeeks(1);
                }
            }
            case MES -> {
                YearMonth mes = YearMonth.from(inicio);
                YearMonth ultimoMes = YearMonth.from(fim);
                while (!mes.isAfter(ultimoMes)) {
                    LocalDate comecoDoMes = mes.atDay(1);
                    LocalDate fimDoMes = mes.atEndOfMonth();
                    intervalos.put(comecoDoMes,
                            new Intervalo(max(comecoDoMes, inicio), min(fimDoMes, fim)));
                    mes = mes.plusMonths(1);
                }
            }
        }
        return intervalos;
    }

    private static LocalDate min(LocalDate primeiro, LocalDate segundo) {
        return primeiro.isBefore(segundo) ? primeiro : segundo;
    }

    private static LocalDate max(LocalDate primeiro, LocalDate segundo) {
        return primeiro.isAfter(segundo) ? primeiro : segundo;
    }

    private record Intervalo(LocalDate inicio, LocalDate fim) { }

    private record Totais(BigDecimal valor, long quantidadeVendas) {
        private static final Totais ZERO = new Totais(BigDecimal.ZERO, 0);

        private Totais somar(Totais outro) {
            return new Totais(valor.add(outro.valor), quantidadeVendas + outro.quantidadeVendas);
        }
    }

    private record TotaisProduto(BigDecimal quantidade, BigDecimal valor) {
        private TotaisProduto somar(TotaisProduto outro) {
            return new TotaisProduto(quantidade.add(outro.quantidade), valor.add(outro.valor));
        }
    }
}
