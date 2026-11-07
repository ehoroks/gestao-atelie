package br.com.atelie.usuario;

import br.com.atelie.comum.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Trilha B - B1. Usuario + autenticacao.
 *
 * A senha NUNCA e armazenada em texto puro: este tipo recebe apenas o hash
 * (BCrypt) produzido pelo ServicoUsuario.
 */
@Entity
@Table(name = "usuarios",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuarios_email", columnNames = "email"))
public class Usuario {

    private static final Pattern EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "email", nullable = false, length = 180)
    private String email;

    /** Hash BCrypt (60 caracteres). Nunca a senha original. */
    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false, length = 20)
    private PerfilUsuario perfil;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /** Exigido pelo mapeamento ORM. */
    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash, PerfilUsuario perfil) {
        this.nome = exigirTexto(nome, "nome");
        this.email = normalizarEmail(email);
        this.senhaHash = exigirTexto(senhaHash, "senhaHash");
        this.perfil = Objects.requireNonNull(perfil, "perfil e obrigatorio");
    }

    @PrePersist
    void aoPersistir() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public void desativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void alterarSenhaHash(String novoHash) {
        this.senhaHash = exigirTexto(novoHash, "senhaHash");
    }

    // --- validacoes ---

    public static String normalizarEmail(String email) {
        String valor = exigirTexto(email, "email").toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(valor).matches()) {
            throw new RegraNegocioException("email invalido: " + valor);
        }
        return valor;
    }

    private static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("%s e obrigatorio".formatted(campo));
        }
        return valor.trim();
    }

    // --- acesso ---

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
