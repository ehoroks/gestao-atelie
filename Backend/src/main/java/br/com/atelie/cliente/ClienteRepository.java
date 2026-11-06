package br.com.atelie.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca por trecho do nome (case-insensitive) ou por trecho do telefone.
     * `digitos` vazio desativa a parte telefonica para nao casar com todos.
     */
    @Query("""
            select c from Cliente c
            where lower(c.nome) like lower(concat('%', :termo, '%'))
               or (:digitos <> '' and c.telefone like concat('%', :digitos, '%'))
            order by c.nome asc
            """)
    List<Cliente> buscarPorNomeOuTelefone(@Param("termo") String termo,
                                          @Param("digitos") String digitos);
}
