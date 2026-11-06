package br.com.atelie.cliente;

import br.com.atelie.comum.RegraNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClienteTest {

    @Test
    void normalizaTelefoneParaSomenteDigitos() {
        Cliente cliente = new Cliente("Ana", "(11) 98765-4321", null);

        assertThat(cliente.getTelefone()).isEqualTo("11987654321");
    }

    @Test
    void telefoneComQuantidadeInvalidaDeDigitosEhRejeitado() {
        assertThatThrownBy(() -> new Cliente("Ana", "1234", null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("telefone");
    }

    @Test
    void emailEmBrancoViraNulo() {
        Cliente cliente = new Cliente("Ana", "1133334444", "   ");

        assertThat(cliente.getEmail()).isNull();
    }

    @Test
    void emailEhNormalizadoParaMinusculas() {
        Cliente cliente = new Cliente("Ana", "1133334444", "  ANA@Exemplo.COM ");

        assertThat(cliente.getEmail()).isEqualTo("ana@exemplo.com");
    }

    @Test
    void emailInvalidoEhRejeitado() {
        assertThatThrownBy(() -> new Cliente("Ana", "1133334444", "invalido"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("email");
    }

    @Test
    void atualizarDadosSubstituiCamposNormalizados() {
        Cliente cliente = new Cliente("Ana", "1133334444", "ana@exemplo.com");

        cliente.atualizarDados("Beatriz", "(21) 99888-7766", null);

        assertThat(cliente.getNome()).isEqualTo("Beatriz");
        assertThat(cliente.getTelefone()).isEqualTo("21998887766");
        assertThat(cliente.getEmail()).isNull();
    }
}
