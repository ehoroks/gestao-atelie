import { ref } from 'vue'

const CHAVE_TOKEN = 'atelie.token'
const CHAVE_USUARIO = 'atelie.usuario'

function ler(chave) {
  try { return sessionStorage.getItem(chave) } catch { return null }
}

function gravar(chave, valor) {
  try {
    if (valor === null) sessionStorage.removeItem(chave)
    else sessionStorage.setItem(chave, valor)
  } catch {
    // sessionStorage indisponível (modo privado restrito): a sessão dura só a aba.
  }
}

function usuarioInicial() {
  try { return JSON.parse(ler(CHAVE_USUARIO) || 'null') } catch { return null }
}

/** Usuário logado, reativo para a UI. */
export const usuarioAtual = ref(usuarioInicial())

export function tokenAtual() {
  return ler(CHAVE_TOKEN) || ''
}

export function salvarSessao({ token, usuario }) {
  gravar(CHAVE_TOKEN, token)
  gravar(CHAVE_USUARIO, JSON.stringify(usuario))
  usuarioAtual.value = usuario
}

export function limparSessao() {
  gravar(CHAVE_TOKEN, null)
  gravar(CHAVE_USUARIO, null)
  usuarioAtual.value = null
}
