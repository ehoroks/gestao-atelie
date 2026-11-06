package br.com.atelie.venda;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    /** Carrega os itens junto com a venda (open-in-view esta desligado). */
    @Override
    @EntityGraph(attributePaths = "itens")
    Optional<Venda> findById(Long id);

    @EntityGraph(attributePaths = "itens")
    List<Venda> findAllByOrderByIdAsc();
}
