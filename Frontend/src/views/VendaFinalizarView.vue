<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ItemLista from '../components/ItemLista.vue'
import CartaoResumo from '../components/CartaoResumo.vue'
import { clientes, produtos, vendas, FORMAS_PAGAMENTO } from '../services/atelie'
import { avisarErro, executar } from '../composables/aviso'
import { moeda, quantidade as fmtQuantidade } from '../utils/formato'

/**
 * Tela "Finalizar venda" do protótipo.
 *
 * VendaResponse traz apenas produtoId e clienteId, sem os nomes, então a tela
 * busca o catálogo e o cliente para conseguir exibir a revisão legível que o
 * protótipo mostra.
 */
const route = useRoute()
const router = useRouter()
const id = computed(() => route.params.id)

const venda = ref(null)
const nomesProdutos = ref({})
const cliente = ref(null)
const carregando = ref(true)
const finalizando = ref(false)

const rotuloPagamento = computed(
  () => FORMAS_PAGAMENTO.find((f) => f.value === venda.value?.formaPagamento)?.title ?? '—',
)

async function carregar() {
  carregando.value = true
  try {
    venda.value = await vendas.buscar(id.value)

    const catalogo = await produtos.listar({})
    nomesProdutos.value = Object.fromEntries(catalogo.map((p) => [p.id, p]))

    if (venda.value.clienteId) {
      cliente.value = await clientes.buscar(venda.value.clienteId).catch(() => null)
    }
  } catch (erro) {
    avisarErro(erro)
  } finally {
    carregando.value = false
  }
}

async function finalizar() {
  const concluida = await executar(
    finalizando,
    () => vendas.finalizar(id.value),
    'Venda finalizada.',
  )
  if (concluida) {
    router.replace({ name: 'venda-comprovante', params: { id: id.value } })
  }
}

onMounted(carregar)
</script>

<template>
  <div v-if="!carregando && venda" class="d-flex flex-column ga-4">
    <h2 class="subtitulo-secao">Revisão da venda</h2>

    <ItemLista
      v-for="item in venda.itens"
      :key="item.id"
      :titulo="nomesProdutos[item.produtoId]?.nome ?? `Produto #${item.produtoId}`"
      :subtitulo="`${fmtQuantidade(item.quantidade, nomesProdutos[item.produtoId]?.unidadeMedida)} × ${moeda(item.precoUnitario)}`"
      :foto="nomesProdutos[item.produtoId]?.foto"
    >
      <template #fim>
        <div class="text-body-1 font-weight-medium">{{ moeda(item.subtotal) }}</div>
      </template>
    </ItemLista>

    <div class="d-flex flex-column ga-1">
      <div class="text-body-1">Cliente: {{ cliente?.nome ?? 'Não informado' }}</div>
      <div class="text-body-1">Forma de pagamento: {{ rotuloPagamento }}</div>
      <div class="text-body-1">Desconto: {{ moeda(venda.desconto ?? 0) }}</div>
    </div>

    <CartaoResumo rotulo="Total da venda" :valor="moeda(venda.valorFinal)" destaque />

    <p class="texto-auxiliar">
      Ao confirmar, o estoque será atualizado automaticamente.
    </p>

    <div class="d-flex flex-wrap ga-3">
      <v-btn variant="tonal" color="primary" class="flex-grow-1" @click="router.back()">
        Voltar
      </v-btn>
      <v-btn
        variant="flat"
        color="primary"
        class="flex-grow-1"
        :loading="finalizando"
        @click="finalizar"
      >
        Finalizar venda
      </v-btn>
    </div>
  </div>

  <v-skeleton-loader v-else type="article, actions" />
</template>
