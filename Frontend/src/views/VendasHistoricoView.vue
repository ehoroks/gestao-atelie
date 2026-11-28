<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import EstadoVazio from '../components/EstadoVazio.vue'
import { vendas, FORMAS_PAGAMENTO, SITUACOES_VENDA } from '../services/atelie'
import { usuarioAtual } from '../auth'
import { avisarErro, executar } from '../composables/aviso'
import { dataHora, moeda } from '../utils/formato'

/**
 * Tela "Histórico de vendas" do protótipo.
 *
 * No Figma a lista reaproveita o cartão de produto, o que não diz nada sobre
 * a venda. Aqui cada linha mostra o que identifica uma venda: número, data,
 * quantidade de itens, valor e situação.
 *
 * O cancelamento (RF22) não tem tela no protótipo, mas o endpoint existe e o
 * requisito é obrigatório, então entra como ação da própria lista.
 */
const router = useRouter()
const ehAdmin = computed(() => usuarioAtual.value?.perfil === 'ADMIN')

const lista = ref([])
const carregando = ref(true)
const busca = ref('')

const cancelando = ref(null)
const motivoCancelamento = ref('')
const salvandoCancelamento = ref(false)

const filtradas = computed(() => {
  const termo = busca.value.trim().toLowerCase()
  if (!termo) return lista.value
  return lista.value.filter((v) =>
    [String(v.id), moeda(v.valorFinal), dataHora(v.criadoEm), SITUACOES_VENDA[v.situacao]]
      .join(' ')
      .toLowerCase()
      .includes(termo),
  )
})

function corSituacao(situacao) {
  return {
    CONCLUIDA: 'situacao-pronta',
    CANCELADA: 'situacao-parado',
    EM_ABERTO: 'situacao-producao',
  }[situacao] ?? 'situacao-neutra'
}

function pagamento(valor) {
  return FORMAS_PAGAMENTO.find((f) => f.value === valor)?.title
}

async function carregar() {
  carregando.value = true
  try {
    const historico = await vendas.historico()
    lista.value = [...historico].sort((a, b) =>
      String(b.criadoEm ?? '').localeCompare(String(a.criadoEm ?? '')),
    )
  } catch (erro) {
    avisarErro(erro)
  } finally {
    carregando.value = false
  }
}

function abrir(venda) {
  if (venda.situacao === 'EM_ABERTO') {
    router.push({ name: 'venda-finalizar', params: { id: venda.id } })
  } else {
    router.push({ name: 'venda-comprovante', params: { id: venda.id } })
  }
}

async function confirmarCancelamento() {
  if (!motivoCancelamento.value.trim()) {
    return avisarErro('Informe o motivo do cancelamento.')
  }
  const ok = await executar(
    salvandoCancelamento,
    () => vendas.cancelar(cancelando.value.id, motivoCancelamento.value.trim()),
    'Venda cancelada.',
  )
  if (ok) {
    cancelando.value = null
    motivoCancelamento.value = ''
    await carregar()
  }
}

onMounted(carregar)
</script>

<template>
  <div>
    <v-text-field
      v-model="busca"
      label="Pesquisar"
      placeholder="Número, período ou valor"
      prepend-inner-icon="mdi-magnify"
      clearable
      class="mb-4"
    />

    <h2 class="subtitulo-secao mb-3">Vendas recentes</h2>

    <v-skeleton-loader v-if="carregando" type="list-item-two-line@4" />

    <EstadoVazio
      v-else-if="!filtradas.length"
      icone="mdi-receipt-text-outline"
      :titulo="busca ? 'Nada encontrado' : 'Nenhuma venda registrada'"
      :descricao="busca ? 'Tente outro termo.' : 'As vendas aparecerão aqui depois de finalizadas.'"
    >
      <v-btn v-if="!busca" to="/vendas/nova" color="primary" variant="tonal">Nova venda</v-btn>
    </EstadoVazio>

    <v-row v-else dense>
      <v-col v-for="venda in filtradas" :key="venda.id" cols="12" md="6">
        <v-card variant="outlined" rounded="lg" link @click="abrir(venda)">
          <v-card-text class="d-flex align-center ga-3">
            <v-avatar color="primary-container" size="40">
              <v-icon color="on-primary-container" size="20">mdi-receipt-text-outline</v-icon>
            </v-avatar>

            <div class="flex-grow-1" style="min-width: 0">
              <div class="d-flex align-center ga-2">
                <span
                  class="text-caption font-weight-medium"
                  :style="`color: rgb(var(--v-theme-${corSituacao(venda.situacao)}))`"
                >
                  {{ SITUACOES_VENDA[venda.situacao] ?? venda.situacao }}
                </span>
              </div>
              <div class="text-body-1 font-weight-medium">
                Venda #{{ String(venda.id).padStart(6, '0') }}
              </div>
              <div class="text-body-2 text-medium-emphasis">
                {{ dataHora(venda.criadoEm) }} ·
                {{ venda.itens?.length ?? 0 }}
                {{ (venda.itens?.length ?? 0) === 1 ? 'item' : 'itens' }}
                <template v-if="pagamento(venda.formaPagamento)">
                  · {{ pagamento(venda.formaPagamento) }}
                </template>
              </div>
            </div>

            <div class="text-right">
              <div class="text-body-1 font-weight-medium">{{ moeda(venda.valorFinal) }}</div>
              <v-btn
                v-if="venda.situacao === 'CONCLUIDA'"
                variant="text"
                size="small"
                density="comfortable"
                color="error"
                class="text-none mt-1"
                @click.stop="cancelando = venda"
              >
                Cancelar
              </v-btn>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- O protótipo tem dois botões que levam ao mesmo relatório, um por
         vendas e outro por faturamento; viraram as duas abas daquela tela. -->
    <div v-if="ehAdmin" class="d-flex flex-wrap ga-3 mt-6">
      <v-btn
        :to="{ name: 'relatorios', query: { aba: 'vendas' } }"
        variant="tonal"
        color="primary"
        class="flex-grow-1"
      >
        Relatório de vendas
      </v-btn>
      <v-btn
        :to="{ name: 'relatorios', query: { aba: 'faturamento' } }"
        variant="tonal"
        color="primary"
        class="flex-grow-1"
      >
        Faturamento
      </v-btn>
    </div>

    <v-fab
      to="/vendas/nova"
      icon="mdi-plus"
      color="primary"
      app
      appear
      location="bottom end"
      class="mb-16 mb-md-4"
      aria-label="Nova venda"
    />

    <v-dialog
      :model-value="Boolean(cancelando)"
      max-width="460"
      @update:model-value="cancelando = null"
    >
      <v-card rounded="xl" class="pa-2">
        <v-card-title class="subtitulo-secao pt-4">Cancelar venda</v-card-title>
        <v-card-text>
          <p class="mb-4">
            Cancelar a venda #{{ String(cancelando?.id ?? '').padStart(6, '0') }}?
            As peças voltam ao estoque.
          </p>
          <v-textarea
            v-model="motivoCancelamento"
            label="Motivo *"
            maxlength="500"
            rows="2"
            autofocus
          />
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="cancelando = null">Voltar</v-btn>
          <v-btn
            variant="tonal"
            color="error"
            :loading="salvandoCancelamento"
            @click="confirmarCancelamento"
          >
            Cancelar venda
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>
