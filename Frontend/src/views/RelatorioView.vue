<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import CartaoResumo from '../components/CartaoResumo.vue'
import EstadoVazio from '../components/EstadoVazio.vue'
import { produtos, relatorios, SITUACOES_VENDA } from '../services/atelie'
import { avisarErro } from '../composables/aviso'
import { data as fmtData, dataHora, hojeISO, inicioDoMesISO, moeda } from '../utils/formato'

/**
 * Tela "Relatório de vendas" do protótipo, cobrindo RF25, RF26 e RF27.
 *
 * O protótipo mostra só o faturamento por semana. Os dois botões do histórico
 * ("Relatório de vendas" e "Faturamento") viraram abas daqui, e "Mais
 * vendidos" foi acrescentado porque o RF27 e o endpoint já existem sem tela.
 *
 * Toda esta rota é ADMIN-only no SecurityConfig; o guard do router já barra
 * antes de chamar a API.
 */
const route = useRoute()

const aba = ref(route.query.aba === 'vendas' ? 'vendas' : 'faturamento')
const inicio = ref(inicioDoMesISO())
const fim = ref(hojeISO())
const agruparPor = ref('SEMANA')
const carregando = ref(false)

const periodos = ref([])
const listaVendas = ref([])
const maisVendidos = ref([])
const nomesProdutos = ref({})

const AGRUPAMENTOS = [
  { value: 'DIA', title: 'Por dia' },
  { value: 'SEMANA', title: 'Por semana' },
  { value: 'MES', title: 'Por mês' },
  { value: 'PERIODO', title: 'Período inteiro' },
]

const totalFaturamento = computed(() =>
  periodos.value.reduce((soma, p) => soma + Number(p.faturamento ?? 0), 0),
)
const totalVendas = computed(() =>
  periodos.value.reduce((soma, p) => soma + Number(p.quantidadeVendas ?? 0), 0),
)
const maiorFaturamento = computed(() =>
  Math.max(1, ...periodos.value.map((p) => Number(p.faturamento ?? 0))),
)

function rotuloPeriodo(periodo, indice) {
  if (agruparPor.value === 'SEMANA') return `Semana ${indice + 1}`
  if (agruparPor.value === 'PERIODO') return 'Período'
  return fmtData(periodo.inicio)
}

async function gerar() {
  if (inicio.value > fim.value) {
    return avisarErro('A data inicial não pode ser depois da data final.')
  }
  carregando.value = true
  try {
    if (aba.value === 'faturamento') {
      periodos.value = await relatorios.faturamento(inicio.value, fim.value, agruparPor.value)
    } else if (aba.value === 'vendas') {
      listaVendas.value = await relatorios.vendas(inicio.value, fim.value)
    } else {
      const [ranking, catalogo] = await Promise.all([
        relatorios.produtosMaisVendidos(inicio.value, fim.value, 10),
        produtos.listar({}),
      ])
      maisVendidos.value = ranking
      nomesProdutos.value = Object.fromEntries(catalogo.map((p) => [p.id, p.nome]))
    }
  } catch (erro) {
    avisarErro(erro)
  } finally {
    carregando.value = false
  }
}

onMounted(gerar)
</script>

