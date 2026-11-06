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

O Flyway aplica automaticamente as migrations da Trilha B (`V200`, `V201`, `V202`...).
A faixa `V2xx` segue a convencao D3 (A=V1xx, B=V2xx, C=V3xx, D=V4xx).

### Limites / proximos passos

1. **B7**: `finalizar()` — exigir itens >= 1 e forma de pagamento definida;
   integrar baixa de estoque (I1, Trilha A).
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

---

## B3 — ItemVenda com preco congelado (entregue nesta branch)

B3 e uma base de **dominio + persistencia**; a API de itens nasce na B4, junto com
o agregado `Venda` (nao faz sentido criar item sem uma venda existente).

| Camada | Arquivo |
|---|---|
| Entidade JPA | `venda/ItemVenda.java` |
| Persistencia | `venda/ItemVendaRepository.java` |
| Caso de uso | `aplicacao/ServicoItemVenda.java` |
| Migration | `resources/db/migration/V202__cria_itens_venda.sql` |

### Regra central

`precoUnitario` e uma **copia congelada** do preco do produto no momento da
criacao do item:

- `ItemVenda` guarda apenas `produtoId` (fronteira temporaria) e nunca uma
  referencia viva a `Produto`;
- `precoUnitario` nao tem setter;
- mudar `Produto.preco` depois **nao** altera itens ja registrados (coberto por
  teste usando `ComposicaoPreco.aplicarComoPrecoDo`).

Outras regras: `quantidade > 0` (ate 3 casas), `precoUnitario >= 0` (2 casas),
`vendaId`/`produtoId` positivos. `calcularSubtotal()` = preco x quantidade, com
arredondamento monetario HALF_UP.

### Servico

- `adicionar(vendaId, produtoId, quantidade, precoAtualDoProduto)` — congela o preco
- `buscarPorId`, `listarPorVenda`, `remover`
- `calcularSubtotalDaVenda(vendaId)`

### Limites

- `produto_id` ainda nao tem FK: `produtos` depende da Trilha A.
- A associacao com `Venda` e as regras de adicionar/remover foram implementadas
  na B4 (ver secao abaixo).

---

## B4 — Venda + adicionar/remover itens (entregue nesta branch)

Cria o agregado `Venda` e integra `ItemVenda` a ele via JPA, substituindo a
fronteira `vendaId` da B3.

| Camada | Arquivo |
|---|---|
| Entidade JPA | `venda/Venda.java` |
| Persistencia | `venda/VendaRepository.java` |
| Caso de uso | `aplicacao/ServicoVenda.java` |
| API REST | `venda/VendaController.java` |
| DTOs | `venda/dto/AberturaVendaRequest`, `AdicionarItemRequest`, `VendaResponse`, `ItemVendaResponse` |
| Migration | `resources/db/migration/V203__cria_vendas.sql` |

### Endpoints

| Metodo | Rota | Sucesso | Erros |
|---|---|---|---|
| POST | `/api/vendas` | 201 + venda | 400, 404, 422 |
| GET | `/api/vendas` | 200 + lista | — |
| GET | `/api/vendas/{id}` | 200 + venda/itens | 404 |
| POST | `/api/vendas/{id}/itens` | 201 + item | 400, 404, 409, 422 |
| DELETE | `/api/vendas/{id}/itens/{itemId}` | 204 | 404, 409 |

Corpo do `POST /api/vendas` e opcional: sem `clienteId` a venda e de balcao.
Se informado, o cliente precisa existir (integra com a B2).

### Regras

- `adicionarItem`/`removerItem` so funcionam com `situacao == EM_ABERTO`.
  Depois de `CONCLUIDA` ou `CANCELADA` a API devolve **409**
  (`VendaNaoEditavelException`).
- O item continua congelando `precoUnitario` na criacao (regra da B3).
- `Venda` carrega os itens com `@EntityGraph` porque `open-in-view` esta
  desligado.
- `alterarSituacao` e interno (package-private); `finalizar()` e
  `cancelar(data, motivo)` entram na B7/B8.

### Limites

- `precoUnitario` ainda e aceito no request: nao existe `ProdutoRepository`
  (Trilha A). Na integracao I1 o preco passa a ser lido de `Produto.preco` no
  servidor.
- `produto_id` continua sem FK ate a Trilha A expor a tabela de produtos.
- Subtotal/valor final, desconto, forma de pagamento e comprovante ficam para
  B5/B6/B7/B9.

---

## B5 — Desconto e forma de pagamento (entregue nesta branch)

| Camada | Arquivo |
|---|---|
| Dominio | `venda/Venda.java` (`aplicarDesconto`, `definirFormaPagamento`) |
| Caso de uso | `aplicacao/ServicoVenda.java` |
| API REST | `venda/VendaController.java` |
| DTOs | `venda/dto/AplicarDescontoRequest`, `DefinirFormaPagamentoRequest` |
| Migration | `resources/db/migration/V204__adiciona_desconto_e_pagamento.sql` |

### Endpoints

| Metodo | Rota | Sucesso | Erros |
|---|---|---|---|
| PUT | `/api/vendas/{id}/desconto` | 200 + venda | 400, 404, 409, 422 |
| PUT | `/api/vendas/{id}/forma-pagamento` | 200 + venda | 400, 404, 409 |

Exemplos:

```json
// PUT /api/vendas/1/desconto
{ "valor": 15.50 }

// PUT /api/vendas/1/forma-pagamento
{ "formaPagamento": "PIX" }
```

### Decisoes

- **Ambiguidade 2 resolvida**: desconto e **valor absoluto em reais**, nao
  percentual (o nome `aplicarDesconto(valor)` do planejamento sugere absoluto).
- Desconto nao pode ser negativo nem **exceder o subtotal atual**; o valor e
  arredondado para 2 casas (HALF_UP).
- Desconto e forma de pagamento so podem ser definidos com a venda `EM_ABERTO`;
  depois de finalizada/cancelada a API devolve **409**.
- A validacao do desconto usa o subtotal interno; `calcularSubtotal` e
  `calcularValorFinal` publicos entram na B6.

---

## B6 — Subtotal e valor final (entregue nesta branch)

Torna publicos os calculos que a B5 usava internamente e os expoe na API.

| Camada | Arquivo |
|---|---|
| Dominio | `venda/Venda.java` (`calcularSubtotal`, `calcularValorFinal`) |
| API REST | `venda/dto/VendaResponse.java` (`subtotal`, `valorFinal`) |

### Regras

- `calcularSubtotal()` = soma dos subtotais dos itens (preco congelado x
  quantidade), com arredondamento de 2 casas por item (HALF_UP).
- `calcularValorFinal()` = `subtotal - desconto`, com **piso em zero**.
- O piso existe porque o desconto e validado no momento da aplicacao, mas itens
  podem ser removidos depois (venda segue `EM_ABERTO`), deixando o subtotal
  menor que o desconto ja registrado.

### Exemplo de resposta (`GET /api/vendas/{id}`)

```json
{
  "id": 1,
  "situacao": "EM_ABERTO",
  "desconto": 15.50,
  "subtotal": 100.00,
  "valorFinal": 84.50,
  "formaPagamento": "PIX",
  "itens": [ ... ]
}
```

