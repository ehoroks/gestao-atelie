package br.com.atelie.api;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class TratadorGlobalDeExcecoesTest {

    @Test
    void conflitoDeVersaoJpaRetorna409SemTrocarContratoDeErroDaApi() {
        var resposta = new TratadorGlobalDeExcecoes().tratarConcorrenciaOtimista(
                new OptimisticLockingFailureException("versao desatualizada"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().mensagem()).contains("alterado por outra operacao");
    }
}
