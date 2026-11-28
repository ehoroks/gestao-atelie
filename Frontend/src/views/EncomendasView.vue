<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import EstadoVazio from '../components/EstadoVazio.vue'
import { encomendas, situacaoEncomenda, SITUACOES_ENCOMENDA } from '../services/atelie'
import { avisarErro } from '../composables/aviso'
import { data as fmtData, moeda } from '../utils/formato'

/**
 * Tela "Encomendas" do protótipo.
 *
 * As situações do protótipo ("Não iniciado", "Em produção", "Parado",
 * "Finalizado") foram mapeadas no enum real SituacaoEncomenda:
 * REGISTRADA, EM_PRODUCAO, PRONTA, ENTREGUE e CANCELADA.
 *
 * Fio solto: o protótipo mostra um selo "Pago" ao lado do valor, mas a
 * entidade Encomenda não guarda pagamento — não há campo nem endpoint. O selo
 * foi omitido; se a cliente precisar dele, é mudança de backend.
 */
const lista = ref([])
const carregando = ref(true)
const filtro = ref('TODAS')

const FILTROS = [
  { value: 'TODAS', title: 'Todas' },
  { value: 'PENDENTES', title: 'Pendentes' },
  ...SITUACOES_ENCOMENDA.map((s) => ({ value: s.value, title: s.title })),
]

const ordenadas = computed(() =>
  [...lista.value].sort((a, b) =>
    String(a.dataPrevistaEntrega ?? '').localeCompare(String(b.dataPrevistaEntrega ?? '')),
  ),
)

async function carregar() {
  carregando.value = true
  try {
    const filtros = {}
    if (filtro.value === 'PENDENTES') filtros.pendentes = true
    else if (filtro.value !== 'TODAS') filtros.situacao = filtro.value
    lista.value = await encomendas.listar(filtros)
  } catch (erro) {
    avisarErro(erro)
    lista.value = []
  } finally {
    carregando.value = false
  }
}

function atrasada(encomenda) {
  if (!encomenda.pendente || !encomenda.dataPrevistaEntrega) return false
  return encomenda.dataPrevistaEntrega < new Date().toISOString().slice(0, 10)
}

watch(filtro, carregar)
onMounted(carregar)
</script>

<template>
  <div>
    <v-chip-group v-model="filtro" mandatory selected-class="bg-secondary-container" class="mb-3">
      <v-chip v-for="f in FILTROS" :key="f.value" :value="f.value" variant="outlined" filter>
        {{ f.title }}
      </v-chip>
    </v-chip-group>

    <v-skeleton-loader v-if="carregando" type="list-item-three-line@4" />

    <EstadoVazio
      v-else-if="!ordenadas.length"
      icone="mdi-clipboard-text-outline"
      titulo="Nenhuma encomenda"
      descricao="As encomendas registradas aparecerão aqui, ordenadas pela data de entrega."
    >
      <v-btn to="/encomendas/nova" color="primary" variant="tonal">Nova encomenda</v-btn>
    </EstadoVazio>

    <v-list v-else class="bg-transparent pa-0">
      <v-list-item
        v-for="encomenda in ordenadas"
        :key="encomenda.id"
        :to="{ name: 'encomenda-detalhe', params: { id: encomenda.id } }"
        class="px-0 py-3"
        append-icon="mdi-chevron-right"
      >
        <template #prepend>
          <v-avatar
            :color="`${situacaoEncomenda(encomenda.situacao).cor}`"
            size="36"
            class="me-3"
            variant="tonal"
          >
            <v-icon size="18">mdi-hanger</v-icon>
          </v-avatar>
        </template>

        <div class="d-flex justify-space-between ga-3">
          <div style="min-width: 0">
            <div
              class="text-caption font-weight-medium"
              :style="`color: rgb(var(--v-theme-${situacaoEncomenda(encomenda.situacao).cor}))`"
            >
              {{ situacaoEncomenda(encomenda.situacao).title }}
              <span v-if="atrasada(encomenda)" class="text-error"> · atrasada</span>
            </div>
            <div class="text-body-1 font-weight-medium text-truncate">
              {{ encomenda.produtoNome || encomenda.descricao }}
            </div>
            <div class="text-body-2 text-medium-emphasis text-truncate">
              {{ encomenda.clienteNome ? `Para ${encomenda.clienteNome}` : encomenda.descricao }}
            </div>
          </div>

          <div class="text-right flex-shrink-0">
            <div class="text-body-1">{{ moeda(encomenda.valor) }}</div>
            <div class="text-caption text-medium-emphasis">
              {{ fmtData(encomenda.dataPrevistaEntrega) }}
            </div>
          </div>
        </div>
      </v-list-item>
    </v-list>

    <v-fab
      to="/encomendas/nova"
      icon="mdi-plus"
      color="primary"
      app
      appear
      location="bottom end"
      class="mb-16 mb-md-4"
      aria-label="Nova encomenda"
    />
  </div>
</template>
