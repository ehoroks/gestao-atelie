package br.com.atelie.estoque;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from ItemEstoque i where i.id = :id")
    Optional<ItemEstoque> buscarPorIdParaAtualizacao(@Param("id") Long id);
}
