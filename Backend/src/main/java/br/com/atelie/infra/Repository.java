package br.com.atelie.infra;

import java.util.List;
import java.util.Optional;

/** Porta generica; IDs sao geridos pelo adaptador ate que o ORM seja definido. */
public interface Repository<T> {
    Optional<T> buscarPorId(Long id);
    List<T> listar();
    void salvar(Long id, T entidade);
    void excluir(Long id);
}
