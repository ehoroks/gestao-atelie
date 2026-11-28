<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { estoque, insumos } from '../services/atelie'
import { avisarErro, executar } from '../composables/aviso'
import { paraNumero, quantidade as fmtQuantidade } from '../utils/formato'

/**
 * Tela "Entrada de estoque" do protótipo.
 *
 * Fio solto: o protótipo pede "Custo unitário" no mesmo formulário, mas
 * POST /api/estoque/{id}/entradas só aceita { quantidade, motivo }. O custo
 * pertence ao insumo (PATCH /api/insumos/{id}/custo) e não existe para
 * produtos. Então o campo só aparece para materiais e, quando alterado,
 * dispara a segunda chamada.
 */
const route = useRoute()
const router = useRouter()

const id = computed(() => route.params.id)
const ehInicial = computed(() => route.query.inicial === '1')

const item = ref(null)
const carregando = ref(true)
const salvando = ref(false)

const quantidade = ref('')
const custoUnitario = ref('')
const custoOriginal = ref(null)
const motivo = ref('')

const ehInsumo = computed(() => item.value?.tipo === 'INSUMO')

async function carregar() {
  carregando.value = true
  try {
    item.value = await estoque.buscar(id.value)
    if (item.value.tipo === 'INSUMO') {
      const insumo = await insumos.buscar(id.value)
      custoOriginal.value = insumo.custoUnitario
      custoUnitario.value = String(insumo.custoUnitario ?? '')
    }
    if (ehInicial.value) motivo.value = 'Quantidade inicial'
  } catch (erro) {
    avisarErro(erro)
  } finally {
    carregando.value = false
  }
}

async function registrar() {
  const qtd = paraNumero(quantidade.value)
  if (qtd === null || qtd <= 0) return avisarErro('Informe uma quantidade maior que zero.')

  const ok = await executar(
    salvando,
    async () => {
      await estoque.entrada(id.value, qtd, motivo.value.trim() || null)

      // Só chama o custo se o material existir e o valor tiver mudado.
      const novoCusto = paraNumero(custoUnitario.value)
      if (ehInsumo.value && novoCusto !== null && Number(custoOriginal.value) !== novoCusto) {
        await insumos.atualizarCusto(id.value, novoCusto)
      }
      return true
    },
    'Entrada registrada.',
  )

  if (ok) router.push({ name: 'estoque' })
}

onMounted(carregar)
</script>

<template>
  <v-form v-if="!carregando && item" class="d-flex flex-column ga-4" @submit.prevent="registrar">
    <h2 class="subtitulo-secao">
      {{ ehInicial ? 'Quantidade inicial' : 'Registrar entrada' }}
    </h2>

    <v-text-field
      :model-value="item.nome"
      :label="ehInsumo ? 'Material' : 'Peça'"
      readonly
      variant="filled"
    />

    <v-text-field
      :model-value="fmtQuantidade(item.quantidade, item.unidadeMedida)"
      label="Quantidade atual"
      readonly
      variant="filled"
    />

    <v-text-field
      v-model="quantidade"
      label="Quantidade a entrar *"
      inputmode="decimal"
      :suffix="{ UN: 'un', M: 'm', G: 'g' }[item.unidadeMedida]"
      autofocus
    />

    <v-text-field
      v-if="ehInsumo"
      v-model="custoUnitario"
      label="Custo unitário (R$)"
      inputmode="decimal"
      prefix="R$"
      hint="Se o valor mudou nesta compra, atualize aqui."
      persistent-hint
    />

    <v-textarea
      v-model="motivo"
      label="Observações"
      maxlength="500"
      rows="2"
      placeholder="Compra de reposição"
    />

    <p class="texto-auxiliar">A entrada será registrada no histórico de movimentações.</p>

    <v-btn type="submit" variant="flat" color="primary" :loading="salvando" class="mt-2">
      Registrar entrada
    </v-btn>
  </v-form>

  <v-skeleton-loader v-else type="article, actions" />
</template>
