package br.com.atelie.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Persistencia da Trilha B (B1) via Spring Data JPA. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
