package br.com.atelie.catalogo;

import br.com.atelie.comum.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long   id;

    @Column(name = "nome", nullable = false, length = 80, unique = true)
    private String nome;

    protected Categoria() { }

    public Categoria(String nome) {
        this.nome = renomear(nome);
    }

    public final String renomear(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("nome da categoria e obrigatorio");
        }
        return this.nome = nome.trim();
    }

    public Long   getId()   { return id; }
    public String getNome() { return nome; }
}
