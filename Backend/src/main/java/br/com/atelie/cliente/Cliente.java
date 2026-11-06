package br.com.atelie.cliente;

import br.com.atelie.comum.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Trilha B - B2. Cliente + CRUD + busca por nome/telefone.
 *
 * Regras:
 * - nome obrigatorio;
 * - telefone obrigatorio, normalizado para apenas digitos (10 ou 11);
 * - email opcional, normalizado para minusculas quando informado.
 *
 * A associacao Usuario -> Cliente (ambiguidade 1 do planejamento) ainda nao
 * foi definida pela equipe; por isso as duas entidades permanecem separadas.
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    private static final Pattern EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    /** Somente digitos: 10 (fixo) ou 11 (celular). */
    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "email", length = 180)
    private String email;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /** Exigido pelo mapeamento ORM. */
    protected Cliente() {
    }

    public Cliente(String nome, String telefone, String email) {
        this.nome = exigirTexto(nome, "nome");
        this.telefone = normalizarTelefone(telefone);
        this.email = normalizarEmail(email);
    }

    @PrePersist
    void aoPersistir() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public void atualizarDados(String nome, String telefone, String email) {
        this.nome = exigirTexto(nome, "nome");
        this.telefone = normalizarTelefone(telefone);
        this.email = normalizarEmail(email);
    }

    // --- normalizacoes ---

    public static String normalizarTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            throw new RegraNegocioException("telefone e obrigatorio");
        }
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.length() < 10 || digitos.length() > 11) {
            throw new RegraNegocioException(
                    "telefone deve conter 10 ou 11 digitos (recebido: %s)".formatted(digitos));
        }
        return digitos;
    }

    public static String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String valor = email.trim().toLowerCase(Locale.ROOT);
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

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
