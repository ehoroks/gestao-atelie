<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import SeletorCliente from '../components/SeletorCliente.vue'
import { encomendas, insumos, produtos } from '../services/atelie'
import { avisarErro, executar } from '../composables/aviso'
import { hojeISO, moeda, paraNumero, quantidade as fmtQuantidade } from '../utils/formato'

/**
 * Tela "Nova encomenda" do protótipo.
 *
 * O protótipo tem "Produto solicitado" como texto livre, mas a API separa
 * produtoId (opcional, para peça de catálogo) de descricao (obrigatória).
 * Os dois aparecem: a peça é opcional porque encomenda sob medida muitas
 * vezes não existe no catálogo.
 *
 * A lista de materiais não está no protótipo, mas EncomendaRequest aceita
 * `insumos`, e é o que faz o estoque baixar quando a encomenda é produzida.
 */
const router = useRouter()

const salvando = ref(false)
const catalogo = ref([])
const listaInsumos = ref([])

const form = ref({
  clienteId: null,
  produtoId: null,
  descricao: '',
  valor: '',
  dataPedido: hojeISO(),
  dataPrevistaEntrega: '',
  observacoes: '',
})

const materiais = ref([])
const materialEscolhido = ref(null)
const qtdMaterial = ref('')

const custoMateriais = computed(() =>
  materiais.value.reduce((soma, m) => soma + m.quantidade * Number(m.custoUnitario ?? 0), 0),
)

async function carregar() {
  try {
    const [p, i] = await Promise.all([produtos.listar({ ativo: true }), insumos.listar()])
    catalogo.value = p
    listaInsumos.value = i
  } catch (erro) {
    avisarErro(erro)
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

async function salvar() {
  if (!form.value.clienteId) return avisarErro('Escolha o cliente da encomenda.')
  if (!form.value.descricao.trim()) return avisarErro('Descreva a encomenda.')
  if (!form.value.dataPrevistaEntrega) return avisarErro('Informe a previsão de entrega.')
  if (form.value.dataPrevistaEntrega < hojeISO()) {
    return avisarErro('A previsão de entrega não pode ser no passado.')
  }

  const criada = await executar(
    salvando,
    () =>
      encomendas.criar({
        clienteId: form.value.clienteId,
        produtoId: form.value.produtoId || null,
        descricao: form.value.descricao.trim(),
        valor: paraNumero(form.value.valor),
        dataPedido: form.value.dataPedido || null,
        dataPrevistaEntrega: form.value.dataPrevistaEntrega,
        observacoes: form.value.observacoes.trim() || null,
        insumos: materiais.value.map((m) => ({
          insumoId: m.insumoId,
          quantidade: m.quantidade,
        })),
      }),
    'Encomenda registrada.',
  )

  if (criada) router.replace({ name: 'encomenda-detalhe', params: { id: criada.id } })
}

onMounted(carregar)
</script>

<template>
  <v-form class="d-flex flex-column ga-4" @submit.prevent="salvar">
    <h2 class="subtitulo-secao">Dados da encomenda</h2>

    <SeletorCliente v-model="form.clienteId" obrigatorio />

    <v-select
      v-model="form.produtoId"
      :items="catalogo"
      item-title="nome"
      item-value="id"
      label="Peça do catálogo (opcional)"
      clearable
      hint="Deixe vazio se for uma peça sob medida."
      persistent-hint
    />

    <v-textarea
      v-model="form.descricao"
      label="Descrição *"
      maxlength="500"
      rows="2"
      placeholder="Vestido longo com manga"
    />

    <v-text-field v-model="form.valor" label="Valor (R$)" inputmode="decimal" prefix="R$" />

    <div class="d-flex ga-4 flex-wrap">
      <v-text-field
        v-model="form.dataPedido"
        label="Data do pedido"
        type="date"
        class="flex-grow-1"
      />
      <v-text-field
        v-model="form.dataPrevistaEntrega"
        label="Previsão de entrega *"
        type="date"
        :min="hojeISO()"
        class="flex-grow-1"
      />
    </div>

    <v-textarea v-model="form.observacoes" label="Observações" maxlength="1000" rows="2" />

    <!-- Materiais consumidos: não está no protótipo, mas é o que liga a
         encomenda ao estoque (EncomendaRequest.insumos). -->
    <div>
      <h2 class="subtitulo-secao mb-1">Materiais usados</h2>
      <p class="texto-auxiliar mb-3">
        Opcional. Se informados, saem do estoque quando a encomenda for produzida.
      </p>

      <v-card
        v-for="(material, indice) in materiais"
        :key="indice"
        variant="outlined"
        rounded="lg"
        class="mb-2"
      >
        <v-card-text class="d-flex align-center justify-space-between ga-2 py-3">
          <div>
            <div class="text-body-1">{{ material.nome }}</div>
            <div class="text-body-2 text-medium-emphasis">
              {{ fmtQuantidade(material.quantidade, material.unidadeMedida) }} ·
              {{ moeda(material.quantidade * Number(material.custoUnitario ?? 0)) }}
            </div>
          </div>
          <v-btn
            icon="mdi-close"
            variant="text"
            size="small"
            aria-label="Remover material"
            @click="materiais.splice(indice, 1)"
          />
        </v-card-text>
      </v-card>

      <v-autocomplete
        v-model="materialEscolhido"
        :items="listaInsumos"
        item-title="nome"
        item-value="id"
        label="Material"
        no-data-text="Nenhum material cadastrado"
        class="mb-4"
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

      <div v-if="materiais.length" class="texto-auxiliar mt-3">
        Custo dos materiais: <strong>{{ moeda(custoMateriais) }}</strong>
      </div>
    </div>

    <p class="texto-auxiliar">Situação inicial: REGISTRADA</p>

    <v-btn type="submit" variant="flat" color="primary" :loading="salvando" class="mt-2">
      Registrar encomenda
    </v-btn>
  </v-form>
</template>
