<script setup>
import { computed, onMounted, ref } from 'vue'
import CartaoResumo from '../components/CartaoResumo.vue'
import { insumos, produtos } from '../services/atelie'
import { avisarErro, executar } from '../composables/aviso'
import { moeda, paraNumero, quantidade as fmtQuantidade } from '../utils/formato'

/**
 * Tela "Preço sugerido" do protótipo (RF29).
 *
 * Fio solto grande: a regra existe e está testada no backend
 * (catalogo/ComposicaoPreco.java), mas nenhum controller a expõe — ela só é
 * alcançável por dentro de ServicoEncomenda. Enquanto não houver um
 * POST /api/produtos/{id}/preco-sugerido, o cálculo é feito aqui, replicando
 * a fórmula do domínio exatamente:
 *
 *   custoMateriais = Σ (custoUnitario × quantidadeUsada)
 *   preco = custoMateriais × (1 + indiretos/100) × (1 + lucro/100)
 *
 * arredondado em 2 casas, HALF_UP. Conferido contra os números do próprio
 * protótipo: 47,00 × 1,10 × 1,60 = 82,72.
 *
 * "Margem" aqui é acréscimo sobre o custo (markup), não margem líquida sobre
 * o preço final — é o que o comentário de ComposicaoPreco define, e o próprio
 * backend marca como "revisar com a cliente antes da homologação".
 */
const catalogo = ref([])
const listaInsumos = ref([])
const carregando = ref(true)
const aplicando = ref(false)

const produtoId = ref(null)
const materiais = ref([])
const materialEscolhido = ref(null)
const qtdMaterial = ref('')
const custosIndiretos = ref('10')
const margemLucro = ref('60')
const precoAjustado = ref('')

function arredondar2(valor) {
  return Math.round((valor + Number.EPSILON) * 100) / 100
}

const custoMateriais = computed(() =>
  materiais.value.reduce((soma, m) => soma + Number(m.custoUnitario) * m.quantidade, 0),
)

const precoSugerido = computed(() => {
  if (!materiais.value.length) return null
  const indiretos = Math.max(0, paraNumero(custosIndiretos.value) ?? 0)
  const lucro = Math.max(0, paraNumero(margemLucro.value) ?? 0)
  return arredondar2(custoMateriais.value * (1 + indiretos / 100) * (1 + lucro / 100))
})

const precoFinal = computed(() => {
  const ajustado = paraNumero(precoAjustado.value)
  return ajustado !== null && ajustado >= 0 ? arredondar2(ajustado) : precoSugerido.value
})

const produtoSelecionado = computed(() =>
  catalogo.value.find((p) => p.id === produtoId.value) ?? null,
)

async function carregar() {
  carregando.value = true
  try {
    const [p, i] = await Promise.all([produtos.listar({ ativo: true }), insumos.listar()])
    catalogo.value = p
    listaInsumos.value = i
  } catch (erro) {
    avisarErro(erro)
  } finally {
    carregando.value = false
  }
}

function adicionarMaterial() {
  const qtd = paraNumero(qtdMaterial.value)
  if (!materialEscolhido.value) return avisarErro('Escolha um material.')
  if (qtd === null || qtd <= 0) return avisarErro('Informe uma quantidade maior que zero.')

  const insumo = listaInsumos.value.find((i) => i.id === materialEscolhido.value)
  materiais.value.push({
    insumoId: insumo.id,
    nome: insumo.nome,
    unidadeMedida: insumo.unidadeMedida,
    custoUnitario: insumo.custoUnitario,
    quantidade: qtd,
  })
  materialEscolhido.value = null
  qtdMaterial.value = ''
}

async function aplicarAoProduto() {
  if (!produtoSelecionado.value) return avisarErro('Escolha a peça primeiro.')
  if (precoFinal.value === null) return avisarErro('Adicione ao menos um material.')

  const p = produtoSelecionado.value
  const atualizado = await executar(
    aplicando,
    () =>
      produtos.editar(p.id, {
        nome: p.nome,
        categoriaId: p.categoriaId ?? null,
        preco: precoFinal.value,
        tamanho: p.tamanho ?? null,
        cor: p.cor ?? null,
        descricao: p.descricao ?? null,
        foto: p.foto ?? null,
      }),
    `Preço de ${p.nome} atualizado para ${moeda(precoFinal.value)}.`,
  )
  if (atualizado) {
    catalogo.value = catalogo.value.map((item) => (item.id === p.id ? atualizado : item))
  }
}

