# Trilha F — Integração Frontend ↔ API

Esta trilha conecta o frontend Vue à API Spring Boot fora do ambiente local,
sem alterar os contratos de domínio das trilhas anteriores.

## O que esta entrega inclui

- **CORS configurável** no backend, por variável de ambiente.
- Uso de **`VITE_API_BASE_URL`** no frontend (já lido por `src/api.js`).
- Mensagem amigável quando a API está fora do ar ou o CORS bloqueia a chamada.
- **Build de produção** do Vue validado.
- **CI de build do frontend** em `.github/workflows/frontend-build.yml`.
- Documentação de desenvolvimento, produção e troubleshooting.

**Não faz parte desta trilha:** deploy em si, autenticação de rotas, telas novas
e Supabase Storage. Esses itens pertencem às trilhas G, H, I e J.

## 1. Desenvolvimento local (proxy do Vite)

Em desenvolvimento **não é necessário CORS**: o Vite faz proxy de `/api` para
`http://localhost:8080` (veja `vite.config.js`).

```bash
# terminal 1 — backend
cd Backend
mvn spring-boot:run

# terminal 2 — frontend
cd Frontend
npm install
npm run dev
# abra http://localhost:5173
```

O frontend chama `/api/...` no mesmo host do Vite, e o proxy encaminha para o
backend. Por isso `VITE_API_BASE_URL` pode ficar **vazio** em desenvolvimento.

## 2. CORS no backend

A configuração está em
[`Backend/src/main/java/br/com/atelie/config/CorsConfig.java`](../Backend/src/main/java/br/com/atelie/config/CorsConfig.java)
e usa a variável:

```bash
export CORS_ALLOWED_ORIGINS='http://localhost:5173,http://127.0.0.1:5173'
```

- A lista é separada por **vírgula**.
- O padrão (sem a variável) libera `http://localhost:5173` e `http://127.0.0.1:5173`.
- Métodos liberados: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
- Cabeçalhos liberados: `Accept`, `Authorization`, `Content-Type`.
- Preflight (`OPTIONS`) fica em cache por 1 hora (`maxAge`).
- O mapeamento vale para **`/api/**`**; o Actuator não é exposto por CORS.

`*` é aceito, mas **não recomendado** com a API pública. Prefira listar o domínio
real do frontend.

## 3. URL da API no frontend

`Frontend/src/api.js` monta as chamadas a partir de:

```js
const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
```

Exemplos:

| Cenário | `VITE_API_BASE_URL` | CORS necessário? |
|---|---|---|
| Desenvolvimento com proxy do Vite | vazio | Não |
| Produção no mesmo domínio (reverse proxy `/api`) | vazio | Não |
| Produção com frontend e API em domínios diferentes | `https://api.atelie.exemplo.com` | Sim |

> **Importante:** no Vite, variáveis `VITE_*` são resolvidas em **tempo de build**
> e ficam embutidas no bundle. Depois de mudar o valor, rode `npm run build`
> novamente. Nunca coloque senhas, tokens ou chaves em variáveis `VITE_*`.

## 4. Build de produção

```bash
cd Frontend
npm ci
npm run build
# resultado em Frontend/dist/
```

Para conferir o build localmente:

```bash
npm run preview
```

O `dist/` e o `node_modules/` são ignorados pelo Git (`Frontend/.gitignore`).

## 5. Integração em produção

**Opção A — mesmo domínio (recomendada):**

- Sirva o frontend em `https://atelie.exemplo.com`.
- Configure um reverse proxy para encaminhar `https://atelie.exemplo.com/api`
  para o backend.
- Deixe `VITE_API_BASE_URL` vazio e **não** é necessário CORS.

**Opção B — domínios diferentes:**

- Frontend em `https://atelie.exemplo.com`.
- API em `https://api.atelie.exemplo.com`.
- No build do frontend: `VITE_API_BASE_URL=https://api.atelie.exemplo.com`.
- No backend: `CORS_ALLOWED_ORIGINS=https://atelie.exemplo.com`.

## 6. CI de build

O workflow `.github/workflows/frontend-build.yml` roda em mudanças na pasta
`Frontend/` ou no próprio workflow:

1. `actions/setup-node` com Node 24 e cache do npm;
2. `npm ci` (usa o `package-lock.json` versionado);
3. `npm run build`.

## 7. Troubleshooting

| Sintoma | Causa provável | Solução |
|---|---|---|
| Erro de CORS no navegador | Origem não está em `CORS_ALLOWED_ORIGINS` | Cadastre a origem exata (esquema + host + porta) e reinicie o backend |
| `403 Invalid CORS request` no preflight | Origem não permitida | Corrija `CORS_ALLOWED_ORIGINS` |
| `404` em `/api/...` | `VITE_API_BASE_URL` errado ou proxy ausente | Confira a URL da API e o reverse proxy |
| "Não foi possível conectar à API" | Backend fora do ar, URL errada ou CORS | Suba o backend e valide `curl /actuator/health` |
| Mudou `VITE_API_BASE_URL` e nada aconteceu | Vite embute a variável no build | Rode `npm run build` novamente |
| `npm ci` falha no CI | `package-lock.json` ausente/desatualizado | Rode `npm install` localmente e versione o lock |

## 8. Checklist de aceite

- [ ] `npm ci && npm run build` conclui sem erros.
- [ ] Frontend em desenvolvimento conversa com o backend pelo proxy do Vite.
- [ ] `CORS_ALLOWED_ORIGINS` configurado com o domínio correto em produção.
- [ ] Testes `CorsConfigTest` e `CorsConfigIT` verdes.
- [ ] Nenhum segredo em variáveis `VITE_*`.
- [ ] Workflow `frontend-build` verde no GitHub Actions.
