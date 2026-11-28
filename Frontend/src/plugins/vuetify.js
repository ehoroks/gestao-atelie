import 'vuetify/styles'
import { h } from 'vue'
import { createVuetify } from 'vuetify'
import { VSvgIcon } from 'vuetify/components/VIcon'
// Os aliases internos do Vuetify ($clear do campo limpável, $dropdown do
// select, etc.) vêm em SVG, com prefixo "svg:" que o Vuetify roteia sozinho.
import { aliases } from 'vuetify/iconsets/mdi-svg'
import { CAMINHOS } from './icones'

/**
 * Conjunto de ícones próprio: recebe o mesmo nome "mdi-xxx" que os templates
 * já usam e devolve o caminho SVG correspondente, sem carregar a fonte
 * inteira do Material Design Icons.
 */
const mdiEnxuto = {
  component: (props) => h(VSvgIcon, { ...props, icon: CAMINHOS[props.icon] ?? props.icon }),
}

/**
 * Tema extraído dos tokens reais do arquivo do Figma
 * (file key G11JyDS2Je7UZN1CiwnstD) pela API REST do Figma.
 * É a paleta baseline do Material 3, sem customização de marca.
 */
const atelie = {
  dark: false,
  colors: {
    background: '#FEF7FF',
    surface: '#FEF7FF',
    'surface-bright': '#FFFFFF',
    'surface-light': '#F7F2FA',
    'surface-variant': '#E7E0EC',
    'on-surface-variant': '#49454F',
    'surface-container-lowest': '#FFFFFF',
    'surface-container-low': '#F7F2FA',
    'surface-container': '#F3EDF7',
    'surface-container-high': '#ECE6F0',
    'surface-container-highest': '#E6E0E9',

    primary: '#6750A4',
    'on-primary': '#FFFFFF',
    'primary-container': '#EADDFF',
    'on-primary-container': '#4F378B',

    secondary: '#625B71',
    'on-secondary': '#FFFFFF',
    'secondary-container': '#E8DEF8',
    'on-secondary-container': '#4A4459',

    error: '#B3261E',
    'on-error': '#FFFFFF',
    'error-container': '#F9DEDC',
    'on-error-container': '#8C1D18',

    // Cores de situação, lidas direto das telas de Encomendas do protótipo.
    'situacao-producao': '#BF6A02',
    'situacao-parado': '#B3261E',
    'situacao-pronta': '#2E6B43',
    'situacao-neutra': '#49454F',

    outline: '#79747E',
    'outline-variant': '#CAC4D0',
    'on-background': '#1D1B20',
    'on-surface': '#1D1B20',
  },
  variables: {
    'border-color': '#79747E',
    'border-opacity': 1,
    'medium-emphasis-opacity': 0.72,
  },
}

export default createVuetify({
  icons: { defaultSet: 'mdi', aliases, sets: { mdi: mdiEnxuto } },
  theme: {
    defaultTheme: 'atelie',
    themes: { atelie },
  },
  defaults: {
    // A Dona Cirinha relatou dificuldade de leitura no protótipo, então os
    // campos e botões saem em densidade confortável, acima do padrão do Figma.
    VTextField: { variant: 'outlined', density: 'comfortable', hideDetails: 'auto' },
    VSelect: { variant: 'outlined', density: 'comfortable', hideDetails: 'auto' },
    VAutocomplete: { variant: 'outlined', density: 'comfortable', hideDetails: 'auto' },
    VTextarea: { variant: 'outlined', density: 'comfortable', hideDetails: 'auto', rows: 3 },
    VBtn: { rounded: 'xl', height: 48, class: 'text-none' },
    VCard: { rounded: 'lg' },
    VChip: { rounded: 'sm' },
  },
})
