import { createRouter, createWebHistory } from 'vue-router'
import { usuarioAtual, tokenAtual } from '../auth'

/**
 * `secao` marca qual item da navegação principal fica aceso, já que
 * telas internas (ex.: nova venda) pertencem a uma seção do menu.
 */
const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { publica: true, semNavegacao: true },
  },
  {
    path: '/',
    name: 'inicio',
    component: () => import('../views/InicioView.vue'),
    meta: { titulo: 'Início', secao: 'inicio' },
  },

  // --- Estoque e catálogo (a aba "Grupos" do protótipo) ---
  {
    path: '/estoque',
    name: 'estoque',
    component: () => import('../views/EstoqueView.vue'),
    meta: { titulo: 'Controle de Estoque', secao: 'estoque' },
  },
  {
    path: '/estoque/:id/entrada',
    name: 'estoque-entrada',
    component: () => import('../views/EstoqueEntradaView.vue'),
    meta: { titulo: 'Entrada de estoque', secao: 'estoque', voltar: true },
  },
  {
    path: '/estoque/:id/ajuste',
    name: 'estoque-ajuste',
    component: () => import('../views/EstoqueAjusteView.vue'),
    meta: { titulo: 'Ajuste de estoque', secao: 'estoque', voltar: true },
  },
  {
    path: '/estoque/:id/movimentacoes',
    name: 'estoque-movimentacoes',
    component: () => import('../views/EstoqueMovimentacoesView.vue'),
    meta: { titulo: 'Movimentações', secao: 'estoque', voltar: true },
  },
  {
    path: '/produtos/novo',
    name: 'produto-novo',
    component: () => import('../views/ProdutoFormView.vue'),
    meta: { titulo: 'Nova peça', secao: 'estoque', voltar: true },
  },
  {
    path: '/produtos/:id/editar',
    name: 'produto-editar',
    component: () => import('../views/ProdutoFormView.vue'),
    meta: { titulo: 'Editar peça', secao: 'estoque', voltar: true },
  },
  {
    path: '/insumos/novo',
    name: 'insumo-novo',
    component: () => import('../views/InsumoFormView.vue'),
    meta: { titulo: 'Novo insumo', secao: 'estoque', voltar: true },
  },
  {
    path: '/categorias',
    name: 'categorias',
    component: () => import('../views/CategoriasView.vue'),
    meta: { titulo: 'Categorias', secao: 'estoque', voltar: true },
  },
  {
    path: '/preco-sugerido',
    name: 'preco-sugerido',
    component: () => import('../views/PrecoSugeridoView.vue'),
    meta: { titulo: 'Preço sugerido', secao: 'estoque', voltar: true },
  },

  // --- Vendas ---
  {
    path: '/vendas',
    name: 'vendas',
    component: () => import('../views/VendasHistoricoView.vue'),
    meta: { titulo: 'Histórico de vendas', secao: 'vendas' },
  },
  {
    path: '/vendas/nova',
    name: 'venda-nova',
    component: () => import('../views/VendaNovaView.vue'),
    meta: { titulo: 'Nova venda', secao: 'vendas', voltar: true },
  },
  {
    path: '/vendas/:id/finalizar',
    name: 'venda-finalizar',
    component: () => import('../views/VendaFinalizarView.vue'),
    meta: { titulo: 'Finalizar venda', secao: 'vendas', voltar: true },
  },
  {
    path: '/vendas/:id/comprovante',
    name: 'venda-comprovante',
    component: () => import('../views/VendaComprovanteView.vue'),
    meta: { titulo: 'Comprovante', secao: 'vendas', voltar: true },
  },
  {
    path: '/relatorios',
    name: 'relatorios',
    component: () => import('../views/RelatorioView.vue'),
    meta: { titulo: 'Relatório de vendas', secao: 'vendas', voltar: true, perfil: 'ADMIN' },
  },

  // --- Encomendas ---
  {
    path: '/encomendas',
    name: 'encomendas',
    component: () => import('../views/EncomendasView.vue'),
    meta: { titulo: 'Encomendas', secao: 'encomendas' },
  },
  {
    path: '/encomendas/nova',
    name: 'encomenda-nova',
    component: () => import('../views/EncomendaFormView.vue'),
    meta: { titulo: 'Nova encomenda', secao: 'encomendas', voltar: true },
  },
  {
    path: '/encomendas/:id',
    name: 'encomenda-detalhe',
    component: () => import('../views/EncomendaDetalheView.vue'),
    meta: { titulo: 'Encomenda', secao: 'encomendas', voltar: true },
  },

  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: (to, from, salvo) => salvo ?? { top: 0 },
})

router.beforeEach((to) => {
  const autenticado = Boolean(tokenAtual())

  if (!to.meta.publica && !autenticado) {
    return { name: 'login', query: { destino: to.fullPath } }
  }
  if (to.name === 'login' && autenticado) {
    return { name: 'inicio' }
  }
  // Relatórios e usuários são ADMIN-only no SecurityConfig; barramos antes
  // de chamar a API para não exibir um 403 cru à proprietária.
  if (to.meta.perfil && usuarioAtual.value?.perfil !== to.meta.perfil) {
    return { name: 'inicio' }
  }
  return true
})

export default router