<template>
  <div>
    <v-tabs v-model="aba" color="primary" class="mb-4" @update:model-value="gerar">
      <v-tab value="faturamento" class="text-none">Faturamento</v-tab>
      <v-tab value="vendas" class="text-none">Vendas</v-tab>
      <v-tab value="ranking" class="text-none">Mais vendidos</v-tab>
    </v-tabs>

    <h2 class="subtitulo-secao mb-3">Período</h2>

    <div class="d-flex flex-column ga-4 mb-4">
      <div class="d-flex ga-4 flex-wrap">
        <v-text-field v-model="inicio" label="Data inicial" type="date" class="flex-grow-1" />
        <v-text-field v-model="fim" label="Data final" type="date" class="flex-grow-1" />
      </div>
      <v-select
        v-if="aba === 'faturamento'"
        v-model="agruparPor"
        :items="AGRUPAMENTOS"
        label="Agrupar"
      />
    </div>

    <v-progress-linear v-if="carregando" indeterminate rounded class="mb-4" />

    <!-- Faturamento -->
    <template v-if="aba === 'faturamento' && !carregando">
      <v-row dense class="mb-2">
        <v-col cols="12" sm="6">
          <CartaoResumo rotulo="Vendas válidas" :valor="totalVendas" />
        </v-col>
        <v-col cols="12" sm="6">
          <CartaoResumo rotulo="Faturamento" :valor="moeda(totalFaturamento)" destaque />
        </v-col>
      </v-row>

      <EstadoVazio
        v-if="!periodos.length"
        icone="mdi-chart-bar"
        titulo="Sem vendas no período"
        descricao="Escolha outro intervalo de datas."
      />

      <div v-else class="d-flex flex-column ga-4 mt-4">
        <div v-for="(periodo, indice) in periodos" :key="indice">
          <div class="d-flex justify-space-between align-center mb-1">
            <span class="text-body-1 font-weight-medium">
              {{ rotuloPeriodo(periodo, indice) }}
            </span>
            <span class="text-body-2 text-medium-emphasis">
              {{ moeda(periodo.faturamento) }}
            </span>
          </div>
          <v-progress-linear
            :model-value="(Number(periodo.faturamento) / maiorFaturamento) * 100"
            color="primary"
            bg-color="surface-container-high"
            height="12"
            rounded
          />
        </div>
      </div>
    </template>

    <!-- Lista de vendas do período -->
    <template v-if="aba === 'vendas' && !carregando">
      <EstadoVazio
        v-if="!listaVendas.length"
        icone="mdi-receipt-text-outline"
        titulo="Sem vendas no período"
        descricao="Escolha outro intervalo de datas."
      />
      <v-card v-for="venda in listaVendas" v-else :key="venda.id" variant="outlined" rounded="lg" class="mb-2">
        <v-card-text class="d-flex align-center justify-space-between ga-3">
          <div>
            <div class="text-body-1 font-weight-medium">
              Venda #{{ String(venda.id).padStart(6, '0') }}
            </div>
            <div class="text-body-2 text-medium-emphasis">
              {{ dataHora(venda.criadoEm) }} · {{ SITUACOES_VENDA[venda.situacao] ?? venda.situacao }}
            </div>
          </div>
          <div class="text-body-1 font-weight-medium">{{ moeda(venda.valorFinal) }}</div>
        </v-card-text>
      </v-card>
    </template>

    <!-- Produtos mais vendidos (RF27) -->
    <template v-if="aba === 'ranking' && !carregando">
      <EstadoVazio
        v-if="!maisVendidos.length"
        icone="mdi-trophy-outline"
        titulo="Sem dados no período"
        descricao="Nenhum produto foi vendido no intervalo escolhido."
      />
      <v-card
        v-for="(item, indice) in maisVendidos"
        v-else
        :key="item.produtoId"
        variant="outlined"
        rounded="lg"
        class="mb-2"
      >
        <v-card-text class="d-flex align-center ga-3">
          <v-avatar color="primary-container" size="36">
            <span class="text-on-primary-container">{{ indice + 1 }}</span>
          </v-avatar>
          <div class="flex-grow-1">
            <div class="text-body-1 font-weight-medium">
              {{ nomesProdutos[item.produtoId] ?? `Produto #${item.produtoId}` }}
            </div>
            <div class="text-body-2 text-medium-emphasis">
              {{ Number(item.quantidadeVendida).toLocaleString('pt-BR') }} vendidos
            </div>
          </div>
          <div class="text-body-1 font-weight-medium">{{ moeda(item.valorVendido) }}</div>
        </v-card-text>
      </v-card>
    </template>

    <v-btn
      variant="flat"
      color="primary"
      :loading="carregando"
      block
      class="mt-6"
      @click="gerar"
    >
      Gerar relatório
    </v-btn>
  </div>
</template>
