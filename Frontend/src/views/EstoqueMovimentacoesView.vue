<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import EstadoVazio from '../components/EstadoVazio.vue'
import { estoque } from '../services/atelie'
import { avisarErro } from '../composables/aviso'
import { dataHora, quantidade as fmtQuantidade } from '../utils/formato'

/**
 * RF28 — histórico de movimentações. Não existe no protótipo, mas o requisito
 * está na especificação e o endpoint já existe
 * (GET /api/estoque/{id}/movimentacoes). Sem esta tela o motivo registrado no
 * ajuste nunca seria lido por ninguém.
 */
const route = useRoute()
const id = computed(() => route.params.id)

const item = ref(null)
const movimentacoes = ref([])
const carregando = ref(true)

const APARENCIA = {
  ENTRADA: { icone: 'mdi-tray-arrow-down', cor: 'situacao-pronta', rotulo: 'Entrada' },
  SAIDA: { icone: 'mdi-tray-arrow-up', cor: 'situacao-producao', rotulo: 'Saída' },
  AJUSTE: { icone: 'mdi-tune-variant', cor: 'primary', rotulo: 'Ajuste' },
}

function aparencia(tipo) {
  return APARENCIA[tipo] ?? { icone: 'mdi-circle-small', cor: 'outline', rotulo: tipo }
}

async function carregar() {
  carregando.value = true
  try {
    const [buscado, historico] = await Promise.all([
      estoque.buscar(id.value),
      estoque.movimentacoes(id.value),
    ])
    item.value = buscado
    // Mais recentes primeiro.
    movimentacoes.value = [...historico].sort((a, b) => String(b.data).localeCompare(String(a.data)))
  } catch (erro) {
    avisarErro(erro)
  } finally {
    carregando.value = false
  }
}

onMounted(carregar)
</script>

<template>
  <div>
    <h2 v-if="item" class="subtitulo-secao mb-1">{{ item.nome }}</h2>
    <p v-if="item" class="texto-auxiliar mb-4">
      Saldo atual: {{ fmtQuantidade(item.quantidade, item.unidadeMedida) }}
    </p>

    <v-skeleton-loader v-if="carregando" type="list-item-two-line@4" />

    <EstadoVazio
      v-else-if="!movimentacoes.length"
      icone="mdi-history"
      titulo="Sem movimentações"
      descricao="Entradas, saídas e ajustes deste item aparecerão aqui."
    />

    <v-timeline v-else side="end" density="compact" truncate-line="both" class="ms-n4">
      <v-timeline-item
        v-for="m in movimentacoes"
        :key="m.id"
        :dot-color="aparencia(m.tipo).cor"
        :icon="aparencia(m.tipo).icone"
        icon-color="white"
        size="small"
      >
        <div class="d-flex justify-space-between ga-3 flex-wrap">
          <div>
            <div class="text-body-1 font-weight-medium">
              {{ aparencia(m.tipo).rotulo }}
              <span class="text-medium-emphasis font-weight-regular">
                {{ fmtQuantidade(m.quantidade, item?.unidadeMedida) }}
              </span>
            </div>
            <div v-if="m.motivo" class="text-body-2 text-medium-emphasis">{{ m.motivo }}</div>
            <div class="text-caption text-medium-emphasis">{{ dataHora(m.data) }}</div>
          </div>
          <v-chip size="small" variant="tonal">
            saldo {{ fmtQuantidade(m.saldoResultante, item?.unidadeMedida) }}
          </v-chip>
        </div>
      </v-timeline-item>
    </v-timeline>
  </div>
</template>
