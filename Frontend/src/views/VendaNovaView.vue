<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ItemLista from '../components/ItemLista.vue'
import CartaoResumo from '../components/CartaoResumo.vue'
import SeletorCliente from '../components/SeletorCliente.vue'
import { produtos, vendas, FORMAS_PAGAMENTO } from '../services/atelie'
import { avisarErro, executar } from '../composables/aviso'
import { moeda, paraNumero, quantidade as fmtQuantidade } from '../utils/formato'

/**
 * Tela "Nova venda" do protótipo.
 *
 * O protótipo mostra um formulário único, mas a API é sequencial: abrir a
 * venda, adicionar cada item, aplicar desconto, definir pagamento e finalizar.
 *
 * A venda é montada como rascunho local e só vai ao servidor quando a
 * proprietária toca em "Continuar". Assim, desistir no meio não deixa vendas
 * EM_ABERTO órfãs no banco — não há endpoint para apagá-las.
 *
 * Fio solto: o protótipo tem "Adicionar produto" sem mostrar a tela de
 * escolha. Ela foi desenhada aqui como um diálogo de busca.
 */
const router = useRouter()

const clienteId = ref(null)
const itens = ref([])
const desconto = ref('')
const formaPagamento = ref(null)
const enviando = ref(false)

const seletorAberto = ref(false)
const catalogo = ref([])
const carregandoCatalogo = ref(false)
const buscaProduto = ref('')
const produtoEscolhido = ref(null)
const qtdEscolhida = ref('1')
const precoEscolhido = ref('')

const subtotal = computed(() =>
  itens.value.reduce((soma, i) => soma + i.quantidade * i.precoUnitario, 0),
)
const descontoNumero = computed(() => Math.max(0, paraNumero(desconto.value) ?? 0))
const valorFinal = computed(() => Math.max(0, subtotal.value - descontoNumero.value))

const catalogoFiltrado = computed(() => {
  const termo = buscaProduto.value.trim().toLowerCase()
  if (!termo) return catalogo.value
  return catalogo.value.filter((p) => p.nome.toLowerCase().includes(termo))
})

async function abrirSeletor() {
  seletorAberto.value = true
  produtoEscolhido.value = null
  buscaProduto.value = ''
  if (catalogo.value.length) return
  carregandoCatalogo.value = true
  try {
    catalogo.value = await produtos.listar({ ativo: true })
  } catch (erro) {
    avisarErro(erro)
  } finally {
    carregandoCatalogo.value = false
  }
}

function escolher(produto) {
  produtoEscolhido.value = produto
  qtdEscolhida.value = '1'
  precoEscolhido.value = String(produto.preco ?? '')
}

function confirmarItem() {
  const qtd = paraNumero(qtdEscolhida.value)
  const preco = paraNumero(precoEscolhido.value)
  if (qtd === null || qtd <= 0) return avisarErro('Informe uma quantidade maior que zero.')
  if (preco === null || preco < 0) return avisarErro('Informe um preço válido.')
  if (qtd > Number(produtoEscolhido.value.quantidade ?? 0)) {
    return avisarErro(
      `Estoque insuficiente: há ${fmtQuantidade(produtoEscolhido.value.quantidade, produtoEscolhido.value.unidadeMedida)} disponível.`,
    )
  }

  itens.value.push({
    produtoId: produtoEscolhido.value.id,
    nome: produtoEscolhido.value.nome,
    foto: produtoEscolhido.value.foto,
    unidadeMedida: produtoEscolhido.value.unidadeMedida,
    quantidade: qtd,
    precoUnitario: preco,
  })
  seletorAberto.value = false
}

function remover(indice) {
  itens.value.splice(indice, 1)
}

async function continuar() {
  if (!itens.value.length) return avisarErro('Adicione ao menos um produto à venda.')

  const venda = await executar(enviando, async () => {
    const aberta = await vendas.abrir(clienteId.value)

    for (const item of itens.value) {
      await vendas.adicionarItem(aberta.id, item.produtoId, item.quantidade, item.precoUnitario)
    }
    if (descontoNumero.value > 0) {
      await vendas.aplicarDesconto(aberta.id, descontoNumero.value)
    }
    if (formaPagamento.value) {
      await vendas.definirFormaPagamento(aberta.id, formaPagamento.value)
    }
    return aberta
  })

  if (venda) router.push({ name: 'venda-finalizar', params: { id: venda.id } })
}
</script>

