# Trilha H — Segurança e autorização

Esta trilha ativa o **Spring Security** no backend com autenticação **JWT
stateless** e autorização por perfil (`ADMIN` e `ATENDENTE`), além de adicionar
login/logout mínimos no frontend. Os contratos de domínio das trilhas A–G não
foram alterados.

## O que esta entrega inclui

- Login emitindo **JWT HS256** (`/api/auth/login`).
- Filtro que valida o token em todas as requisições (`Authorization: Bearer ...`).
- Autorização por perfil:
  - `/api/usuarios/**` e `/api/relatorios/**` → **ADMIN**;
  - demais rotas `/api/**` → **ADMIN ou ATENDENTE**;
  - login e `/actuator/health/**` → públicos.
- Respostas JSON padronizadas para **401** e **403**.
- Bootstrap idempotente do primeiro **ADMIN** por variáveis de ambiente.
- CORS integrado ao Spring Security (o bean `CorsConfigurationSource` da Trilha F
  é usado pelo MVC e pelo filtro de segurança).
- Frontend com tela de login, envio do Bearer token e logout.
- Testes unitários e de integração da segurança.
- Exemplo de configuração em `Backend/.env.example`.

**Não faz parte desta trilha:** recuperação de senha, refresh token, tela de
gestão de usuários, auditoria de acessos e deploy. Esses itens pertencem às
trilhas seguintes.

## 1. Como funciona

A API é **stateless**: não há sessão HTTP nem cookie. O fluxo é:

1. O frontend envia e-mail + senha para `POST /api/auth/login`.
2. O backend valida a senha com **BCrypt** e devolve um JWT assinado.
3. O frontend guarda o token em `sessionStorage` e o envia como
   `Authorization: Bearer <token>` nas chamadas seguintes.
4. O `JwtAuthenticationFilter` valida a assinatura, a expiração e o emissor
   (`cirinha-atelie`), e popula o contexto com o perfil do usuário.
5. O Spring Security decide entre **401** (sem autenticação) e **403** (perfil
   sem permissão).

O token carrega `sub` (e-mail), `nome`, `perfil` e, quando o usuário já está
persistido, `uid`. A senha nunca é incluída no token.

## 2. Variáveis de ambiente

| Variável | Obrigatória | Padrão | Descrição |
|---|---|---|---|
| `JWT_SECRET` | **Sim** (em produção) | vazio | Segredo HS256 com **mínimo de 32 caracteres** (recomendado 64 aleatórios) |
| `JWT_EXPIRATION_MINUTES` | Não | `120` | Validade do token em minutos |
| `ADMIN_EMAIL` | Não | vazio | E-mail do primeiro administrador |
| `ADMIN_SENHA` | Não | vazio | Senha do primeiro administrador (mínimo 8 caracteres) |
| `ADMIN_NOME` | Não | `Administrador` | Nome exibido do primeiro administrador |
| `CORS_ALLOWED_ORIGINS` | Não | `http://localhost:5173,http://127.0.0.1:5173` | Origens do frontend (Trilha F) |

Gerar um segredo forte:

```bash
openssl rand -base64 48
```

> **Nunca** versione o `JWT_SECRET`, a `ADMIN_SENHA` ou o `Backend/.env`.
> O `.env` já é ignorado pelo Git. Em produção, cadastre os valores no painel
> do provedor (variáveis de ambiente/secret manager).

Se o `JWT_SECRET` estiver ausente ou tiver menos de 32 caracteres, **a aplicação
não sobe** — falha rápida e explícita, evitando rodar com token previsível.

## 3. Bootstrap do primeiro administrador

Um banco novo não tem usuários, e todas as rotas de gestão de usuários exigem
ADMIN. Para destravar o primeiro acesso, o `AdminInicial` cria o usuário na
subida quando `ADMIN_EMAIL` e `ADMIN_SENHA` estão definidos:

- é **idempotente**: se o e-mail já existir, nada é alterado;
- não registra a senha em log;
- sem as variáveis, apenas registra que o bootstrap está desativado.

Depois do primeiro login, remova `ADMIN_SENHA` do ambiente para não manter a
senha em texto no servidor. Para trocar a senha atualmente existem duas opções:
criar um novo ADMIN via `POST /api/usuarios` (autenticado como ADMIN) ou
atualizar o hash BCrypt diretamente no banco. Um endpoint de troca de senha
fica para uma trilha futura.

## 4. Regras de autorização

| Rota | Acesso |
|---|---|
| `POST /api/auth/login` | Público |
| `/actuator/health`, `/actuator/health/**` | Público |
| `/api/usuarios/**` | `ADMIN` |
| `/api/relatorios/**` | `ADMIN` |
| Demais `/api/**` | Autenticado (`ADMIN` ou `ATENDENTE`) |
| Qualquer outra rota | Público (ex.: recursos estáticos) |

O CSRF está desabilitado por ser uma API stateless com token no cabeçalho. Não
use o token em cookies. Use **HTTPS** em produção.

## 5. Login e uso do token

**Requisição:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@atelie.local","senha":"SUA_SENHA"}'
```

**Resposta (200):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEmSegundos": 7200,
  "usuario": {
    "id": 1,
    "nome": "Administrador",
    "email": "admin@atelie.local",
    "perfil": "ADMIN",
    "ativo": true,
    "criadoEm": "2026-11-12T12:00:00"
  }
}
```

