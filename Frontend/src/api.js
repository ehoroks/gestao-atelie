import { limparSessao, tokenAtual } from './auth'

const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')

export async function api(path, { method = 'GET', body } = {}) {
  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = tokenAtual()
  if (token) headers.Authorization = `Bearer ${token}`

  let response
  try {
    response = await fetch(base + path, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new Error('Não foi possível conectar à API. Verifique se o backend está no ar e se a URL e o CORS estão configurados.')
  }

  // Token ausente, inválido ou expirado: encerra a sessão e avisa a aplicação.
  // O login fica de fora para que a mensagem de credenciais inválidas apareça.
  if (response.status === 401 && !path.startsWith('/api/auth/login')) {
    limparSessao()
    window.dispatchEvent(new CustomEvent('atelie:sessao-expirada'))
  }

  const type = response.headers.get('content-type') || ''
  const result = response.status === 204 ? null : type.includes('json')
    ? await response.json() : await response.text()
  if (!response.ok) {
    const campos = result?.campos ? Object.entries(result.campos).map(([key, value]) => `${key}: ${value}`).join(' • ') : ''
    throw new Error(campos || result?.mensagem || result?.message || `Erro HTTP ${response.status}`)
  }
  return result
}

export function query(params) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && String(value).trim() !== '') search.set(key, value)
  })
  return search.toString() ? `?${search}` : ''
}
