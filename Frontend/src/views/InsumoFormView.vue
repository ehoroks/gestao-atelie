<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { insumos, UNIDADES } from '../services/atelie'
import { avisarErro, executar } from '../composables/aviso'
import { paraNumero } from '../utils/formato'

/**
 * O protótipo não tem tela de cadastro de material: a lista de "Componentes"
 * mostra um botão "+" que não leva a lugar nenhum. Este formulário foi
 * desenhado a partir do contrato de InsumoCadastroRequest, seguindo o mesmo
 * padrão visual das outras telas de cadastro.
 */
const router = useRouter()

const salvando = ref(false)
const form = ref({
  nome: '',
  unidadeMedida: 'UN',
  estoqueMinimo: '1',
  custoUnitario: '',
})

async function salvar() {
  if (!form.value.nome.trim()) return avisarErro('Informe o nome do material.')
  if (paraNumero(form.value.estoqueMinimo) === null) return avisarErro('Informe o estoque mínimo.')
  if (paraNumero(form.value.custoUnitario) === null) return avisarErro('Informe o custo unitário.')

  const criado = await executar(
    salvando,
    () =>
      insumos.criar({
        nome: form.value.nome.trim(),
        unidadeMedida: form.value.unidadeMedida,
        estoqueMinimo: paraNumero(form.value.estoqueMinimo),
        custoUnitario: paraNumero(form.value.custoUnitario),
      }),
    'Material cadastrado.',
  )

  if (criado) {
    router.replace({ name: 'estoque-entrada', params: { id: criado.id }, query: { inicial: '1' } })
  }
}
</script>

<template>
  <v-form class="d-flex flex-column ga-4" @submit.prevent="salvar">
    <h2 class="subtitulo-secao">Dados do material</h2>

    <v-text-field
      v-model="form.nome"
      label="Nome *"
      maxlength="120"
      placeholder="Tecido algodão"
      autofocus
    />

    <v-select v-model="form.unidadeMedida" :items="UNIDADES" label="Unidade de medida *" />

    <v-text-field
      v-model="form.estoqueMinimo"
      label="Estoque mínimo *"
      inputmode="decimal"
      hint="Abaixo dessa quantidade o material entra no alerta de estoque baixo."
      persistent-hint
    />

    <v-text-field
      v-model="form.custoUnitario"
      label="Custo unitário (R$) *"
      inputmode="decimal"
      prefix="R$"
      hint="Usado no cálculo do preço sugerido das peças."
      persistent-hint
    />

    <p class="texto-auxiliar">
      O material é criado com estoque zerado. Na próxima tela você registra a quantidade inicial.
    </p>

    <v-btn type="submit" variant="flat" color="primary" :loading="salvando" class="mt-2">
      Cadastrar material
    </v-btn>
  </v-form>
</template>
