import { api, query } from '../api'

/**
 * Envelopes tipados da API. Os nomes de campo seguem exatamente os
 * records do backend (br.com.atelie.*.dto), conferidos no código-fonte.
 */

export const auth = {
  login: (email, senha) =>
    api('/api/auth/login', { method: 'POST', body: { email, senha } }),
}

export const produtos = {
  listar: (filtros = {}) => api(`/api/produtos${query(filtros)}`),
  buscar: (id) => api(`/api/produtos/${id}`),
  criar: (dados) => api('/api/produtos', { method: 'POST', body: dados }),
  editar: (id, dados) => api(`/api/produtos/${id}`, { method: 'PUT', body: dados }),
  desativar: (id) => api(`/api/produtos/${id}/desativar`, { method: 'PATCH' }),
}

export const categorias = {
  listar: () => api('/api/categorias'),
  buscar: (id) => api(`/api/categorias/${id}`),
  criar: (nome) => api('/api/categorias', { method: 'POST', body: { nome } }),
  editar: (id, nome) => api(`/api/categorias/${id}`, { method: 'PUT', body: { nome } }),
  excluir: (id) => api(`/api/categorias/${id}`, { method: 'DELETE' }),
}

export const insumos = {
  listar: (nome) => api(`/api/insumos${query({ nome })}`),
  buscar: (id) => api(`/api/insumos/${id}`),
  criar: (dados) => api('/api/insumos', { method: 'POST', body: dados }),
  atualizarCusto: (id, custoUnitario) =>
    api(`/api/insumos/${id}/custo`, { method: 'PATCH', body: { custoUnitario } }),
}

export const estoque = {
  /** tipo: 'PRODUTO' | 'INSUMO' | undefined */
  listar: (filtros = {}) => api(`/api/estoque${query(filtros)}`),
  baixo: () => api('/api/estoque/baixo'),
  buscar: (id) => api(`/api/estoque/${id}`),
  movimentacoes: (id) => api(`/api/estoque/${id}/movimentacoes`),
  entrada: (id, quantidade, motivo) =>
    api(`/api/estoque/${id}/entradas`, { method: 'POST', body: { quantidade, motivo } }),
  saida: (id, quantidade, motivo) =>
    api(`/api/estoque/${id}/saidas`, { method: 'POST', body: { quantidade, motivo } }),
  ajustar: (id, novaQuantidade, motivo) =>
    api(`/api/estoque/${id}/ajustes`, { method: 'POST', body: { novaQuantidade, motivo } }),
}

export const vendas = {
  abrir: (clienteId) =>
    api('/api/vendas', { method: 'POST', body: clienteId ? { clienteId } : {} }),
  listar: () => api('/api/vendas'),
  historico: () => api('/api/vendas/historico'),
  historicoDoCliente: (clienteId) => api(`/api/vendas/historico/clientes/${clienteId}`),
  buscar: (id) => api(`/api/vendas/${id}`),
  adicionarItem: (id, produtoId, quantidade, precoUnitario) =>
    api(`/api/vendas/${id}/itens`, {
      method: 'POST',
      body: { produtoId, quantidade, precoUnitario },
    }),
  removerItem: (id, itemId) =>
    api(`/api/vendas/${id}/itens/${itemId}`, { method: 'DELETE' }),
  aplicarDesconto: (id, valor) =>
    api(`/api/vendas/${id}/desconto`, { method: 'PUT', body: { valor } }),
  definirFormaPagamento: (id, formaPagamento) =>
    api(`/api/vendas/${id}/forma-pagamento`, { method: 'PUT', body: { formaPagamento } }),
  finalizar: (id) => api(`/api/vendas/${id}/finalizar`, { method: 'POST' }),
  cancelar: (id, motivo) =>
    api(`/api/vendas/${id}/cancelar`, {
      method: 'POST',
      body: { data: new Date().toISOString().slice(0, 19), motivo },
    }),
  comprovante: (id) => api(`/api/vendas/${id}/comprovante`),
}

export const clientes = {
  listar: () => api('/api/clientes'),
  buscar: (id) => api(`/api/clientes/${id}`),
  criar: (dados) => api('/api/clientes', { method: 'POST', body: dados }),
  editar: (id, dados) => api(`/api/clientes/${id}`, { method: 'PUT', body: dados }),
  excluir: (id) => api(`/api/clientes/${id}`, { method: 'DELETE' }),
}

export const encomendas = {
  listar: (filtros = {}) => api(`/api/encomendas${query(filtros)}`),
  buscar: (id) => api(`/api/encomendas/${id}`),
  criar: (dados) => api('/api/encomendas', { method: 'POST', body: dados }),
  atualizarSituacao: (id, situacao) =>
    api(`/api/encomendas/${id}/situacao`, { method: 'PATCH', body: { situacao } }),
}

/** Exige perfil ADMIN no backend. */
export const relatorios = {
  vendas: (inicio, fim) => api(`/api/relatorios/vendas${query({ inicio, fim })}`),
  faturamento: (inicio, fim, agruparPor = 'SEMANA') =>
    api(`/api/relatorios/vendas/faturamento${query({ inicio, fim, agruparPor })}`),
  produtosMaisVendidos: (inicio, fim, limite = 10) =>
    api(`/api/relatorios/vendas/produtos-mais-vendidos${query({ inicio, fim, limite })}`),
}

/** Exige perfil ADMIN no backend. */
export const usuarios = {
  listar: () => api('/api/usuarios'),
  buscar: (id) => api(`/api/usuarios/${id}`),
  criar: (dados) => api('/api/usuarios', { method: 'POST', body: dados }),
}

// --- Enums do domínio, espelhando os enums Java ---

export const UNIDADES = [
  { value: 'UN', title: 'Unidade' },
  { value: 'M', title: 'Metro' },
  { value: 'G', title: 'Grama' },
]

export const FORMAS_PAGAMENTO = [
  { value: 'PIX', title: 'PIX' },
  { value: 'DINHEIRO', title: 'Dinheiro' },
  { value: 'CARTAO', title: 'Cartão' },
  { value: 'OUTROS', title: 'Outros' },
]

export const SITUACOES_ENCOMENDA = [
  { value: 'REGISTRADA', title: 'Registrada', cor: 'situacao-neutra' },
  { value: 'EM_PRODUCAO', title: 'Em produção', cor: 'situacao-producao' },
  { value: 'PRONTA', title: 'Pronta', cor: 'situacao-pronta' },
  { value: 'ENTREGUE', title: 'Entregue', cor: 'situacao-pronta' },
  { value: 'CANCELADA', title: 'Cancelada', cor: 'situacao-parado' },
]

export function situacaoEncomenda(valor) {
  return SITUACOES_ENCOMENDA.find((s) => s.value === valor)
    ?? { value: valor, title: valor, cor: 'situacao-neutra' }
}

export const SITUACOES_VENDA = {
  EM_ABERTO: 'Em aberto',
  CONCLUIDA: 'Concluída',
  CANCELADA: 'Cancelada',
}
