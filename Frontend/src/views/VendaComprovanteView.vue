<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ItemLista from '../components/ItemLista.vue'
import CartaoResumo from '../components/CartaoResumo.vue'
import { produtos, vendas, FORMAS_PAGAMENTO } from '../services/atelie'
import { avisarErro, avisarSucesso } from '../composables/aviso'
import { dataHora, moeda, quantidade as fmtQuantidade } from '../utils/formato'

/** Tela "Comprovante" do protótipo (RF23). */
const route = useRoute()
const router = useRouter()
const id = computed(() => route.params.id)

const venda = ref(null)
const catalogo = ref({})
const textoComprovante = ref('')
const carregando = ref(true)

const numero = computed(() => String(venda.value?.id ?? '').padStart(6, '0'))
const rotuloPagamento = computed(
  () => FORMAS_PAGAMENTO.find((f) => f.value === venda.value?.formaPagamento)?.title ?? '—',
)

async function carregar() {
  carregando.value = true
  try {
    const [buscada, lista, texto] = await Promise.all([
      vendas.buscar(id.value),
      produtos.listar({}),
      // O endpoint devolve text/plain; se falhar, a tela ainda funciona.
      vendas.comprovante(id.value).catch(() => ''),
    ])
    venda.value = buscada
    catalogo.value = Object.fromEntries(lista.map((p) => [p.id, p]))
    textoComprovante.value = texto
  } catch (erro) {
    avisarErro(erro)
  } finally {
    carregando.value = false
  }
}

async function compartilhar() {
  const texto = textoComprovante.value || montarTextoLocal()
  try {
    if (navigator.share) {
      await navigator.share({ title: `Comprovante #${numero.value}`, text: texto })
      return
    }
    await navigator.clipboard.writeText(texto)
    avisarSucesso('Comprovante copiado.')
  } catch (erro) {
    // Cancelar o compartilhamento nativo não é um erro a reportar.
    if (erro?.name !== 'AbortError') avisarErro('Não foi possível compartilhar.')
  }
}

function montarTextoLocal() {
  const linhas = [
    `Comprovante #${numero.value}`,
    dataHora(venda.value?.criadoEm),
    '',
    ...(venda.value?.itens ?? []).map(
      (i) =>
        `${catalogo.value[i.produtoId]?.nome ?? `Produto #${i.produtoId}`} — ` +
        `${i.quantidade} × ${moeda(i.precoUnitario)} = ${moeda(i.subtotal)}`,
    ),
    '',
    `Desconto: ${moeda(venda.value?.desconto ?? 0)}`,
    `Pagamento: ${rotuloPagamento.value}`,
    `Total pago: ${moeda(venda.value?.valorFinal)}`,
  ]
  return linhas.join('\n')
}

onMounted(carregar)
</script>

<template>
  <div v-if="!carregando && venda" class="d-flex flex-column ga-4">
    <div>
      <div class="subtitulo-secao" style="color: rgb(var(--v-theme-situacao-pronta))">
        Venda concluída
      </div>
      <div class="text-body-1 font-weight-medium mt-1">Comprovante #{{ numero }}</div>
      <div class="texto-auxiliar">{{ dataHora(venda.criadoEm) }}</div>
    </div>

    <ItemLista
      v-for="item in venda.itens"
      :key="item.id"
      :titulo="catalogo[item.produtoId]?.nome ?? `Produto #${item.produtoId}`"
      :subtitulo="`${fmtQuantidade(item.quantidade, catalogo[item.produtoId]?.unidadeMedida)} × ${moeda(item.precoUnitario)}`"
      :foto="catalogo[item.produtoId]?.foto"
    >
      <template #fim>
        <div class="text-body-1 font-weight-medium">{{ moeda(item.subtotal) }}</div>
      </template>
    </ItemLista>

    <div class="d-flex flex-column ga-1">
      <div class="text-body-1">Desconto: {{ moeda(venda.desconto ?? 0) }}</div>
      <div class="text-body-1">Pagamento: {{ rotuloPagamento }}</div>
    </div>

    <CartaoResumo rotulo="Total pago" :valor="moeda(venda.valorFinal)" destaque />

    <div class="d-flex flex-wrap ga-3">
      <v-btn variant="tonal" color="primary" class="flex-grow-1" @click="compartilhar">
        Compartilhar
      </v-btn>
      <v-btn
        variant="flat"
        color="primary"
        class="flex-grow-1"
        @click="router.push({ name: 'vendas' })"
      >
        Concluir
      </v-btn>
    </div>
  </div>

  <v-skeleton-loader v-else type="article, actions" />
</template>