<template>
  <div class="d-flex flex-column ga-4">
    <h2 class="subtitulo-secao">Itens da venda</h2>

    <SeletorCliente v-model="clienteId" />

    <v-row v-if="itens.length" dense>
      <v-col v-for="(item, indice) in itens" :key="indice" cols="12">
        <ItemLista
          :titulo="item.nome"
          :subtitulo="`${fmtQuantidade(item.quantidade, item.unidadeMedida)} × ${moeda(item.precoUnitario)}`"
          :foto="item.foto"
        >
          <template #fim>
            <div class="text-right">
              <div class="text-body-1 font-weight-medium">
                {{ moeda(item.quantidade * item.precoUnitario) }}
              </div>
              <v-btn
                variant="text"
                size="small"
                density="comfortable"
                color="error"
                class="text-none mt-1"
                @click="remover(indice)"
              >
                Remover
              </v-btn>
            </div>
          </template>
        </ItemLista>
      </v-col>
    </v-row>

    <v-sheet v-else color="surface-container" rounded="lg" class="pa-6 text-center">
      <v-icon color="outline" size="36" class="mb-2">mdi-cart-outline</v-icon>
      <div class="texto-auxiliar">Nenhum produto adicionado ainda.</div>
    </v-sheet>

    <v-btn variant="tonal" color="primary" prepend-icon="mdi-plus" @click="abrirSeletor">
      Adicionar produto
    </v-btn>

    <v-text-field v-model="desconto" label="Desconto (R$)" inputmode="decimal" prefix="R$" />

    <v-select
      v-model="formaPagamento"
      :items="FORMAS_PAGAMENTO"
      label="Forma de pagamento"
      clearable
    />

    <CartaoResumo rotulo="Subtotal" :valor="moeda(subtotal)" />
    <CartaoResumo rotulo="Valor final" :valor="moeda(valorFinal)" destaque />

    <v-btn
      variant="flat"
      color="primary"
      :loading="enviando"
      :disabled="!itens.length"
      @click="continuar"
    >
      Continuar
    </v-btn>

    <!-- Escolha do produto: a etapa que o protótipo não desenha. -->
    <v-dialog v-model="seletorAberto" max-width="560" scrollable>
      <v-card rounded="xl">
        <v-card-title class="subtitulo-secao pt-4 px-4">Adicionar produto</v-card-title>

        <v-card-text class="px-4">
          <template v-if="!produtoEscolhido">
            <v-text-field
              v-model="buscaProduto"
              label="Pesquisar peça"
              prepend-inner-icon="mdi-magnify"
              clearable
              autofocus
              class="mb-3"
            />

            <v-skeleton-loader v-if="carregandoCatalogo" type="list-item-two-line@3" />

            <div v-else-if="!catalogoFiltrado.length" class="texto-auxiliar py-4 text-center">
              Nenhuma peça encontrada.
            </div>

            <v-list v-else class="bg-transparent pa-0">
              <v-list-item
                v-for="p in catalogoFiltrado"
                :key="p.id"
                :title="p.nome"
                :subtitle="`${moeda(p.preco)} · ${fmtQuantidade(p.quantidade, p.unidadeMedida)} em estoque`"
                :disabled="Number(p.quantidade) <= 0"
                rounded="lg"
                @click="escolher(p)"
              >
                <template #prepend>
                  <v-avatar color="primary-container" size="40">
                    <span class="text-on-primary-container">{{ p.nome.charAt(0) }}</span>
                  </v-avatar>
                </template>
              </v-list-item>
            </v-list>
          </template>

          <template v-else>
            <div class="subtitulo-secao mb-3">{{ produtoEscolhido.nome }}</div>
            <div class="texto-auxiliar mb-4">
              {{ fmtQuantidade(produtoEscolhido.quantidade, produtoEscolhido.unidadeMedida) }} em estoque
            </div>
            <v-text-field
              v-model="qtdEscolhida"
              label="Quantidade *"
              inputmode="decimal"
              class="mb-4"
              autofocus
            />
            <v-text-field
              v-model="precoEscolhido"
              label="Preço unitário (R$) *"
              inputmode="decimal"
              prefix="R$"
            />
          </template>
        </v-card-text>

        <v-card-actions class="px-4 pb-4">
          <v-btn v-if="produtoEscolhido" variant="text" @click="produtoEscolhido = null">
            Trocar peça
          </v-btn>
          <v-spacer />
          <v-btn variant="text" @click="seletorAberto = false">Cancelar</v-btn>
          <v-btn v-if="produtoEscolhido" variant="tonal" color="primary" @click="confirmarItem">
            Adicionar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>
