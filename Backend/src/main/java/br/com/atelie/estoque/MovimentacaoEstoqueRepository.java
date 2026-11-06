package br.com.atelie.estoque;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    @Query("select m from MovimentacaoEstoque m where m.item.id = :itemId order by m.id asc")
    List<MovimentacaoEstoque> listarPorItemId(@Param("itemId") Long itemId);
}
