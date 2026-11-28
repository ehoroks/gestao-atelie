<script setup>
import { computed, onMounted, ref } from 'vue'
import CartaoResumo from '../components/CartaoResumo.vue'
import { encomendas, estoque, produtos, relatorios, vendas } from '../services/atelie'
import { usuarioAtual } from '../auth'
import { moeda, hojeISO, inicioDoMesISO } from '../utils/formato'

/**
 * O protótipo tem um painel inicial (RF24), mas o backend não expõe um
 * endpoint agregador. Os cinco números são compostos aqui a partir de
 * chamadas independentes, cada uma tolerante a falha para que um erro
 * isolado não derrube o painel inteiro.
 */
const ehAdmin = computed(() => usuarioAtual.value?.perfil === 'ADMIN')

const carregando = ref(true)
const vendasDoDia = ref(null)
const faturamentoMes = ref(null)
const produtosDisponiveis = ref(null)
const estoqueBaixo = ref(null)
const encomendasPendentes = ref(null)

async function tentar(fn, destino) {
  try {
    destino.value = await fn()
  } catch {
    destino.value = null
  }
}

async function carregar() {
  carregando.value = true
  const hoje = hojeISO()

  const tarefas = [
    tentar(async () => {
      const lista = await vendas.historico()
      return lista
        .filter((v) => v.situacao === 'CONCLUIDA' && String(v.criadoEm ?? '').startsWith(hoje))
        .reduce((soma, v) => soma + Number(v.valorFinal ?? 0), 0)
    }, vendasDoDia),

    tentar(async () => (await produtos.listar({ ativo: true })).length, produtosDisponiveis),
    tentar(async () => (await estoque.baixo()).length, estoqueBaixo),
    tentar(async () => (await encomendas.listar({ pendentes: true })).length, encomendasPendentes),
  ]

  // /api/relatorios/** é ADMIN-only; para ATENDENTE o cartão nem aparece.
  if (ehAdmin.value) {
    tarefas.push(
      tentar(async () => {
        const periodos = await relatorios.faturamento(inicioDoMesISO(), hoje, 'PERIODO')
        return periodos.reduce((soma, p) => soma + Number(p.faturamento ?? 0), 0)
      }, faturamentoMes),
    )
  }

  await Promise.all(tarefas)
  carregando.value = false
}

function ou(valor, formatador = String) {
  return valor === null || valor === undefined ? '—' : formatador(valor)
}

onMounted(carregar)
</script>

<template>
  <div>
    <h2 class="subtitulo-secao mb-3">Resumo do ateliê</h2>

    <div v-if="carregando" class="d-flex flex-column ga-3">
      <v-skeleton-loader v-for="n in 5" :key="n" type="list-item-two-line" rounded="lg" />
    </div>

    <v-row v-else dense>
      <v-col cols="12" sm="6">
        <CartaoResumo rotulo="Vendas do dia" :valor="ou(vendasDoDia, moeda)" destaque />
      </v-col>
      <v-col v-if="ehAdmin" cols="12" sm="6">
        <CartaoResumo rotulo="Faturamento do mês" :valor="ou(faturamentoMes, moeda)" destaque />
      </v-col>
      <v-col cols="12" sm="6">
        <CartaoResumo
          rotulo="Produtos disponíveis"
          :valor="produtosDisponiveis === null ? '—' : `${produtosDisponiveis} peças`"
        />
      </v-col>
      <v-col cols="12" sm="6">
        <CartaoResumo
          rotulo="Estoque baixo"
          :valor="estoqueBaixo === null ? '—' : `${estoqueBaixo} itens`"
        />
      </v-col>
      <v-col cols="12" sm="6">
        <CartaoResumo
          rotulo="Encomendas pendentes"
          :valor="encomendasPendentes === null ? '—' : `${encomendasPendentes} encomendas`"
        />
      </v-col>
    </v-row>

    <div class="d-flex flex-wrap ga-3 mt-5">
      <v-btn to="/vendas/nova" color="primary" variant="tonal" class="flex-grow-1">
        Nova venda
      </v-btn>
      <v-btn to="/encomendas/nova" color="primary" variant="tonal" class="flex-grow-1">
        Nova encomenda
      </v-btn>
    </div>

    <v-alert
      v-if="estoqueBaixo"
      type="warning"
      variant="tonal"
      rounded="lg"
      class="mt-5"
      density="comfortable"
    >
      <div class="d-flex align-center justify-space-between flex-wrap ga-2">
        <span>{{ estoqueBaixo }} {{ estoqueBaixo === 1 ? 'item está' : 'itens estão' }} com estoque baixo.</span>
        <v-btn to="/estoque" variant="text" density="comfortable">Ver estoque</v-btn>
      </div>
    </v-alert>
  </div>
</template>
