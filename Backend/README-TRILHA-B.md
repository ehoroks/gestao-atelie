# Trilha B — Vendas e Acesso

## B1 — Usuario + autenticacao (entregue nesta branch)

Fatia vertical completa em Spring Boot, servindo de modelo para B2 em diante:

| Camada | Arquivo |
|---|---|
| Entidade JPA | `usuario/Usuario.java` + `usuario/PerfilUsuario.java` |
| Persistencia | `usuario/UsuarioRepository.java` (`JpaRepository`) |
| Caso de uso | `aplicacao/ServicoUsuario.java` |
| API REST | `usuario/UsuarioController.java`, `usuario/AutenticacaoController.java` |
| DTOs | `usuario/dto/*` |
| Senha | `infra/ConfiguracaoSenha.java` (BCrypt) |
| Erros HTTP | `api/TratadorGlobalDeExcecoes.java` + `api/ErroResposta.java` |
| Migration | `resources/db/migration/V200__cria_usuarios.sql` |

### Endpoints

| Metodo | Rota | Sucesso | Erros |
|---|---|---|---|
| POST | `/api/usuarios` | 201 + usuario | 400, 409, 422 |
| GET | `/api/usuarios/{id}` | 200 | 404 |
| GET | `/api/usuarios` | 200 | — |
| POST | `/api/auth/login` | 200 + usuario | 400, 401 |

Exemplo:

```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Ana","email":"ana@exemplo.com","senha":"senha12345","perfil":"ATENDENTE"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana@exemplo.com","senha":"senha12345"}'
```

### Decisoes desta fatia

- **Senha**: apenas hash BCrypt (`spring-security-crypto`). A senha em texto puro
  existe somente dentro de `ServicoUsuario.cadastrar`. O hash nunca aparece na API.
- **Sem `spring-boot-starter-security` completo ainda**: para nao bloquear rotas
  antes de a Trilha D definir a politica de autorizacao. Token/JWT/sessao e a
  proxima etapa.
- **Perfis**: `ADMIN` e `ATENDENTE` (suposicao — confirmar com a equipe).
- **Login sem cliente**: `POST /api/auth/login` devolve os dados do usuario; nao
  emite token nesta etapa.
- **Persistencia**: JPA/Spring Data no dominio de Usuario, começando a migracao
  incremental para "Spring Boot completo". As entidades da Sprint 0
  (`ItemEstoque`, `Produto`, `Encomenda`) **nao** foram alteradas.
- **Erros**: `DominioException` → HTTP centralizado, base do D2 (Trilha D).

### Como executar os testes

```bash
cd Backend
mvn clean test
```

Os testes de integracao usam **H2 em modo PostgreSQL** (`application-test.yml`),
sem Docker. Quando o ambiente tiver Docker, a evolucao natural e migrar para
Testcontainers + PostgreSQL real (D9).

### Como subir com PostgreSQL/Supabase

```bash
cd Backend
export DB_JDBC_URL='jdbc:postgresql://localhost:5432/cirinha_atelie'
export DB_USERNAME='postgres'
export DB_PASSWORD='sua-senha'
mvn spring-boot:run
```

O Flyway aplica `V200__cria_usuarios.sql` automaticamente na subida.
A faixa `V2xx` segue a convencao D3 (A=V1xx, B=V2xx, C=V3xx, D=V4xx).

### Limites / proximos passos

1. **B3**: `Venda`/`ItemVenda` com `precoUnitario` congelado, seguindo o mesmo
   template (entidade + repository + service + controller + testes + migration).
2. **Spring Security completo**: autenticacao stateless com JWT e autorizacao por
   `PerfilUsuario`, coordenada com a Trilha D.
3. **Testcontainers**: substituir H2 quando o Docker estiver disponivel.
4. **Relacionamentos**: `Usuario` ↔ `Cliente` (ambiguidade 1 do planejamento)
   continua em aberto e deve ser resolvido com a equipe.

---

## B2 — Cliente + CRUD + busca (entregue nesta branch)

Mesmo padrao arquitetural da B1:

| Camada | Arquivo |
|---|---|
| Entidade JPA | `cliente/Cliente.java` |
| Persistencia | `cliente/ClienteRepository.java` |
| Caso de uso | `aplicacao/ServicoCliente.java` |
| API REST | `cliente/ClienteController.java` |
| DTOs | `cliente/dto/ClienteRequest.java`, `ClienteResponse.java` |
| Migration | `resources/db/migration/V201__cria_clientes.sql` |

### Endpoints

| Metodo | Rota | Sucesso | Erros |
|---|---|---|---|
| POST | `/api/clientes` | 201 + cliente | 400, 422 |
| GET | `/api/clientes?termo=...` | 200 + lista | — |
| GET | `/api/clientes/{id}` | 200 | 404 |
| PUT | `/api/clientes/{id}` | 200 + cliente | 400, 404, 422 |
| DELETE | `/api/clientes/{id}` | 204 | 404 |

`termo` busca por trecho do **nome** (case-insensitive) ou por trecho dos
**digitos do telefone**. Sem `termo`, lista todos ordenados por nome.

### Decisoes

- **Telefone** normalizado para apenas digitos (10 ou 11) antes de persistir;
  `(11) 98765-4321` vira `11987654321`.
- **Email opcional**: em branco vira `null`; quando informado, e normalizado para
  minusculas e validado.
- **Sem unicidade de email/telefone**: nao ha requisito; familias podem
  compartilhar telefone.
- **Usuario ↔ Cliente**: associacao ainda nao implementada (ambiguidade 1).
- **Exclusao**: `DELETE` remove o registro. Quando `Venda` existir, sera preciso
  decidir entre bloquear exclusao de cliente com vendas (soft delete) ou usar
  `ON DELETE RESTRICT`.

