package br.com.atelie.api;

import br.com.atelie.comum.DominioException;
import br.com.atelie.comum.EstoqueInsuficienteException;
import br.com.atelie.comum.RecursoJaExistenteException;
import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.comum.RegraNegocioException;
import br.com.atelie.comum.TransicaoInvalidaException;
import br.com.atelie.usuario.CredenciaisInvalidasException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traducao da hierarquia DominioException para HTTP (base do D2).
 *
 * 400 - payload invalido; 401 - credenciais; 404 - nao encontrado;
 * 409 - conflito/estoque/transicao; 422 - invariante de dominio.
 */
@RestControllerAdvice
public class TratadorGlobalDeExcecoes {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Dados invalidos", campos);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResposta> tratarCredenciais(CredenciaisInvalidasException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RecursoJaExistenteException.class)
    public ResponseEntity<ErroResposta> tratarConflito(RecursoJaExistenteException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarIntegridade(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT, "Registro em conflito com dados ja existentes");
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<ErroResposta> tratarEstoque(EstoqueInsuficienteException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(TransicaoInvalidaException.class)
    public ResponseEntity<ErroResposta> tratarTransicao(TransicaoInvalidaException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> tratarRegra(RegraNegocioException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(DominioException.class)
    public ResponseEntity<ErroResposta> tratarDominio(DominioException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    private static ResponseEntity<ErroResposta> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status)
                .body(ErroResposta.de(status.value(), status.getReasonPhrase(), mensagem));
    }

    private static ResponseEntity<ErroResposta> resposta(HttpStatus status, String mensagem,
                                                         Map<String, String> campos) {
        return ResponseEntity.status(status)
                .body(ErroResposta.de(status.value(), status.getReasonPhrase(), mensagem, campos));
    }
}
