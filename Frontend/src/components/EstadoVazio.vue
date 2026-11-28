<script setup>
defineProps({
  icone: { type: String, default: 'mdi-tray-arrow-down' },
  titulo: { type: String, required: true },
  descricao: { type: String, default: '' },
})
</script>

<template>
  <div class="text-center py-10 px-4">
    <v-icon :icon="icone" size="48" color="outline" class="mb-3" />
    <div class="subtitulo-secao mb-1">{{ titulo }}</div>
    <div v-if="descricao" class="texto-auxiliar">{{ descricao }}</div>
    <div class="mt-4">
      <slot />
    </div>
  </div>
</template>
