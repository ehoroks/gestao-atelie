const MOEDA = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const DATA = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' })
const DATA_HORA = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

export function moeda(valor) {
  const n = Number(valor)
  return Number.isFinite(n) ? MOEDA.format(n) : '—'
}

export function data(valor) {
  if (!valor) return '—'
  // LocalDate chega como 'YYYY-MM-DD'; sem o T12:00 o fuso joga para o dia anterior.
  const d = valor.length === 10 ? new Date(`${valor}T12:00:00`) : new Date(valor)
  return Number.isNaN(d.getTime()) ? '—' : DATA.format(d)
}

export function dataHora(valor) {
  if (!valor) return '—'
  const d = new Date(valor)
  return Number.isNaN(d.getTime()) ? '—' : DATA_HORA.format(d)
}

/** Quantidade com a unidade, no formato que o protótipo usa ("1,5 m"). */
export function quantidade(valor, unidade) {
  const n = Number(valor)
  if (!Number.isFinite(n)) return '—'
  const texto = n.toLocaleString('pt-BR', { maximumFractionDigits: 3 })
  const sufixo = { UN: '', M: ' m', G: ' g' }[unidade] ?? ''
  return texto + sufixo
}

/** Aceita "1.234,56" e "1234.56" e devolve Number. */
export function paraNumero(texto) {
  if (texto === null || texto === undefined || texto === '') return null
  if (typeof texto === 'number') return texto
  const limpo = String(texto).replace(/[^\d,.-]/g, '')
  const normalizado = limpo.includes(',')
    ? limpo.replace(/\./g, '').replace(',', '.')
    : limpo
  const n = Number(normalizado)
  return Number.isFinite(n) ? n : null
}

/** 'YYYY-MM-DD' de hoje, no fuso local. */
export function hojeISO() {
  const d = new Date()
  const mes = String(d.getMonth() + 1).padStart(2, '0')
  const dia = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${mes}-${dia}`
}

/** Primeiro dia do mês corrente, em ISO. */
export function inicioDoMesISO() {
  const d = new Date()
  const mes = String(d.getMonth() + 1).padStart(2, '0')
  return `${d.getFullYear()}-${mes}-01`
}

export function iniciais(nome) {
  return (nome || '?').trim().charAt(0).toUpperCase() || '?'
}
