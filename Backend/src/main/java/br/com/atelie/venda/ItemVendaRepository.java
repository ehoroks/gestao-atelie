package br.com.atelie.venda;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {

    List<ItemVenda> findByVendaIdOrderByIdAsc(Long vendaId);
}
