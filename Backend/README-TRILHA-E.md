# Trilha E — Integração com o Supabase (PostgreSQL)

Esta trilha conecta o backend Spring Boot ao PostgreSQL do Supabase com segurança,
sem alterar os contratos de domínio das trilhas A–D.

## O que esta entrega inclui

- Configuração de conexão por variáveis de ambiente (`DB_JDBC_URL`, `DB_USERNAME`, `DB_PASSWORD`).
- Pool Hikari com limites conservadores e parâmetros ajustáveis por ambiente.
- Flyway explícito no `application.yml` (`V100` até `V401`), com validação e proteção contra `clean`.
- Healthcheck em `/actuator/health`, incluindo o status do banco, além de `liveness` e `readiness`.
- Testes de integração que conferem o schema completo e o healthcheck com PostgreSQL real.
- Exemplo de configuração em `Backend/.env.example`.
- Documentação de conexão, migrations, segurança e troubleshooting.

**Não faz parte desta trilha:** CORS, deploy, autenticação de rotas, telas do frontend e
Supabase Storage. Esses itens pertencem às trilhas F, G, H, I e J.

## Pré-requisitos

- Projeto criado no [Supabase](https://supabase.com).
- JDK 21 e Maven 3.9+.
- Acesso a *Project Settings → Database* no painel do Supabase.

## 1. Criar o projeto no Supabase

1. Crie o projeto e escolha a região mais próxima dos usuários.
2. Defina uma senha forte para o banco e guarde-a em um gerenciador de senhas.
3. **Nunca** coloque a senha, a URL com credenciais ou chaves no Git.

## 2. Escolher a conexão

O Supabase oferece três formas de conexão. Para este backend (servidor Java contínuo),
a recomendação é o **Session Pooler**:

| Conexão | Porta | Quando usar | Observações |
|---|---|---|---|
| **Session Pooler** | `5432` | **Recomendado** para o backend e para o Flyway | Fornece IPv4, mantém sessão e suporta prepared statements |
| Direta | `5432` | Migrations pontuais | Pode ser IPv6-only; depende da rede do host |
| Transaction Pooler | `6543` | Funções serverless | Não mantém prepared statements; exige `prepareThreshold=0` e Flyway separado |

No painel, use **Connection string → JDBC** e confirme que a URL termina com
`sslmode=require`.

> No pooler, o usuário normalmente tem o formato `postgres.<project-ref>`.
> Na conexão direta, o usuário costuma ser apenas `postgres`.

## 3. Configurar as variáveis de ambiente

O Spring Boot lê as variáveis do processo. O arquivo `.env` **não** é carregado
automaticamente; exporte no shell, configure no seu IDE ou no host de deploy.

```bash
export DB_JDBC_URL='jdbc:postgresql://<host-do-pooler>:5432/postgres?sslmode=require'
export DB_USERNAME='postgres.<project-ref>'
export DB_PASSWORD='<senha-do-banco>'
```

Variáveis opcionais de pool e Flyway:

```bash
export DB_POOL_MAX_SIZE=5
export DB_POOL_MIN_IDLE=1
export DB_CONNECTION_TIMEOUT_MS=10000
export DB_MAX_LIFETIME_MS=600000
export DB_KEEPALIVE_MS=300000
export DB_CONNECT_RETRIES=5
export DB_CONNECT_RETRIES_INTERVAL=10s
```

O arquivo `Backend/.env.example` contém todos os exemplos. Ele é apenas referência:
**não** contém credenciais reais.

## 4. Subir e validar

```bash
cd Backend
mvn spring-boot:run
```

Em outro terminal:

```bash
curl -s http://localhost:8080/actuator/health
# {"status":"UP"}

curl -s http://localhost:8080/api/produtos
# []
```

Se o healthcheck retornar `DOWN`, o banco não está acessível. Para ver o detalhe
do componente `db` **apenas em desenvolvimento**, rode temporariamente:

```bash
MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=always mvn spring-boot:run
```

Em produção, mantenha os detalhes ocultos (`show-details: never`, padrão do projeto).

### Confirmar no SQL Editor do Supabase

```sql
select installed_rank, version, description, success
from flyway_schema_history
order by installed_rank;

select tablename
from pg_tables
where schemaname = 'public'
order by tablename;
```

Você deve ver as migrations `V100` a `V401` com `success = true` e as tabelas
`categorias`, `item_estoque`, `produtos`, `insumos`, `movimentacoes_estoque`,
`usuarios`, `clientes`, `vendas`, `itens_venda`, `encomendas` e `encomenda_insumos`.

## 5. Flyway: regras importantes

- O Flyway roda **automaticamente** na inicialização e aplica as migrations em ordem.
- **Não execute** `Backend/sql/trilha-c.sql` manualmente. Esse script é legado; o schema
  oficial é criado exclusivamente pelo Flyway.
- `baseline-on-migrate: false`: se o banco já tiver tabelas sem `flyway_schema_history`,
  a aplicação falha em vez de ignorar migrations silenciosamente.
- `clean-disabled: true`: impede apagar o schema por acidente.
- Se a instância estiver pausada (free tier), o Flyway tenta reconectar conforme
  `DB_CONNECT_RETRIES`/`DB_CONNECT_RETRIES_INTERVAL`.

### Usando o Transaction Pooler (6543)

O Transaction Pooler não mantém uma sessão fixa. Para esse cenário:

1. Rode as migrations com uma conexão direta ou Session Pooler.
2. Na aplicação, use a URL do Transaction Pooler com `prepareThreshold=0`:
   ```
   jdbc:postgresql://<host>:6543/postgres?sslmode=require&prepareThreshold=0
   ```
3. Desligue o Flyway na aplicação para não tentar migrar pelo pooler:
   ```bash
   export SPRING_FLYWAY_ENABLED=false
   ```

## 6. Segurança

- `.env`, `.env.local` e credenciais são ignorados pelo `.gitignore` da raiz.
- Use `sslmode=require` obrigatoriamente.
- Rotacione a senha imediatamente se ela for exposta.
- O healthcheck não expõe detalhes do banco por padrão.
- **A API ainda não tem autenticação de rotas** (isso é a Trilha H). Não exponha
  o backend publicamente antes de concluir aquela trilha.
- Opcionalmente, use usuários diferentes: um com privilégios de dono para migrations
  e outro com privilégios apenas de DML para a aplicação.

## 7. Troubleshooting

| Sintoma | Causa provável | Solução |
|---|---|---|
| `password authentication failed` | Usuário/senha errados | No pooler, use `postgres.<project-ref>`; confira a senha |
| `The connection attempt failed` / timeout | Projeto pausado, IPv6 ou host/porta errados | Retome o projeto e prefira o Session Pooler |
| `prepared statement ... already exists` | Transaction Pooler sem ajuste | Adicione `prepareThreshold=0` à URL |
| `Detected resolved migration not applied` | Histórico do Flyway inconsistente | Não rode SQL manual; use banco novo ou restaure um backup |
| `relation "..." does not exist` | Flyway desabilitado ou falhou | Verifique `spring.flyway.enabled` e os logs de startup |
| `Connection is not available, request timed out` | Pool pequeno ou banco pausado | Reduza concorrência, ajuste `DB_POOL_MAX_SIZE` e verifique o Supabase |
| `too many connections` | Muitas instâncias ou pool grande | Reduza `DB_POOL_MAX_SIZE`; cada instância abre seu próprio pool |

## 8. Checklist de aceite

- [ ] Projeto Supabase criado e senha guardada fora do Git.
- [ ] `DB_JDBC_URL`, `DB_USERNAME` e `DB_PASSWORD` configurados no ambiente.
- [ ] URL com `sslmode=require`.
- [ ] `mvn verify` verde (inclui `SaudeDoBancoIT` e `SchemaCompletoIT`).
- [ ] Logs de startup mostram as migrations aplicadas.
- [ ] `/actuator/health` retorna `UP`.
- [ ] `flyway_schema_history` contém `V100` a `V401` com sucesso.
- [ ] `GET /api/produtos` responde pela API conectada ao Supabase.
- [ ] Nenhuma credencial versionada.