onMounted(carregar)
</script>

<template>
  <div class="d-flex flex-column ga-4">
    <h2 class="subtitulo-secao">Composição da peça</h2>

    <v-select
      v-model="produtoId"
      :items="catalogo"
      item-title="nome"
      item-value="id"
      label="Peça"
      :loading="carregando"
      clearable
      no-data-text="Nenhuma peça cadastrada"
    />

    <div v-if="produtoSelecionado" class="texto-auxiliar">
      Preço atual: <strong>{{ moeda(produtoSelecionado.preco) }}</strong>
    </div>

    <v-card
      v-for="(material, indice) in materiais"
      :key="indice"
      variant="outlined"
      rounded="lg"
    >
      <v-card-text class="d-flex align-center justify-space-between ga-2 py-3">
        <div>
          <div class="text-body-1 font-weight-medium">{{ material.nome }}</div>
          <div class="text-body-2 text-medium-emphasis">
            {{ fmtQuantidade(material.quantidade, material.unidadeMedida) }} ×
            {{ moeda(material.custoUnitario) }}
          </div>
        </div>
        <div class="d-flex align-center ga-1">
          <span class="text-body-1">
            {{ moeda(material.quantidade * Number(material.custoUnitario)) }}
          </span>
          <v-btn
            icon="mdi-close"
            variant="text"
            size="small"
            aria-label="Remover material"
            @click="materiais.splice(indice, 1)"
          />
        </div>
      </v-card-text>
    </v-card>

    <!-- Em uma linha só, o rótulo "Adicionar material" era truncado no
         celular. O seletor ganhou a largura inteira. -->
    <v-autocomplete
      v-model="materialEscolhido"
      :items="listaInsumos"
      item-title="nome"
      item-value="id"
      label="Adicionar material"
      no-data-text="Nenhum material cadastrado"
    />
    <div class="d-flex ga-2 align-start">
      <v-text-field
        v-model="qtdMaterial"
        label="Quantidade"
        inputmode="decimal"
        class="flex-grow-1"
      />
      <v-btn
        icon="mdi-plus"
        variant="tonal"
        color="primary"
        :height="56"
        :width="56"
        rounded="lg"
        aria-label="Adicionar material"
        @click="adicionarMaterial"
      />
    </div>

    <v-text-field
      v-model="custosIndiretos"
      label="Custos indiretos (%)"
      inputmode="decimal"
      suffix="%"
      hint="Luz, linha, agulha, desgaste da máquina."
      persistent-hint
    />

    <v-text-field
      v-model="margemLucro"
      label="Margem de lucro (%)"
      inputmode="decimal"
      suffix="%"
      hint="Acréscimo sobre o custo, não percentual do preço final."
      persistent-hint
    />

    <CartaoResumo rotulo="Custo dos materiais" :valor="moeda(custoMateriais)" />
    <CartaoResumo
      rotulo="Preço sugerido"
      :valor="precoSugerido === null ? '—' : moeda(precoSugerido)"
      destaque
    />

    <v-text-field
      v-model="precoAjustado"
      label="Preço ajustado (R$)"
      inputmode="decimal"
      prefix="R$"
      hint="Preencha se quiser arredondar o valor sugerido."
      persistent-hint
    />

    <v-alert
      v-if="!materiais.length"
      type="info"
      variant="tonal"
      rounded="lg"
      density="comfortable"
    >
      Adicione ao menos um material para calcular o preço.
    </v-alert>

    <v-btn
      variant="flat"
      color="primary"
      :loading="aplicando"
      :disabled="!produtoSelecionado || precoFinal === null"
      @click="aplicarAoProduto"
    >
      Aplicar {{ precoFinal === null ? '' : moeda(precoFinal) }} à peça
    </v-btn>

    <p class="texto-auxiliar">
      O cálculo é feito no aplicativo porque a API ainda não expõe a regra de
      preço sugerido. A fórmula é a mesma do backend.
    </p>
  </div>
</template>
