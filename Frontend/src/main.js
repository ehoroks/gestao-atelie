// Só os subconjuntos latinos: o pacote completo traz cirílico, grego,
// vietnamita e matemático, que este app nunca usa.
import '@fontsource/roboto/latin-400.css'
import '@fontsource/roboto/latin-500.css'
import '@fontsource/roboto/latin-700.css'
import '@fontsource/roboto/latin-ext-400.css'
import '@fontsource/roboto/latin-ext-500.css'
import '@fontsource/roboto/latin-ext-700.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify'
import './style.css'

createApp(App).use(router).use(vuetify).mount('#app')
