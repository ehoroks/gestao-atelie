<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import ItemLista from '../components/ItemLista.vue'
import EstadoVazio from '../components/EstadoVazio.vue'
import { insumos, produtos } from '../services/atelie'
import { avisarErro } from '../composables/aviso'
import { moeda, quantidade as fmtQuantidade } from '../utils/formato'

/**
 * Tela "Controle de Estoque" do protótipo.
 *
 * Nomenclatura: a validação registrou que "Componentes" e "Peças" não foram
 * compreendidos. O próprio protótipo já usa "materiais" na tela de preço
 * sugerido, então as abas são "Produtos" e "Materiais". Na API continuam
 * sendo Produto e Insumo.
 */
const router = useRouter()

const aba = ref('PRODUTO')
const busca = ref('')
const carregando = ref(false)
const itens = ref([])
const selecionado = ref(null)

const ehProduto = computed(() => aba.value === 'PRODUTO')

async function carregar() {
  carregando.value = true
  try {
    const nome = busca.value.trim() || undefined
    itens.value = ehProduto.value
      ? await produtos.listar({ nome, ativo: true })
      : await insumos.listar(nome)
  } catch (erro) {
    avisarErro(erro)
    itens.value = []
  } finally {
    carregando.value = false
  }
}

function subtitulo(item) {
  const qtd = fmtQuantidade(item.quantidade, item.unidadeMedida)
  return ehProduto.value ? `${moeda(item.preco)} · ${qtd} em estoque` : qtd
}

function abrirAcoes(item) {
  selecionado.value = item
}

function irPara(nome) {
  const id = selecionado.value.id
  selecionado.value = null
  router.push({ name: nome, params: { id } })
}

let temporizador
watch(busca, () => {
  clearTimeout(temporizador)
  temporizador = setTimeout(carregar, 300)
})
watch(aba, carregar)

onMounted(carregar)
</script>

<template>
  <div>
    <v-btn-toggle
      v-model="aba"
      mandatory
      divided
      rounded="xl"
      color="primary"
      variant="outlined"
      class="w-100 mb-4"
      style="max-width: 420px"
    >
      <v-btn value="PRODUTO" class="flex-grow-1 text-none" height="48">Produtos</v-btn>
      <v-btn value="INSUMO" class="flex-grow-1 text-none" height="48">Materiais</v-btn>
    </v-btn-toggle>

    <v-text-field
      v-model="busca"
      :label="ehProduto ? 'Pesquisar peça' : 'Pesquisar material'"
      prepend-inner-icon="mdi-magnify"
      clearable
      class="mb-4"
    />

    <div v-if="carregando" class="d-flex flex-column ga-3">
      <v-skeleton-loader v-for="n in 4" :key="n" type="list-item-avatar-two-line" rounded="lg" />
    </div>

    <EstadoVazio
      v-else-if="!itens.length"
      :icone="ehProduto ? 'mdi-hanger' : 'mdi-content-cut'"
      :titulo="busca ? 'Nada encontrado' : ehProduto ? 'Nenhuma peça cadastrada' : 'Nenhum material cadastrado'"
      :descricao="busca ? 'Tente outro termo de busca.' : 'Cadastre o primeiro para começar a controlar o estoque.'"
    >
      <v-btn
        v-if="!busca"
        :to="ehProduto ? '/produtos/novo' : '/insumos/novo'"
        color="primary"
        variant="tonal"
      >
        {{ ehProduto ? 'Cadastrar peça' : 'Cadastrar material' }}
      </v-btn>
    </EstadoVazio>

    <v-row v-else dense>
      <v-col v-for="item in itens" :key="item.id" cols="12" md="6">
        <ItemLista
          :titulo="item.nome"
          :subtitulo="subtitulo(item)"
          :foto="item.foto"
          :alerta="item.estoqueBaixo"
          clicavel
          @click="abrirAcoes(item)"
        >
          <!-- O selo fica na própria coluna de texto: como chip à direita ele
               espremia o subtítulo e era cortado em telas estreitas. -->
          <template #abaixo>
            <v-chip
              v-if="item.estoqueBaixo"
              color="error"
              size="x-small"
              variant="tonal"
              class="mt-1"
            >
              Estoque baixo
            </v-chip>
          </template>
        </ItemLista>
      </v-col>
    </v-row>

    <!-- Atalhos que o protótipo deixa soltos: Categorias e Preço sugerido
         não têm entrada na navegação, mas são requisitos (RF07 e RF29). -->
    <div class="d-flex flex-wrap ga-3 mt-6">
      <v-btn to="/categorias" variant="tonal" color="primary" class="flex-grow-1">
        Categorias
      </v-btn>
      <v-btn to="/preco-sugerido" variant="tonal" color="primary" class="flex-grow-1">
        Preço sugerido
      </v-btn>
    </div>

    <v-fab
      :to="ehProduto ? '/produtos/novo' : '/insumos/novo'"
      icon="mdi-plus"
      color="primary"
      app
      appear
      location="bottom end"
      class="mb-16 mb-md-4"
      :aria-label="ehProduto ? 'Nova peça' : 'Novo material'"
    />

    <!-- Ações de um item. O protótipo pula direto para "Entrada" e "Ajuste"
         sem mostrar como se chega lá a partir da lista. -->
    <v-bottom-sheet :model-value="Boolean(selecionado)" @update:model-value="selecionado = null">
      <v-card v-if="selecionado" rounded="t-xl">
        <v-card-title class="subtitulo-secao pt-4">{{ selecionado.nome }}</v-card-title>
        <v-list>
          <v-list-item
            prepend-icon="mdi-tray-arrow-down"
            title="Registrar entrada"
            @click="irPara('estoque-entrada')"
          />
          <v-list-item
            prepend-icon="mdi-tune-variant"
            title="Ajustar quantidade"
            @click="irPara('estoque-ajuste')"
          />
          <v-list-item
            prepend-icon="mdi-history"
            title="Ver movimentações"
            @click="irPara('estoque-movimentacoes')"
          />
          <v-list-item
            v-if="ehProduto"
            prepend-icon="mdi-pencil-outline"
            title="Editar peça"
            @click="irPara('produto-editar')"
          />
        </v-list>
        <v-card-actions class="pb-4 px-4">
          <v-spacer />
          <v-btn variant="text" @click="selecionado = null">Fechar</v-btn>
        </v-card-actions>
      </v-card>
    </v-bottom-sheet>
  </div>
</template>