**Chamada autenticada:**

```bash
curl http://localhost:8080/api/produtos \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...'
```

**Credenciais inválidas (401):**

```json
{
  "momento": "2026-11-12T12:00:00Z",
  "status": 401,
  "erro": "Unauthorized",
  "mensagem": "Email ou senha invalidos",
  "campos": {}
}
```

**Sem token / token inválido (401):**

```json
{
  "momento": "2026-11-12T12:00:00Z",
  "status": 401,
  "erro": "Unauthorized",
  "mensagem": "Autenticacao obrigatoria",
  "campos": {}
}
```

**Perfil sem permissão (403):**

```json
{
  "momento": "2026-11-12T12:00:00Z",
  "status": 403,
  "erro": "Forbidden",
  "mensagem": "Acesso negado para este perfil",
  "campos": {}
}
```

## 6. Frontend

- `Frontend/src/auth.js` guarda token e usuário em **`sessionStorage`**
  (a sessão termina ao fechar a aba).
- `Frontend/src/api.js` adiciona o cabeçalho `Authorization: Bearer` e, em
  qualquer **401** fora do login, limpa a sessão e dispara o evento
  `atelie:sessao-expirada`.
- `Frontend/src/App.vue` mostra a tela de login quando não há token, exibe o
  usuário logado no topo e oferece o botão **Sair**.

> **Atenção:** variáveis `VITE_*` são embutidas no bundle e ficam públicas.
> Nunca coloque `JWT_SECRET`, senhas ou chaves em `VITE_*`. O token é obtido em
> tempo de execução e fica apenas no navegador.

## 7. Executar localmente

1. Configure o banco (Trilha E) e as variáveis em `Backend/.env`.
2. Carregue o ambiente e suba o backend:

```bash
cd Backend
set -a && source .env && set +a
mvn spring-boot:run
```

3. Na primeira subida, confirme no log que o admin foi criado.
4. Suba o frontend:

```bash
cd Frontend
npm install
npm run dev
```

5. Abra `http://localhost:5173`, entre com `ADMIN_EMAIL` e `ADMIN_SENHA` e teste
   as telas. O token fica em `sessionStorage` e é enviado automaticamente.

## 8. Produção — checklist

- [ ] `JWT_SECRET` com 64 caracteres aleatórios e guardado em secret manager.
- [ ] `ADMIN_SENHA` removida do ambiente após o primeiro login.
- [ ] HTTPS habilitado (o token trafega no cabeçalho).
- [ ] `CORS_ALLOWED_ORIGINS` com o domínio exato do frontend.
- [ ] Logs sem token, senha ou `JWT_SECRET`.
- [ ] `JWT_EXPIRATION_MINUTES` adequado à operação do ateliê.
- [ ] Ao trocar o `JWT_SECRET`, todos os tokens antigos são invalidados (todos
      precisam entrar novamente) — faça isso de forma planejada.

## 9. Testes

| Teste | O que valida |
|---|---|
| `JwtServiceTest` | Geração/leitura do token, adulteração, segredo ausente/curto e expiração |
| `AdminInicialTest` | Bootstrap idempotente, desativado sem variáveis |
| `SegurancaTest` | 401 sem token, 401 com token inválido, health público, login com token acessando rota protegida, ATENDENTE recebendo 403 e ADMIN acessando relatórios (H2, sem Docker) |
| `UsuarioControllerIT` | Cadastro + login retornando `token` e `usuario.email` |
| `CorsConfigTest` / `CorsConfigIT` | CORS continua funcionando com a segurança ativa |

```bash
cd Backend
mvn test     # unitários + H2 (rápido, sem Docker)
mvn verify   # inclui os *IT com PostgreSQL via Testcontainers (precisa de Docker)
```

## 10. Troubleshooting

| Sintoma | Causa provável | Solução |
|---|---|---|
| Aplicação não sobe com `JWT_SECRET nao configurado` | Variável ausente/curta | Defina ao menos 32 caracteres (recomendado 64) |
| Login retorna 401 | E-mail/senha errados ou usuário inativo | Confira as credenciais e o `ativo` no banco |
| Login funciona, mas a API retorna 401 | Token expirado ou `JWT_SECRET` alterado | Entre novamente; mantenha o mesmo segredo entre reinícios |
| ATENDENTE recebe 403 em `/api/usuarios` | Regra por perfil | Use um ADMIN para gestão de usuários |
| Frontend volta para o login sozinho | 401 em alguma chamada | Token expirado; faça login novamente |
| `ADMIN_*` definidos, mas nenhum admin criado | E-mail já existente | O bootstrap é idempotente; consulte o usuário existente |
| CORS bloqueado após ativar segurança | Origem fora de `CORS_ALLOWED_ORIGINS` | Cadastre a origem exata e reinicie o backend |

## 11. Checklist de aceite

- [ ] `mvn test` verde.
- [ ] `mvn verify` verde no CI (Testcontainers).
- [ ] Login devolve token e dados do usuário.
- [ ] Rotas protegidas retornam 401 sem token e 403 para perfil sem permissão.
- [ ] `/actuator/health` permanece público.
- [ ] Frontend entra, envia o token e sai corretamente.
- [ ] `Backend/.env`, `JWT_SECRET` e senhas fora do Git.
