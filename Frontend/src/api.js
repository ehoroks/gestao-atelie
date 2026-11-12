const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')

export async function api(path, { method = 'GET', body } = {}) {
  const response = await fetch(base + path, {
    method,
    headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
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
