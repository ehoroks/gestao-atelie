<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { auth } from '../services/atelie'
import { salvarSessao } from '../auth'
import { avisarErro } from '../composables/aviso'

const route = useRoute()
const router = useRouter()

const email = ref('')
const senha = ref('')
const verSenha = ref(false)
const entrando = ref(false)
const erro = ref('')

async function entrar() {
  erro.value = ''
  if (!email.value.trim() || !senha.value) {
    erro.value = 'Preencha o e-mail e a senha.'
    return
  }
  entrando.value = true
  try {
    const resposta = await auth.login(email.value.trim(), senha.value)
    salvarSessao({ token: resposta.token, usuario: resposta.usuario })
    router.push(route.query.destino || { name: 'inicio' })
  } catch (e) {
    // O 401 do login não derruba a sessão (ver api.js), então a mensagem
    // de credencial inválida aparece aqui mesmo, no formulário.
    erro.value = e.message
    avisarErro(e)
  } finally {
    entrando.value = false
  }
}
</script>

<template>
  <div class="d-flex align-center justify-center" style="min-height: 100dvh">
    <div class="w-100" style="max-width: 420px">
      <div class="mb-2 text-body-2 text-medium-emphasis">CIRINHA ATELIÊ</div>
      <h1 class="titulo-pagina mb-6">Login</h1>

      <v-form class="d-flex flex-column ga-4" @submit.prevent="entrar">
        <!--
          O protótipo pede "Nome de Usuário", mas POST /api/auth/login
          recebe { email, senha }. O rótulo segue o que a API aceita.
        -->
        <v-text-field
          v-model="email"
          label="E-mail"
          type="email"
          autocomplete="username"
          prepend-inner-icon="mdi-email-outline"
          :error="Boolean(erro)"
          autofocus
        />

        <v-text-field
          v-model="senha"
          label="Senha"
          :type="verSenha ? 'text' : 'password'"
          autocomplete="current-password"
          prepend-inner-icon="mdi-lock-outline"
          :append-inner-icon="verSenha ? 'mdi-eye-off-outline' : 'mdi-eye-outline'"
          :error="Boolean(erro)"
          :error-messages="erro || undefined"
          @click:append-inner="verSenha = !verSenha"
        />

        <v-btn
          type="submit"
          color="primary"
          variant="flat"
          size="large"
          :loading="entrando"
          block
        >
          Entrar
        </v-btn>
      </v-form>

      <!--
        O protótipo tinha um botão "Cadastrar" ao lado de "Entrar", mas criar
        usuário é POST /api/usuarios, restrito ao perfil ADMIN pelo
        SecurityConfig. Quem não entrou ainda não tem como se cadastrar, então
        o botão viraria um 403 garantido e foi substituído por esta orientação.
      -->
      <p class="texto-auxiliar mt-6 text-center">
        O acesso é criado pela proprietária do ateliê.
      </p>
    </div>
  </div>
</template>
