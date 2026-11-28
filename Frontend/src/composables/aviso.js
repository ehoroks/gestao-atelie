import { reactive } from 'vue'

/** Snackbar global, para confirmar ações e mostrar erros da API. */
export const aviso = reactive({
  visivel: false,
  texto: '',
  cor: 'surface-container-highest',
})

function mostrar(texto, cor) {
  aviso.texto = String(texto ?? '')
  aviso.cor = cor
  aviso.visivel = true
}

export function avisarSucesso(texto) {
  mostrar(texto, 'situacao-pronta')
}

export function avisarErro(erro) {
  mostrar(erro?.message ?? erro ?? 'Algo deu errado.', 'error')
}

/**
 * Executa uma chamada de API tratando o estado de carregando e o erro.
 * Devolve o resultado, ou undefined se falhou.
 */
export async function executar(ref, fn, mensagemSucesso) {
  ref.value = true
  try {
    const resultado = await fn()
    if (mensagemSucesso) avisarSucesso(mensagemSucesso)
    return resultado
  } catch (erro) {
    avisarErro(erro)
    return undefined
  } finally {
    ref.value = false
  }
}
