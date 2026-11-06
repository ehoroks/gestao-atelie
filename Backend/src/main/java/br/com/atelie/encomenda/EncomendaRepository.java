package br.com.atelie.encomenda;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EncomendaRepository extends JpaRepository<Encomenda, Long> {

    boolean existsByCliente_Id(Long clienteId);

    @EntityGraph(attributePaths = {"cliente", "produto", "insumos", "insumos.insumo"})
    @Override
    Optional<Encomenda> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Encomenda e where e.id = :id")
    Optional<Encomenda> buscarPorIdParaAtualizacao(@Param("id") Long id);

    @EntityGraph(attributePaths = {"cliente", "produto", "insumos", "insumos.insumo"})
    List<Encomenda> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = {"cliente", "produto", "insumos", "insumos.insumo"})
    List<Encomenda> findBySituacaoOrderByIdDesc(SituacaoEncomenda situacao);

    @EntityGraph(attributePaths = {"cliente", "produto", "insumos", "insumos.insumo"})
    List<Encomenda> findBySituacaoInOrderByIdDesc(Collection<SituacaoEncomenda> situacoes);
}
