<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { encomendas, situacaoEncomenda } from '../services/atelie'
import { avisarErro, executar } from '../composables/aviso'
import { data as fmtData, moeda, quantidade as fmtQuantidade } from '../utils/formato'

/**
 * Tela "Encomendas - Vestido A" do protótipo.
 *
 * Fio solto importante: o protótipo mostra os dados em campos editáveis e um
 * botão "Salvar alterações", mas o backend não tem endpoint de edição de
 * encomenda. O único que existe é PATCH /api/encomendas/{id}/situacao.
 *
 * Então os dados aparecem como leitura e só a situação é alterável. Reativar
 * o "Salvar alterações" exige um PUT /api/encomendas/{id} no backend.
 */
const route = useRoute()
const id = computed(() => route.params.id)

const encomenda = ref(null)
const carregando = ref(true)
const alterando = ref(false)

/** Transições oferecidas a partir da situação atual. */
const TRANSICOES = {
  REGISTRADA: ['EM_PRODUCAO', 'CANCELADA'],
  EM_PRODUCAO: ['PRONTA', 'CANCELADA'],
  PRONTA: ['ENTREGUE', 'CANCELADA'],
  ENTREGUE: [],
  CANCELADA: [],
}

const acoes = computed(() => {
  const atual = encomenda.value?.situacao
  return (TRANSICOES[atual] ?? []).map((destino) => ({
    destino,
    rotulo:
      {
        EM_PRODUCAO: 'Iniciar produção',
        PRONTA: 'Marcar como pronta',
        ENTREGUE: 'Marcar como entregue',
        CANCELADA: 'Cancelar encomenda',
      }[destino] ?? destino,
    perigo: destino === 'CANCELADA',
  }))
})

async function carregar() {
  carregando.value = true
  try {
    encomenda.value = await encomendas.buscar(id.value)
  } catch (erro) {
    avisarErro(erro)
  } finally {
    carregando.value = false
  }
}

async function alterar(destino) {
  const atualizada = await executar(
    alterando,
    () => encomendas.atualizarSituacao(id.value, destino),
    'Situação atualizada.',
  )
  if (atualizada) encomenda.value = atualizada
}

onMounted(carregar)
</script>

<template>
  <div v-if="!carregando && encomenda" class="d-flex flex-column ga-4">
    <div>
      <div
        class="text-body-2 font-weight-medium"
        :style="`color: rgb(var(--v-theme-${situacaoEncomenda(encomenda.situacao).cor}))`"
      >
        {{ situacaoEncomenda(encomenda.situacao).title }}
      </div>
      <h2 class="subtitulo-secao mt-1">
        {{ encomenda.produtoNome || encomenda.descricao }}
      </h2>
    </div>

    <v-card variant="outlined" rounded="lg">
      <v-list class="bg-transparent">
        <v-list-item title="Cliente" :subtitle="encomenda.clienteNome ?? '—'" />
        <v-divider />
        <v-list-item title="Descrição" :subtitle="encomenda.descricao || '—'" />
        <v-divider />
        <v-list-item title="Valor" :subtitle="moeda(encomenda.valor)" />
        <v-divider />
        <v-list-item title="Data do pedido" :subtitle="fmtData(encomenda.dataPedido)" />
        <v-divider />
        <v-list-item
          title="Previsão de entrega"
          :subtitle="fmtData(encomenda.dataPrevistaEntrega)"
        />
        <template v-if="encomenda.observacoes">
          <v-divider />
          <v-list-item title="Observações" :subtitle="encomenda.observacoes" />
        </template>
      </v-list>
    </v-card>

    <div v-if="encomenda.insumos?.length">
      <h2 class="subtitulo-secao mb-2">Materiais</h2>
      <v-card variant="outlined" rounded="lg">
        <v-list class="bg-transparent">
          <v-list-item
            v-for="material in encomenda.insumos"
            :key="material.insumoId"
            :title="material.nome"
            :subtitle="fmtQuantidade(material.quantidade, null)"
          />
        </v-list>
      </v-card>
      <p class="texto-auxiliar mt-2">
        {{
          encomenda.insumosConsumidos
            ? 'Materiais já baixados do estoque.'
            : 'Materiais ainda não baixados do estoque.'
        }}
      </p>
    </div>

    <div v-if="acoes.length">
      <h2 class="subtitulo-secao mb-3">Alterar situação</h2>
      <div class="d-flex flex-wrap ga-3">
        <v-btn
          v-for="acao in acoes"
          :key="acao.destino"
          :color="acao.perigo ? 'error' : 'primary'"
          variant="tonal"
          class="flex-grow-1"
          :loading="alterando"
          @click="alterar(acao.destino)"
        >
          {{ acao.rotulo }}
        </v-btn>
      </div>
    </div>

    <v-alert v-else type="info" variant="tonal" rounded="lg" density="comfortable">
      Esta encomenda está {{ situacaoEncomenda(encomenda.situacao).title.toLowerCase() }} e não
      aceita mais mudanças de situação.
    </v-alert>

    <!-- Ver a nota sobre "Salvar alterações" no comentário do topo. -->
    <p class="texto-auxiliar">
      Os dados da encomenda não podem ser editados depois do registro: a API só
      expõe a mudança de situação.
    </p>
  </div>

  <v-skeleton-loader v-else type="article, actions" />
</template>
