<script setup>
import { onMounted, ref } from 'vue'
import { clientes } from '../services/atelie'
import { avisarErro, executar } from '../composables/aviso'

/**
 * Remenda um fio solto do protótipo: nas telas de Nova venda e Nova encomenda
 * o campo "Cliente" é texto livre, mas a API exige clienteId (obrigatório em
 * encomendas, opcional em vendas).
 *
 * A Dona Cirinha disse na validação que não quer uma seção de Clientes, porque
 * já organiza isso pelo WhatsApp. Então não existe tela de cadastro: o cliente
 * é escolhido ou criado aqui mesmo, com o mínimo que o backend pede.
 */
const modelo = defineModel({ default: null })

defineProps({
  rotulo: { type: String, default: 'Cliente' },
  obrigatorio: { type: Boolean, default: false },
})

const lista = ref([])
const carregando = ref(false)
const salvando = ref(false)
const dialogo = ref(false)
const novo = ref({ nome: '', telefone: '', email: '' })

async function carregar() {
  carregando.value = true
  try {
    lista.value = await clientes.listar()
  } catch (erro) {
    avisarErro(erro)
  } finally {
    carregando.value = false
  }
}

function abrirNovo() {
  novo.value = { nome: '', telefone: '', email: '' }
  dialogo.value = true
}

async function salvarNovo() {
  if (!novo.value.nome.trim() || !novo.value.telefone.trim()) {
    avisarErro('Informe ao menos o nome e o telefone.')
    return
  }
  const criado = await executar(
    salvando,
    () =>
      clientes.criar({
        nome: novo.value.nome.trim(),
        telefone: novo.value.telefone.trim(),
        email: novo.value.email.trim() || null,
      }),
    'Cliente adicionado.',
  )
  if (criado) {
    lista.value = [...lista.value, criado]
    modelo.value = criado.id
    dialogo.value = false
  }
}

onMounted(carregar)
</script>

<template>
  <div class="d-flex ga-2 align-start">
    <v-autocomplete
      v-model="modelo"
      :items="lista"
      item-title="nome"
      item-value="id"
      :label="obrigatorio ? `${rotulo} *` : `${rotulo} (opcional)`"
      :loading="carregando"
      :clearable="!obrigatorio"
      no-data-text="Nenhum cliente cadastrado"
      class="flex-grow-1"
    />
    <v-btn
      icon="mdi-account-plus-outline"
      variant="tonal"
      color="primary"
      aria-label="Adicionar cliente"
      :height="56"
      :width="56"
      rounded="lg"
      @click="abrirNovo"
    />
  </div>

  <v-dialog v-model="dialogo" max-width="480">
    <v-card rounded="xl" class="pa-2">
      <v-card-title class="subtitulo-secao pt-4">Novo cliente</v-card-title>
      <v-card-text class="d-flex flex-column ga-4 pt-2">
        <v-text-field v-model="novo.nome" label="Nome *" maxlength="120" autofocus />
        <v-text-field v-model="novo.telefone" label="Telefone *" maxlength="20" />
        <v-text-field v-model="novo.email" label="E-mail" type="email" maxlength="180" />
        <div class="texto-auxiliar">Só o nome e o telefone são obrigatórios.</div>
      </v-card-text>
      <v-card-actions class="px-4 pb-4 ga-2">
        <v-spacer />
        <v-btn variant="text" @click="dialogo = false">Cancelar</v-btn>
        <v-btn variant="tonal" color="primary" :loading="salvando" @click="salvarNovo">
          Adicionar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
