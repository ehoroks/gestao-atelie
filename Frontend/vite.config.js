import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vuetify from 'vite-plugin-vuetify'

export default defineConfig({
  plugins: [
    vue(),
    // Importa só os componentes e estilos do Vuetify que as telas usam.
    // Sem isto o CSS sai com ~880 kB, o que pesa no celular da proprietária.
    vuetify({ autoImport: true }),
  ],
  server: {
    port: 5173,
    proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } },
  },
})
