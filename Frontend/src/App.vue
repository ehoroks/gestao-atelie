<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { api, query } from './api'
import { limparSessao, salvarSessao, tokenAtual, usuarioAtual } from './auth'

const pages = [
  { id: 'produtos', label: 'Produtos', icon: '◈' },
  { id: 'categorias', label: 'Categorias', icon: '◫' },
  { id: 'insumos', label: 'Insumos', icon: '✳' },
  { id: 'estoque', label: 'Estoque', icon: '▤' },
]
const page = ref('produtos')
const busy = ref(false)
const loading = ref(true)
const feedback = ref('')
const feedbackError = ref(false)
const categorias = ref([])
const produtos = ref([])
const todosProdutos = ref([])
const insumos = ref([])
const estoque = ref([])
const movimentos = ref([])
const filtro = reactive({ nome: '', categoriaId: '', ativo: '' })
const selecionado = ref('')
const movimento = reactive({ tipo: 'entrada', quantidade: '', motivo: '' })
const produtoEditando = ref(null)
const categoriaEditando = ref(null)
const categoriaNome = ref('')
const insumoEditandoCusto = ref(null)
const insumoNovoCusto = ref('')
const novaCategoriaVisivel = ref(false)
const novoProdutoVisivel = ref(false)
const novoInsumoVisivel = ref(false)

const autenticado = ref(Boolean(tokenAtual()))
const usuario = usuarioAtual
const loginForm = reactive({ email: '', senha: '' })
const loginErro = ref('')
const loginBusy = ref(false)

const produtoInicial = () => ({
  nome: '', unidadeMedida: 'UN', estoqueMinimo: 0,
  categoriaId: '', preco: '', tamanho: '', cor: '', descricao: '', foto: ''
})
const insumoInicial = () => ({ nome: '', unidadeMedida: 'M', estoqueMinimo: 0, custoUnitario: '' })
const produtoForm = reactive(produtoInicial())
const insumoForm = reactive(insumoInicial())

