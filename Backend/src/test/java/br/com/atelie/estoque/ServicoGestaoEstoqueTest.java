package br.com.atelie.estoque;

import br.com.atelie.catalogo.Insumo;
import br.com.atelie.catalogo.Produto;
import br.com.atelie.comum.EstoqueInsuficienteException;
import br.com.atelie.estoque.dto.AjustarEstoqueRequest;
import br.com.atelie.estoque.dto.MovimentarEstoqueRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoGestaoEstoqueTest {
    @Mock ItemEstoqueRepository itens;
    @Mock MovimentacaoEstoqueRepository movimentacoes;
    @InjectMocks ServicoGestaoEstoque servico;

    @Test void entradaEAjusteRegistramMovimentacao() {
        var insumo = insumo("Linha", 3L);
        when(itens.buscarPorIdParaAtualizacao(3L)).thenReturn(Optional.of(insumo));
        servico.entrada(3L, new MovimentarEstoqueRequest(new BigDecimal("5"), "compra"));
        assertEquals(new BigDecimal("5"), insumo.getQuantidade());
        servico.ajustar(3L, new AjustarEstoqueRequest(new BigDecimal("7"), "inventario"));
        assertEquals(new BigDecimal("7"), insumo.getQuantidade());
        assertEquals(2, insumo.getHistorico().size());
        verify(movimentacoes, times(2)).saveAndFlush(any(MovimentacaoEstoque.class));
    }

    @Test void saidaInsuficienteNaoAlteraSaldoNemCriaMovimento() {
        var insumo = insumo("Linha", 8L);
        when(itens.buscarPorIdParaAtualizacao(8L)).thenReturn(Optional.of(insumo));
        assertThrows(EstoqueInsuficienteException.class,
                () -> servico.saida(8L, new MovimentarEstoqueRequest(new BigDecimal("4"), "uso")));
        assertEquals(BigDecimal.ZERO, insumo.getQuantidade());
        assertTrue(insumo.getHistorico().isEmpty());
        verifyNoInteractions(movimentacoes);
    }

    @Test void identificaEstoqueBaixoDeProdutosEInsumos() {
        var insumo = insumo("Linha", 8L);
        var produto = new Produto("Vestido", UnidadeMedida.UN, new BigDecimal("2"), null,
                new BigDecimal("100"));
        ReflectionTestUtils.setField(produto, "id", 9L);
        produto.registrarEntrada(new BigDecimal("5"));
        when(itens.findAll()).thenReturn(List.of(insumo, produto));
        var baixos = servico.listar(null, true, null);
        assertEquals(1, baixos.size());
        assertEquals("INSUMO", baixos.get(0).tipo());
        assertEquals("Linha", baixos.get(0).nome());
    }

    private static Insumo insumo(String nome, Long id) {
        var insumo = new Insumo(nome, UnidadeMedida.UN, new BigDecimal("2"), new BigDecimal("1.25"));
        ReflectionTestUtils.setField(insumo, "id", id);
        return insumo;
    }
}
