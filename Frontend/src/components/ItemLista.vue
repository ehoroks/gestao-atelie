<script setup>
import { computed } from 'vue'
import { iniciais } from '../utils/formato'

const props = defineProps({
  titulo: { type: String, required: true },
  subtitulo: { type: String, default: '' },
  foto: { type: String, default: '' },
  alerta: { type: Boolean, default: false },
  clicavel: { type: Boolean, default: false },
})

const letra = computed(() => iniciais(props.titulo))
</script>

<template>
  <v-card
    :ripple="clicavel"
    :link="clicavel"
    variant="outlined"
    rounded="lg"
    class="d-flex align-center overflow-hidden"
    :class="{ alerta }"
  >
    <div class="d-flex align-center flex-grow-1 pa-3 ga-3 min-w-0">
      <v-avatar :color="alerta ? 'error-container' : 'primary-container'" size="40">
        <span :class="alerta ? 'text-on-error-container' : 'text-on-primary-container'">
          {{ letra }}
        </span>
      </v-avatar>

      <div class="min-w-0 flex-grow-1">
        <div class="text-body-1 font-weight-medium text-truncate">{{ titulo }}</div>
        <div v-if="subtitulo" class="text-body-2 text-medium-emphasis text-truncate">
          {{ subtitulo }}
        </div>
        <slot name="abaixo" />
      </div>

      <slot name="fim" />
    </div>

    <!-- Miniatura da peça, no lugar do placeholder cinza do protótipo. -->
    <div
      class="miniatura d-flex align-center justify-center flex-shrink-0"
      :style="foto ? `background-image: url('${foto}')` : ''"
    >
      <v-icon v-if="!foto" color="outline" size="24">mdi-image-outline</v-icon>
    </div>
  </v-card>
</template>

<style scoped>
/*
 * No estado de alerta só a borda muda de cor. Tingir o cartão inteiro deixava
 * o nome da peça vermelho, o que lia como erro em vez de aviso.
 */
.alerta {
  border-color: rgb(var(--v-theme-error));
}
.miniatura {
  width: 72px;
  align-self: stretch;
  min-height: 64px;
  background-color: rgb(var(--v-theme-surface-container-high));
  background-size: cover;
  background-position: center;
}
.min-w-0 {
  min-width: 0;
}
</style>
