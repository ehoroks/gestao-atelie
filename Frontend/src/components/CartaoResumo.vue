<script setup>
defineProps({
  rotulo: { type: String, required: true },
  valor: { type: [String, Number], required: true },
  destaque: { type: Boolean, default: false },
})
</script>

<template>
  <!-- Cartão de número usado no Início, na Nova venda e no Relatório. -->
  <v-sheet
    :color="destaque ? 'primary-container' : 'secondary-container'"
    rounded="lg"
    class="pa-4"
  >
    <div class="text-body-2 text-medium-emphasis">{{ rotulo }}</div>
    <div
      class="text-h6 font-weight-medium mt-1"
      :class="destaque ? 'text-on-primary-container' : 'text-on-secondary-container'"
    >
      {{ valor }}
    </div>
  </v-sheet>
</template>
