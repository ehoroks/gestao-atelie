package br.com.atelie.infra;

import br.com.atelie.comum.RegraNegocioException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Fake reutilizavel em testes de integracao A/B/C; NAO e banco persistente. */
public final class RepositorioEmMemoria<T> implements Repository<T> {
    private final ConcurrentMap<Long, T> entidades = new ConcurrentHashMap<>();

    @Override public Optional<T> buscarPorId(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(entidades.get(id));
    }

    @Override public List<T> listar() {
        return List.copyOf(new ArrayList<>(entidades.values()));
    }

    @Override public void salvar(Long id, T entidade) {
        if (id == null || id <= 0) throw new RegraNegocioException("id deve ser positivo");
        entidades.put(id, Objects.requireNonNull(entidade, "entidade e obrigatoria"));
    }

    @Override public void excluir(Long id) {
        if (id != null) entidades.remove(id);
    }
}