const fmt = n => Number(n ?? 0).toLocaleString('pt-BR', { maximumFractionDigits: 3 })
const moeda = n => Number(n ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
const data = n => n ? new Date(n).toLocaleString('pt-BR') : '—'
const estoqueBaixo = computed(() => estoque.value.filter(i => i.estoqueBaixo).length)
const produtosAtivos = computed(() => todosProdutos.value.filter(p => p.ativo).length)
const itemSelecionado = computed(() => estoque.value.find(i => String(i.id) === String(selecionado.value)))
const quantidadeLabel = computed(() => movimento.tipo === 'ajuste' ? 'Nova quantidade total' : 'Quantidade')

function toast(message, error = false) {
  feedback.value = message
  feedbackError.value = error
}
async function carregarProdutos() {
  produtos.value = await api('/api/produtos' + query({
    nome: filtro.nome, categoriaId: filtro.categoriaId,
    ativo: filtro.ativo === '' ? null : filtro.ativo
  }))
}
async function carregarTudo() {
  const [c, i, e, p] = await Promise.all([
    api('/api/categorias'), api('/api/insumos'), api('/api/estoque'), api('/api/produtos')
  ])
  categorias.value = c
  todosProdutos.value = p
  insumos.value = i
  estoque.value = e
  await carregarProdutos()
  if (selecionado.value && !e.some(item => String(item.id) === String(selecionado.value))) {
    selecionado.value = ''
    movimentos.value = []
  }
}
async function executar(acao, sucesso) {
  busy.value = true
  feedback.value = ''
  try {
    await acao()
    await carregarTudo()
    if (selecionado.value) await carregarMovimentos()
    toast(sucesso)
  } catch (e) {
    toast(e.message, true)
  } finally {
    busy.value = false
  }
}
async function carregarMovimentos() {
  if (!selecionado.value) { movimentos.value = []; return }
  try { movimentos.value = await api(`/api/estoque/${selecionado.value}/movimentacoes`) }
  catch (e) { toast(e.message, true) }
}
function abrirProduto(produto = null) {
  produtoEditando.value = produto?.id ?? null
  Object.assign(produtoForm, produto ? {
    nome: produto.nome, unidadeMedida: produto.unidadeMedida,
    estoqueMinimo: produto.estoqueMinimo, categoriaId: produto.categoriaId ?? '',
    preco: produto.preco, tamanho: produto.tamanho ?? '', cor: produto.cor ?? '',
    descricao: produto.descricao ?? '', foto: produto.foto ?? ''
  } : produtoInicial())
  novoProdutoVisivel.value = true
}
function fecharProduto() { novoProdutoVisivel.value = false; produtoEditando.value = null }
async function salvarProduto() {
  const id = produtoEditando.value
  const body = {
    ...produtoForm,
    nome: produtoForm.nome.trim(), preco: Number(produtoForm.preco),
    categoriaId: produtoForm.categoriaId ? Number(produtoForm.categoriaId) : null,
  }
  if (!id) body.estoqueMinimo = Number(produtoForm.estoqueMinimo)
  else { delete body.unidadeMedida; delete body.estoqueMinimo }
  await executar(() => api(id ? `/api/produtos/${id}` : '/api/produtos', {
    method: id ? 'PUT' : 'POST', body
  }), id ? 'Produto atualizado com sucesso.' : 'Produto cadastrado com sucesso.')
  if (!feedbackError.value) fecharProduto()
}
async function desativarProduto(produto) {
  if (!confirm(`Desativar ${produto.nome}? O histórico será mantido.`)) return
  await executar(() => api(`/api/produtos/${produto.id}/desativar`, { method: 'PATCH' }),
    'Produto desativado. Ele não poderá ser vendido.')
}
function abrirCategoria(c = null) {
  categoriaEditando.value = c?.id ?? null
  categoriaNome.value = c?.nome ?? ''
  novaCategoriaVisivel.value = true
}
async function salvarCategoria() {
  const id = categoriaEditando.value
  await executar(() => api(id ? `/api/categorias/${id}` : '/api/categorias', {
    method: id ? 'PUT' : 'POST', body: { nome: categoriaNome.value }
  }), id ? 'Categoria atualizada.' : 'Categoria cadastrada.')
  if (!feedbackError.value) novaCategoriaVisivel.value = false
}
async function excluirCategoria(c) {
  if (!confirm(`Excluir a categoria ${c.nome}?`)) return
  await executar(() => api(`/api/categorias/${c.id}`, { method: 'DELETE' }), 'Categoria excluída.')
}
async function salvarInsumo() {
  await executar(() => api('/api/insumos', {
    method: 'POST', body: {
      ...insumoForm, nome: insumoForm.nome.trim(),
      estoqueMinimo: Number(insumoForm.estoqueMinimo),
      custoUnitario: Number(insumoForm.custoUnitario)
    }
  }), 'Insumo cadastrado.')
  if (!feedbackError.value) { novoInsumoVisivel.value = false; Object.assign(insumoForm, insumoInicial()) }
}
async function salvarCusto() {
  const id = insumoEditandoCusto.value
  await executar(() => api(`/api/insumos/${id}/custo`, {
    method: 'PATCH', body: { custoUnitario: Number(insumoNovoCusto.value) }
  }), 'Custo unitário atualizado.')
  if (!feedbackError.value) insumoEditandoCusto.value = null
}
async function registrarMovimento() {
  if (!selecionado.value) return toast('Selecione um item de estoque.', true)
  const tipo = movimento.tipo
  const url = tipo === 'entrada' ? 'entradas' : tipo === 'saida' ? 'saidas' : 'ajustes'
  const body = tipo === 'ajuste'
    ? { novaQuantidade: Number(movimento.quantidade), motivo: movimento.motivo }
    : { quantidade: Number(movimento.quantidade), motivo: movimento.motivo }
  await executar(() => api(`/api/estoque/${selecionado.value}/${url}`, { method: 'POST', body }),
    'Movimentação registrada e saldo atualizado.')
  if (!feedbackError.value) { movimento.quantidade = ''; movimento.motivo = '' }
}
watch(() => [filtro.nome, filtro.categoriaId, filtro.ativo], async () => {
  try { await carregarProdutos() } catch (e) { toast(e.message, true) }
})
watch(selecionado, carregarMovimentos)
async function entrar() {
  loginBusy.value = true
  loginErro.value = ''
  try {
    const resposta = await api('/api/auth/login', { method: 'POST', body: loginForm })
    salvarSessao(resposta)
    autenticado.value = true
    loginForm.senha = ''
    page.value = 'produtos'
    try { await carregarTudo() }
    catch (e) { toast(`Login ok, mas não foi possível carregar os dados: ${e.message}`, true) }
  } catch (e) {
    loginErro.value = e.message
  } finally {
    loginBusy.value = false
    loading.value = false
  }
}
function sair() {
  limparSessao()
  autenticado.value = false
  categorias.value = []
  produtos.value = []
  todosProdutos.value = []
  insumos.value = []
  estoque.value = []
  movimentos.value = []
  selecionado.value = ''
  feedback.value = ''
}
function sessaoExpirada() {
  if (!autenticado.value) return
  sair()
  loginErro.value = 'Sua sessão expirou. Entre novamente.'
}
onMounted(async () => {
  window.addEventListener('atelie:sessao-expirada', sessaoExpirada)
  if (!tokenAtual()) { loading.value = false; return }
  try { await carregarTudo() }
  catch (e) { toast(`Não foi possível conectar ao backend: ${e.message}`, true) }
  finally { loading.value = false }
})
onUnmounted(() => window.removeEventListener('atelie:sessao-expirada', sessaoExpirada))
</script>

<template>
  <template v-if="autenticado">
  <div class="layout">
    <aside class="sidebar">
      <div class="brand"><div class="brand-symbol">C<span>✳</span></div><div><strong>Cirinha</strong><small>ATELIÊ · GESTÃO</small></div></div>
      <div class="nav-caption">MENU PRINCIPAL</div>
      <nav aria-label="Navegação principal">
        <button v-for="p in pages" :key="p.id" class="nav-button" :class="{ selected: page === p.id }" @click="page = p.id">
          <span class="nav-icon">{{ p.icon }}</span>{{ p.label }}
          <span v-if="p.id === 'estoque' && estoqueBaixo" class="nav-count">{{ estoqueBaixo }}</span>
        </button>
      </nav>
      <div class="sidebar-footer"><span class="status-dot"></span> Módulo Trilha A <small>Vue.js + Spring Boot</small></div>
    </aside>

    <main class="main">
      <header class="topbar">
        <div><span class="eyebrow">CIRINHA ATELIÊ / GESTÃO</span><h1>{{ pages.find(p => p.id === page)?.label }}</h1></div>
        <div class="topbar-user">
          <span class="topbar-chip"><span class="status-dot"></span> {{ usuario?.nome || 'Controle do ateliê' }}<template v-if="usuario?.perfil"> · {{ usuario.perfil }}</template></span>
          <button class="secondary small" @click="sair">Sair</button>
        </div>
      </header>

      <div v-if="feedback" class="feedback" :class="{ error: feedbackError }" role="status">
        <span>{{ feedbackError ? '!' : '✓' }}</span><p>{{ feedback }}</p><button @click="feedback = ''" aria-label="Fechar aviso">×</button>
      </div>

      <div class="stats">
        <article class="stat"><span>Produtos ativos</span><strong>{{ produtosAtivos }}</strong><small>Peças disponíveis no cadastro</small></article>
        <article class="stat"><span>Categorias</span><strong>{{ categorias.length }}</strong><small>Organização do catálogo</small></article>
        <article class="stat"><span>Insumos</span><strong>{{ insumos.length }}</strong><small>Materiais cadastrados</small></article>
        <article class="stat" :class="{ 'stat-alert': estoqueBaixo }"><span>Estoque baixo</span><strong>{{ estoqueBaixo }}</strong><small>Itens que precisam de atenção</small></article>
      </div>

      <section v-if="page === 'produtos'" class="panel">
        <div class="section-heading"><div><span class="eyebrow">CATÁLOGO</span><h2>Peças e produtos</h2><p>Cadastre suas criações e organize as informações das peças.</p></div><button class="primary" @click="abrirProduto()">+ Novo produto</button></div>
        <div class="filters">
          <label class="field search"><span>Pesquisar</span><input v-model="filtro.nome" placeholder="Nome do produto..." /></label>
          <label class="field"><span>Categoria</span><select v-model="filtro.categoriaId"><option value="">Todas</option><option v-for="c in categorias" :key="c.id" :value="c.id">{{ c.nome }}</option></select></label>
          <label class="field"><span>Situação</span><select v-model="filtro.ativo"><option value="">Todas</option><option value="true">Ativos</option><option value="false">Inativos</option></select></label>
        </div>
        <div class="table-scroll"><table><thead><tr><th>PRODUTO</th><th>CATEGORIA</th><th>PREÇO</th><th>SALDO</th><th>SITUAÇÃO</th><th>AÇÕES</th></tr></thead>
          <tbody><tr v-for="p in produtos" :key="p.id"><td><div class="item-cell"><span class="thumb">✳</span><div><b>{{ p.nome }}</b><small>{{ [p.cor, p.tamanho].filter(Boolean).join(' · ') || 'Peça artesanal' }}</small></div></div></td><td>{{ p.categoriaNome || 'Sem categoria' }}</td><td class="strong">{{ moeda(p.preco) }}</td><td><span :class="{ 'warning-text': p.estoqueBaixo }">{{ fmt(p.quantidade) }} {{ p.unidadeMedida }}</span></td><td><span class="pill" :class="p.ativo ? 'positive' : 'muted'">{{ p.ativo ? 'Ativo' : 'Inativo' }}</span></td><td><div class="table-actions"><button @click="abrirProduto(p)">Editar</button><button v-if="p.ativo" class="danger-link" @click="desativarProduto(p)">Desativar</button></div></td></tr>
          <tr v-if="!produtos.length"><td colspan="6" class="empty">Nenhum produto encontrado. Comece cadastrando uma peça.</td></tr></tbody></table></div>
      </section>

      <section v-if="page === 'categorias'" class="panel">
        <div class="section-heading"><div><span class="eyebrow">ORGANIZAÇÃO</span><h2>Categorias</h2><p>Organize as peças em grupos para facilitar a busca.</p></div><button class="primary" @click="abrirCategoria()">+ Nova categoria</button></div>
        <div class="category-grid"><article v-for="c in categorias" :key="c.id" class="category-card"><div class="category-image">✳</div><div><b>{{ c.nome }}</b><small>{{ todosProdutos.filter(p => p.categoriaId === c.id).length }} produto(s)</small></div><div class="card-actions"><button @click="abrirCategoria(c)">Editar</button><button class="danger-link" @click="excluirCategoria(c)">Excluir</button></div></article><p v-if="!categorias.length" class="empty">Nenhuma categoria cadastrada.</p></div>
      </section>

      <section v-if="page === 'insumos'" class="panel">
        <div class="section-heading"><div><span class="eyebrow">MATÉRIA-PRIMA</span><h2>Insumos</h2><p>Gerencie tecidos, linhas, botões e outros materiais.</p></div><button class="primary" @click="novoInsumoVisivel = true">+ Novo insumo</button></div>
        <div class="table-scroll"><table><thead><tr><th>INSUMO</th><th>UNIDADE</th><th>CUSTO UNITÁRIO</th><th>ESTOQUE</th><th>SITUAÇÃO</th><th>AÇÕES</th></tr></thead><tbody>
          <tr v-for="i in insumos" :key="i.id"><td class="strong">{{ i.nome }}</td><td>{{ i.unidadeMedida }}</td><td>{{ moeda(i.custoUnitario) }}</td><td>{{ fmt(i.quantidade) }}</td><td><span class="pill" :class="i.estoqueBaixo ? 'caution' : 'positive'">{{ i.estoqueBaixo ? 'Baixo' : 'Normal' }}</span></td><td><button class="table-link" @click="insumoEditandoCusto = i.id; insumoNovoCusto = i.custoUnitario">Editar custo</button></td></tr>
          <tr v-if="!insumos.length"><td colspan="6" class="empty">Nenhum insumo cadastrado.</td></tr></tbody></table></div>
      </section>

      <section v-if="page === 'estoque'" class="panel">
        <div class="section-heading"><div><span class="eyebrow">MOVIMENTAÇÕES</span><h2>Controle de estoque</h2><p>Registre entradas, saídas e ajustes, com histórico de cada item.</p></div><span class="warning-counter" v-if="estoqueBaixo">{{ estoqueBaixo }} item(ns) abaixo do mínimo</span></div>
        <div class="inventory-layout"><div><div class="table-scroll"><table><thead><tr><th>ITEM</th><th>TIPO</th><th>SALDO</th><th>MÍNIMO</th><th>ESTADO</th></tr></thead><tbody>
          <tr v-for="i in estoque" :key="i.id" class="select-row" :class="{ 'active-row': String(selecionado) === String(i.id) }" @click="selecionado = i.id"><td class="strong">{{ i.nome }}</td><td>{{ i.tipo === 'PRODUTO' ? 'Produto' : 'Insumo' }}</td><td>{{ fmt(i.quantidade) }} {{ i.unidadeMedida }}</td><td>{{ fmt(i.estoqueMinimo) }}</td><td><span class="pill" :class="i.estoqueBaixo ? 'caution' : 'positive'">{{ i.estoqueBaixo ? 'Baixo' : 'OK' }}</span></td></tr>
          <tr v-if="!estoque.length"><td colspan="5" class="empty">Cadastre produtos ou insumos para controlar o estoque.</td></tr></tbody></table></div></div>
          <div class="stock-side"><h3>Registrar movimentação</h3><p>Selecione um item na tabela para alterar seu saldo.</p><form @submit.prevent="registrarMovimento"><label class="field"><span>Item escolhido</span><select v-model="selecionado" required><option value="">Selecione...</option><option v-for="i in estoque" :key="i.id" :value="i.id">{{ i.nome }}</option></select></label><div v-if="itemSelecionado" class="balance">Saldo atual <strong>{{ fmt(itemSelecionado.quantidade) }} {{ itemSelecionado.unidadeMedida }}</strong></div><label class="field"><span>Operação</span><select v-model="movimento.tipo"><option value="entrada">Entrada</option><option value="saida">Saída</option><option value="ajuste">Ajuste de inventário</option></select></label><label class="field"><span>{{ quantidadeLabel }}</span><input v-model="movimento.quantidade" type="number" :min="movimento.tipo === 'ajuste' ? 0 : 0.001" :step="itemSelecionado?.unidadeMedida === 'UN' ? 1 : 0.001" required /></label><label class="field"><span>Motivo {{ movimento.tipo === 'ajuste' ? '(obrigatório)' : '' }}</span><textarea v-model="movimento.motivo" rows="2" :required="movimento.tipo === 'ajuste'" placeholder="Ex.: compra de materiais" /></label><button class="primary full" :disabled="busy || !selecionado">Registrar</button></form></div></div>
        <div v-if="selecionado" class="history"><h3>Histórico de movimentações</h3><p>{{ itemSelecionado?.nome }}</p><div class="table-scroll"><table><thead><tr><th>DATA</th><th>TIPO</th><th>QUANTIDADE</th><th>SALDO APÓS</th><th>MOTIVO</th></tr></thead><tbody><tr v-for="m in [...movimentos].reverse()" :key="m.id"><td>{{ data(m.data) }}</td><td>{{ m.tipo }}</td><td>{{ fmt(m.quantidade) }}</td><td>{{ fmt(m.saldoResultante) }}</td><td>{{ m.motivo || '—' }}</td></tr><tr v-if="!movimentos.length"><td colspan="5" class="empty">Ainda não há movimentações para este item.</td></tr></tbody></table></div></div>
      </section>
      <footer>Cirinha Ateliê · Organização feita com carinho · Trilha A</footer>
    </main>
  </div>

  <div v-if="novoProdutoVisivel" class="overlay" @click.self="fecharProduto()"><section class="modal" role="dialog" aria-modal="true" aria-label="Produto"><div class="modal-header"><div><span class="eyebrow">CATÁLOGO</span><h2>{{ produtoEditando ? 'Editar produto' : 'Novo produto' }}</h2></div><button class="close" @click="fecharProduto()">×</button></div><form @submit.prevent="salvarProduto" class="modal-form"><label class="field span-2"><span>Nome da peça *</span><input v-model="produtoForm.nome" maxlength="120" required /></label><label class="field"><span>Categoria</span><select v-model="produtoForm.categoriaId"><option value="">Sem categoria</option><option v-for="c in categorias" :value="c.id" :key="c.id">{{ c.nome }}</option></select></label><label class="field"><span>Preço (R$) *</span><input v-model="produtoForm.preco" type="number" step="0.01" min="0" required /></label><label class="field"><span>Tamanho</span><input v-model="produtoForm.tamanho" maxlength="40" placeholder="Ex.: M" /></label><label class="field"><span>Cor</span><input v-model="produtoForm.cor" maxlength="60" placeholder="Ex.: Areia" /></label><label v-if="!produtoEditando" class="field"><span>Unidade de medida *</span><select v-model="produtoForm.unidadeMedida"><option value="UN">Unidade</option><option value="M">Metro</option><option value="G">Grama</option></select></label><label v-if="!produtoEditando" class="field"><span>Estoque mínimo *</span><input v-model="produtoForm.estoqueMinimo" type="number" min="0" step="0.001" required /></label><label class="field span-2"><span>Descrição</span><textarea v-model="produtoForm.descricao" rows="3" maxlength="1000" /></label><label class="field span-2"><span>URL da foto</span><input v-model="produtoForm.foto" maxlength="500" placeholder="https://..." /></label><div class="modal-actions span-2"><button type="button" class="secondary" @click="fecharProduto()">Cancelar</button><button class="primary" :disabled="busy">{{ busy ? 'Salvando...' : 'Salvar produto' }}</button></div></form></section></div>

  <div v-if="novaCategoriaVisivel" class="overlay" @click.self="novaCategoriaVisivel = false"><section class="modal modal-small" role="dialog" aria-modal="true" aria-label="Categoria"><div class="modal-header"><h2>{{ categoriaEditando ? 'Editar categoria' : 'Nova categoria' }}</h2><button class="close" @click="novaCategoriaVisivel = false">×</button></div><form @submit.prevent="salvarCategoria" class="modal-form"><label class="field span-2"><span>Nome da categoria *</span><input v-model="categoriaNome" required maxlength="80" placeholder="Ex.: Vestidos" /></label><div class="modal-actions span-2"><button type="button" class="secondary" @click="novaCategoriaVisivel = false">Cancelar</button><button class="primary" :disabled="busy">Salvar</button></div></form></section></div>

  <div v-if="novoInsumoVisivel" class="overlay" @click.self="novoInsumoVisivel = false"><section class="modal modal-small" role="dialog" aria-modal="true" aria-label="Insumo"><div class="modal-header"><h2>Novo insumo</h2><button class="close" @click="novoInsumoVisivel = false">×</button></div><form @submit.prevent="salvarInsumo" class="modal-form"><label class="field span-2"><span>Nome *</span><input v-model="insumoForm.nome" maxlength="120" required placeholder="Ex.: Tecido algodão" /></label><label class="field"><span>Unidade *</span><select v-model="insumoForm.unidadeMedida"><option value="UN">Unidade</option><option value="M">Metro</option><option value="G">Grama</option></select></label><label class="field"><span>Estoque mínimo *</span><input type="number" v-model="insumoForm.estoqueMinimo" min="0" step="0.001" required /></label><label class="field span-2"><span>Custo unitário (R$) *</span><input v-model="insumoForm.custoUnitario" type="number" step="0.0001" min="0" required /></label><div class="modal-actions span-2"><button type="button" class="secondary" @click="novoInsumoVisivel = false">Cancelar</button><button class="primary" :disabled="busy">Cadastrar</button></div></form></section></div>

  <div v-if="insumoEditandoCusto" class="overlay" @click.self="insumoEditandoCusto = null"><section class="modal modal-small" role="dialog" aria-modal="true" aria-label="Editar custo"><div class="modal-header"><h2>Atualizar custo unitário</h2><button class="close" @click="insumoEditandoCusto = null">×</button></div><form @submit.prevent="salvarCusto" class="modal-form"><label class="field span-2"><span>Novo custo (R$) *</span><input v-model="insumoNovoCusto" type="number" step="0.0001" min="0" required /></label><div class="modal-actions span-2"><button type="button" class="secondary" @click="insumoEditandoCusto = null">Cancelar</button><button class="primary" :disabled="busy">Salvar</button></div></form></section></div>
  </template>

  <main v-else class="login-page">
    <section class="login-card" aria-label="Login">
      <div class="brand login-brand"><div class="brand-symbol">C<span>✳</span></div><div><strong>Cirinha</strong><small>ATELIÊ · GESTÃO</small></div></div>
      <span class="eyebrow">ACESSO RESTRITO</span>
      <h1>Bem-vinda de volta</h1>
      <p class="login-subtitle">Entre com o e-mail e a senha cadastrados no ateliê.</p>
      <form class="login-form" @submit.prevent="entrar">
        <label class="field"><span>E-mail</span><input v-model.trim="loginForm.email" type="email" autocomplete="username" required autofocus /></label>
        <label class="field"><span>Senha</span><input v-model="loginForm.senha" type="password" autocomplete="current-password" required /></label>
        <p v-if="loginErro" class="login-error" role="alert">{{ loginErro }}</p>
        <button class="primary full" :disabled="loginBusy">{{ loginBusy ? 'Entrando...' : 'Entrar' }}</button>
      </form>
      <small class="login-hint">O acesso é gerenciado pelo administrador do ateliê.</small>
    </section>
  </main>
</template>
