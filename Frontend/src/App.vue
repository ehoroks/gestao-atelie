<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDisplay } from 'vuetify'
import { usuarioAtual, limparSessao } from './auth'
import { aviso, avisarErro } from './composables/aviso'

const route = useRoute()
const router = useRouter()
const { mdAndUp } = useDisplay()

/**
 * Mesma ordem de abas do protótipo: a validação registrou que a proprietária
 * "compreendeu a configuração da navbar e navegou pelas telas sem dificuldade",
 * então mexer na ordem seria desfazer a única parte já aprovada.
 * Só o rótulo "Grupos" virou "Estoque", que ela não havia entendido.
 */
const abas = [
  { secao: 'inicio', rota: '/', rotulo: 'Início', icone: 'mdi-home-outline', iconeAtivo: 'mdi-home' },
  { secao: 'encomendas', rota: '/encomendas', rotulo: 'Encomendas', icone: 'mdi-clipboard-text-outline', iconeAtivo: 'mdi-clipboard-text' },
  { secao: 'estoque', rota: '/estoque', rotulo: 'Estoque', icone: 'mdi-package-variant-closed', iconeAtivo: 'mdi-package-variant' },
  { secao: 'vendas', rota: '/vendas', rotulo: 'Vendas', icone: 'mdi-cart-outline', iconeAtivo: 'mdi-cart' },
]

const comNavegacao = computed(() => !route.meta.semNavegacao)
const secaoAtiva = computed(() => route.meta.secao ?? 'inicio')
const titulo = computed(() => route.meta.titulo ?? 'Cirinha Ateliê')
const podeVoltar = computed(() => Boolean(route.meta.voltar))

function voltar() {
  if (window.history.length > 1) router.back()
  else router.push({ name: secaoAtiva.value })
}

function sair() {
  limparSessao()
  router.push({ name: 'login' })
}

function aoExpirarSessao() {
  avisarErro('Sua sessão expirou. Entre novamente.')
  router.push({ name: 'login' })
}

onMounted(() => window.addEventListener('atelie:sessao-expirada', aoExpirarSessao))
onUnmounted(() => window.removeEventListener('atelie:sessao-expirada', aoExpirarSessao))
</script>

<template>
  <v-app>
    <!-- Desktop: o menu vira uma coluna lateral. Mobile: barra inferior. -->
    <v-navigation-drawer
      v-if="comNavegacao && mdAndUp"
      permanent
      width="240"
      color="surface-container-low"
      border="0"
    >
      <div class="pa-6 pb-4">
        <div class="text-body-2 text-medium-emphasis">CIRINHA ATELIÊ</div>
        <div class="subtitulo-secao">Gestão</div>
      </div>

      <v-list nav density="comfortable" class="px-3">
        <v-list-item
          v-for="aba in abas"
          :key="aba.secao"
          :to="aba.rota"
          :active="secaoAtiva === aba.secao"
          :prepend-icon="secaoAtiva === aba.secao ? aba.iconeAtivo : aba.icone"
          :title="aba.rotulo"
          rounded="xl"
          active-color="on-secondary-container"
          class="mb-1"
        />
      </v-list>

      <template #append>
        <div class="pa-3">
          <v-list-item
            v-if="usuarioAtual"
            :title="usuarioAtual.nome"
            :subtitle="usuarioAtual.perfil === 'ADMIN' ? 'Proprietária' : 'Atendente'"
            rounded="xl"
          >
            <template #prepend>
              <v-avatar color="primary-container" size="36">
                <span class="text-on-primary-container">{{ (usuarioAtual.nome || '?').charAt(0) }}</span>
              </v-avatar>
            </template>
          </v-list-item>
          <v-btn
            block
            variant="text"
            prepend-icon="mdi-logout"
            class="mt-1 justify-start"
            @click="sair"
          >
            Sair
          </v-btn>
        </div>
      </template>
    </v-navigation-drawer>

    <v-main :class="comNavegacao && !mdAndUp ? 'com-barra-inferior' : ''">
      <v-container class="pa-5" style="max-width: 900px">
        <header v-if="comNavegacao" class="d-flex align-center ga-2 mb-5">
          <v-btn
            v-if="podeVoltar"
            icon="mdi-arrow-left"
            variant="text"
            size="small"
            aria-label="Voltar"
            class="ms-n2"
            @click="voltar"
          />
          <h1 class="titulo-pagina">{{ titulo }}</h1>
          <v-spacer />
          <v-btn
            v-if="!mdAndUp"
            icon="mdi-logout"
            variant="text"
            size="small"
            aria-label="Sair"
            @click="sair"
          />
        </header>

        <router-view v-slot="{ Component }">
          <component :is="Component" />
        </router-view>
      </v-container>
    </v-main>

    <v-bottom-navigation
      v-if="comNavegacao && !mdAndUp"
      :model-value="secaoAtiva"
      color="on-secondary-container"
      bg-color="surface-container"
      grow
      height="80"
    >
      <v-btn v-for="aba in abas" :key="aba.secao" :value="aba.secao" :to="aba.rota" class="text-none">
        <v-icon size="24">{{ secaoAtiva === aba.secao ? aba.iconeAtivo : aba.icone }}</v-icon>
        <span class="text-caption mt-1">{{ aba.rotulo }}</span>
      </v-btn>
    </v-bottom-navigation>

    <v-snackbar v-model="aviso.visivel" :color="aviso.cor" location="top" timeout="4500">
      {{ aviso.texto }}
      <template #actions>
        <v-btn variant="text" @click="aviso.visivel = false">Fechar</v-btn>
      </template>
    </v-snackbar>
  </v-app>
</template>
