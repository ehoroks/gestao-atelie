package br.com.atelie.venda;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    /** Carrega os itens junto com a venda (open-in-view esta desligado). */
    @Override
    @EntityGraph(attributePaths = "itens")
    Optional<Venda> findById(Long id);

    @EntityGraph(attributePaths = "itens")
    List<Venda> findAllByOrderByIdAsc();

    /** Historico completo, mais recentes primeiro, com os itens carregados. */
    @EntityGraph(attributePaths = "itens")
    @Query("select v from Venda v order by v.criadoEm desc, v.id desc")
    List<Venda> listarHistorico();

    /** Todas as vendas associadas ao cliente, incluindo situacao e itens. */
    @EntityGraph(attributePaths = "itens")
    @Query("select v from Venda v where v.clienteId = :clienteId order by v.criadoEm desc, v.id desc")
    List<Venda> listarHistoricoDoCliente(@Param("clienteId") Long clienteId);

    /** Serializa finalizacao/cancelamento para impedir baixa ou estorno duplicados. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Venda v where v.id = :id")
    Optional<Venda> buscarPorIdParaAtualizacao(@Param("id") Long id);
}
