<script setup>
import { onMounted, ref } from 'vue'
import EstadoVazio from '../components/EstadoVazio.vue'
import { categorias } from '../services/atelie'
import { avisarErro, executar } from '../composables/aviso'

/**
 * Tela "Categorias" do protótipo. No Figma a lista abaixo do campo mostrava
 * itens de estoque ("Botão de Camisa Branco · 6"), que é resíduo de
 * placeholder: aqui ela lista categorias de verdade, com editar e excluir.
 */
const lista = ref([])
const carregando = ref(false)
const salvando = ref(false)
const nova = ref('')

const emEdicao = ref(null)
const nomeEditado = ref('')
const confirmandoExclusao = ref(null)

async function carregar() {
  carregando.value = true
  try {
    lista.value = await categorias.listar()
  } catch (erro) {
    avisarErro(erro)
  } finally {
    carregando.value = false
  }
}

async function adicionar() {
  const nome = nova.value.trim()
  if (!nome) return avisarErro('Informe o nome da categoria.')
  const criada = await executar(salvando, () => categorias.criar(nome), 'Categoria adicionada.')
  if (criada) {
    nova.value = ''
    await carregar()
  }
}

function editar(categoria) {
  emEdicao.value = categoria
  nomeEditado.value = categoria.nome
}

async function salvarEdicao() {
  const nome = nomeEditado.value.trim()
  if (!nome) return avisarErro('O nome não pode ficar vazio.')
  const ok = await executar(
    salvando,
    () => categorias.editar(emEdicao.value.id, nome),
    'Categoria atualizada.',
  )
  if (ok) {
    emEdicao.value = null
    await carregar()
  }
}

async function excluir() {
  const ok = await executar(
    salvando,
    () => categorias.excluir(confirmandoExclusao.value.id),
    'Categoria excluída.',
  )
  confirmandoExclusao.value = null
  if (ok !== undefined) await carregar()
}

onMounted(carregar)
</script>

<template>
  <div>
    <h2 class="subtitulo-secao mb-3">Organize as peças por categoria</h2>

    <v-form class="d-flex flex-column ga-3 mb-6" @submit.prevent="adicionar">
      <v-text-field
        v-model="nova"
        label="Nova categoria"
        maxlength="80"
        placeholder="Acessórios"
      />
      <v-btn type="submit" variant="tonal" color="primary" :loading="salvando" block>
        Adicionar categoria
      </v-btn>
    </v-form>

    <h2 class="subtitulo-secao mb-3">Categorias cadastradas</h2>

    <v-skeleton-loader v-if="carregando" type="list-item-two-line@3" />

    <EstadoVazio
      v-else-if="!lista.length"
      icone="mdi-tag-outline"
      titulo="Nenhuma categoria"
      descricao="As categorias ajudam a filtrar as peças no estoque."
    />

    <v-list v-else class="bg-transparent pa-0">
      <v-card
        v-for="categoria in lista"
        :key="categoria.id"
        variant="outlined"
        rounded="lg"
        class="mb-2"
      >
        <v-list-item :title="categoria.nome">
          <template #prepend>
            <v-avatar color="primary-container" size="40">
              <v-icon color="on-primary-container" size="20">mdi-tag-outline</v-icon>
            </v-avatar>
          </template>
          <template #append>
            <v-btn
              icon="mdi-pencil-outline"
              variant="text"
              size="small"
              aria-label="Editar categoria"
              @click="editar(categoria)"
            />
            <v-btn
              icon="mdi-delete-outline"
              variant="text"
              size="small"
              aria-label="Excluir categoria"
              @click="confirmandoExclusao = categoria"
            />
          </template>
        </v-list-item>
      </v-card>
    </v-list>

    <v-dialog :model-value="Boolean(emEdicao)" max-width="440" @update:model-value="emEdicao = null">
      <v-card rounded="xl" class="pa-2">
        <v-card-title class="subtitulo-secao pt-4">Editar categoria</v-card-title>
        <v-card-text>
          <v-text-field v-model="nomeEditado" label="Nome" maxlength="80" autofocus />
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="emEdicao = null">Cancelar</v-btn>
          <v-btn variant="tonal" color="primary" :loading="salvando" @click="salvarEdicao">
            Salvar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog
      :model-value="Boolean(confirmandoExclusao)"
      max-width="440"
      @update:model-value="confirmandoExclusao = null"
    >
      <v-card rounded="xl" class="pa-2">
        <v-card-title class="subtitulo-secao pt-4">Excluir categoria</v-card-title>
        <v-card-text>
          Excluir <strong>{{ confirmandoExclusao?.nome }}</strong>? As peças dessa categoria
          ficam sem categoria, mas não são apagadas.
        </v-card-text>
        <v-card-actions class="px-4 pb-4">
          <v-spacer />
          <v-btn variant="text" @click="confirmandoExclusao = null">Cancelar</v-btn>
          <v-btn variant="tonal" color="error" :loading="salvando" @click="excluir">Excluir</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>
