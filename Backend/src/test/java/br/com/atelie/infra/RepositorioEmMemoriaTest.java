package br.com.atelie.infra;

import br.com.atelie.comum.RegraNegocioException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RepositorioEmMemoriaTest {
    @Test void permiteSalvarConsultarListarERemover() {
        Repository<String> repo = new RepositorioEmMemoria<>();
        repo.salvar(7L, "teste");
        assertEquals("teste", repo.buscarPorId(7L).orElseThrow());
        assertEquals(1, repo.listar().size());
        assertThrows(UnsupportedOperationException.class, () -> repo.listar().clear());
        repo.excluir(7L);
        assertTrue(repo.buscarPorId(7L).isEmpty());
    }

    @Test void naoAceitaIdInvalido() {
        Repository<String> repo = new RepositorioEmMemoria<>();
        assertThrows(RegraNegocioException.class, () -> repo.salvar(-1L, "teste"));
        assertThrows(RegraNegocioException.class, () -> repo.salvar(null, "teste"));
    }
}
