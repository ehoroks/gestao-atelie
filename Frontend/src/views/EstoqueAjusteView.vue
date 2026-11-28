<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { estoque } from '../services/atelie'
import { avisarErro, executar } from '../composables/aviso'
import { paraNumero, quantidade as fmtQuantidade } from '../utils/formato'

/** Tela "Ajuste de estoque" do protótipo (RF10). */
const route = useRoute()
const router = useRouter()

const id = computed(() => route.params.id)
const item = ref(null)
const carregando = ref(true)
const salvando = ref(false)

const novaQuantidade = ref('')
const motivo = ref('')

const diferenca = computed(() => {
  const nova = paraNumero(novaQuantidade.value)
  if (nova === null || !item.value) return null
  return nova - Number(item.value.quantidade ?? 0)
})

async function carregar() {
  carregando.value = true
  try {
    item.value = await estoque.buscar(id.value)
    novaQuantidade.value = String(item.value.quantidade ?? '')
  } catch (erro) {
    avisarErro(erro)
  } finally {
    carregando.value = false
  }
}

async function salvar() {
  const nova = paraNumero(novaQuantidade.value)
  if (nova === null || nova < 0) return avisarErro('Informe uma quantidade válida.')
  if (!motivo.value.trim()) return avisarErro('O motivo do ajuste é obrigatório.')

  const ok = await executar(
    salvando,
    () => estoque.ajustar(id.value, nova, motivo.value.trim()),
    'Ajuste salvo.',
  )
  if (ok) router.push({ name: 'estoque' })
}

onMounted(carregar)
</script>

<template>
  <v-form v-if="!carregando && item" class="d-flex flex-column ga-4" @submit.prevent="salvar">
    <h2 class="subtitulo-secao">Corrigir quantidade</h2>

    <v-text-field
      :model-value="item.nome"
      :label="item.tipo === 'INSUMO' ? 'Material' : 'Peça'"
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
      v-model="novaQuantidade"
      label="Nova quantidade *"
      inputmode="decimal"
      :suffix="{ UN: 'un', M: 'm', G: 'g' }[item.unidadeMedida]"
      autofocus
    />

    <v-alert
      v-if="diferenca !== null && diferenca !== 0"
      :type="diferenca > 0 ? 'info' : 'warning'"
      variant="tonal"
      density="comfortable"
      rounded="lg"
    >
      {{ diferenca > 0 ? 'Acréscimo' : 'Baixa' }} de
      {{ fmtQuantidade(Math.abs(diferenca), item.unidadeMedida) }}.
    </v-alert>

    <v-textarea
      v-model="motivo"
      label="Motivo do ajuste *"
      maxlength="500"
      rows="2"
      placeholder="Perda no corte"
    />

    <p class="texto-auxiliar">
      O motivo é obrigatório para manter a rastreabilidade do estoque.
    </p>

    <v-btn type="submit" variant="flat" color="primary" :loading="salvando" class="mt-2">
      Salvar ajuste
    </v-btn>
  </v-form>

  <v-skeleton-loader v-else type="article, actions" />
</template>
