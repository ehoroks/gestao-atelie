<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { categorias as apiCategorias, produtos, UNIDADES } from '../services/atelie'
import { avisarErro, executar } from '../composables/aviso'
import { paraNumero } from '../utils/formato'

/**
 * Telas "Nova peça" e "Editar peça" do protótipo, no mesmo componente.
 *
 * Dois fios soltos remendados aqui:
 *
 * 1. O protótipo tem um campo "Quantidade". A API não aceita quantidade no
 *    cadastro: ProdutoCadastroRequest pede unidadeMedida e estoqueMinimo, e o
 *    saldo só muda por movimentação de estoque. O campo virou "Estoque mínimo"
 *    mais "Unidade de medida", com um atalho para registrar a entrada inicial.
 *
 * 2. O botão "Adicionar foto" não tem para onde enviar o arquivo: o upload é
 *    da trilha J (Supabase Storage), ainda não implementada. O backend guarda
 *    apenas uma URL em `foto`, então o campo é uma URL, com prévia.
 */
const route = useRoute()
const router = useRouter()

const id = computed(() => route.params.id)
const editando = computed(() => Boolean(id.value))

const carregando = ref(false)
const salvando = ref(false)
const listaCategorias = ref([])

const form = ref({
  nome: '',
  categoriaId: null,
  preco: '',
  tamanho: '',
  cor: '',
  descricao: '',
  foto: '',
  unidadeMedida: 'UN',
  estoqueMinimo: '1',
})

async function carregar() {
  carregando.value = true
  try {
    listaCategorias.value = await apiCategorias.listar()
    if (editando.value) {
      const p = await produtos.buscar(id.value)
      form.value = {
        nome: p.nome ?? '',
        categoriaId: p.categoriaId ?? null,
        preco: p.preco ?? '',
        tamanho: p.tamanho ?? '',
        cor: p.cor ?? '',
        descricao: p.descricao ?? '',
        foto: p.foto ?? '',
        unidadeMedida: p.unidadeMedida ?? 'UN',
        estoqueMinimo: p.estoqueMinimo ?? '0',
      }
    }
  } catch (erro) {
    avisarErro(erro)
  } finally {
    carregando.value = false
  }
}

function validar() {
  if (!form.value.nome.trim()) return 'Informe o nome da peça.'
  if (paraNumero(form.value.preco) === null) return 'Informe um preço válido.'
  if (!editando.value && paraNumero(form.value.estoqueMinimo) === null) {
    return 'Informe o estoque mínimo.'
  }
  return null
}

async function salvar() {
  const problema = validar()
  if (problema) {
    avisarErro(problema)
    return
  }

  const base = {
    nome: form.value.nome.trim(),
    categoriaId: form.value.categoriaId || null,
    preco: paraNumero(form.value.preco),
    tamanho: form.value.tamanho.trim() || null,
    cor: form.value.cor.trim() || null,
    descricao: form.value.descricao.trim() || null,
    foto: form.value.foto.trim() || null,
  }

  const corpo = editando.value
    ? base
    : {
        ...base,
        unidadeMedida: form.value.unidadeMedida,
        estoqueMinimo: paraNumero(form.value.estoqueMinimo),
      }

  const salvo = await executar(
    salvando,
    () => (editando.value ? produtos.editar(id.value, corpo) : produtos.criar(corpo)),
    editando.value ? 'Peça atualizada.' : 'Peça cadastrada.',
  )

  if (!salvo) return

  // Recém-criada, a peça fica com saldo zero. Levar direto à entrada evita
  // que a proprietária cadastre a peça e não entenda por que o estoque é 0.
  if (!editando.value) {
    router.replace({ name: 'estoque-entrada', params: { id: salvo.id }, query: { inicial: '1' } })
  } else {
    router.back()
  }
}

async function desativar() {
  const ok = await executar(
    salvando,
    () => produtos.desativar(id.value),
    'Peça desativada.',
  )
  if (ok) router.push({ name: 'estoque' })
}

onMounted(carregar)
</script>

<template>
  <v-form v-if="!carregando" class="d-flex flex-column ga-4" @submit.prevent="salvar">
    <h2 class="subtitulo-secao">{{ editando ? form.nome || 'Dados da peça' : 'Dados da peça' }}</h2>

    <v-text-field v-model="form.nome" label="Nome *" maxlength="120" autofocus />

    <v-select
      v-model="form.categoriaId"
      :items="listaCategorias"
      item-title="nome"
      item-value="id"
      label="Categoria"
      clearable
      no-data-text="Nenhuma categoria cadastrada"
    >
      <template #append-item>
        <v-divider class="mb-2" />
        <v-list-item to="/categorias" prepend-icon="mdi-plus" title="Gerenciar categorias" />
      </template>
    </v-select>

    <div class="d-flex ga-4">
      <v-text-field v-model="form.tamanho" label="Tamanho" maxlength="40" placeholder="M" />
      <v-text-field v-model="form.cor" label="Cor" maxlength="60" placeholder="Lilás" />
    </div>

    <v-text-field
      v-model="form.preco"
      label="Preço (R$) *"
      inputmode="decimal"
      prefix="R$"
    />

    <!-- Só no cadastro: a edição não aceita estes dois campos. -->
    <template v-if="!editando">
      <v-select
        v-model="form.unidadeMedida"
        :items="UNIDADES"
        label="Unidade de medida *"
      />
      <v-text-field
        v-model="form.estoqueMinimo"
        label="Estoque mínimo *"
        inputmode="decimal"
        hint="Abaixo dessa quantidade a peça aparece no alerta de estoque baixo."
        persistent-hint
      />
    </template>

    <v-textarea v-model="form.descricao" label="Descrição" maxlength="1000" />

    <v-text-field
      v-model="form.foto"
      label="URL da foto"
      placeholder="https://..."
      maxlength="500"
      prepend-inner-icon="mdi-image-outline"
      hint="O envio de arquivo depende do Supabase Storage, ainda não implementado."
      persistent-hint
    />

    <v-img
      v-if="form.foto"
      :src="form.foto"
      height="160"
      cover
      rounded="lg"
      class="bg-surface-container"
    />

    <p v-if="!editando" class="texto-auxiliar">
      A peça é criada com estoque zerado. Na próxima tela você registra a quantidade inicial.
    </p>

    <div class="d-flex flex-wrap ga-3 mt-2">
      <v-btn
        v-if="editando"
        variant="tonal"
        color="error"
        class="flex-grow-1"
        :loading="salvando"
        @click="desativar"
      >
        Desativar
      </v-btn>
      <v-btn
        type="submit"
        variant="flat"
        color="primary"
        class="flex-grow-1"
        :loading="salvando"
      >
        {{ editando ? 'Salvar alterações' : 'Salvar peça' }}
      </v-btn>
    </div>
  </v-form>

  <v-skeleton-loader v-else type="article, actions" />
</template>
