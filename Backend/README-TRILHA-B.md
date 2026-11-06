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

1. **B9**: `gerarComprovante()` — emitir comprovante da venda.
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
- `alterarSituacao` e interno (package-private); a finalizacao entrou na B7 e
  `cancelar(data, motivo)` fica para B8.

### Limites

- `precoUnitario` ainda e aceito no request por compatibilidade. A B7 usa o
  `ProdutoRepository` para localizar o produto e baixar o estoque ao finalizar;
  congelar o preco corrente do catalogo na criacao do item continua pendente.
- `produto_id` continua sem FK em `itens_venda`; a existencia do produto e
  validada durante a finalizacao da venda.
- Cancelamento foi entregue na B8; comprovante fica para B9.

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
  `calcularValorFinal` publicos foram entregues na B6.

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

---

## B7 — Finalizacao da venda e baixa de estoque (entregue nesta branch)

Finaliza uma venda valida e baixa, na mesma transacao, o estoque dos produtos
vendidos por meio da integracao I1.

| Camada | Arquivo |
|---|---|
| Dominio | `venda/Venda.java` (`validarFinalizacao`, `finalizar`) |
| Caso de uso | `aplicacao/ServicoVenda.java` |
| Porta e adaptador | `aplicacao/EstoqueDaVenda.java`, `aplicacao/AdaptadorEstoqueDaVenda.java` |
| Catalogo | `catalogo/Produto.java`, `catalogo/ProdutoRepository.java` |
| Estoque | `estoque/ItemEstoque.java`, `estoque/ServicoEstoqueJpa.java` |
| API REST | `venda/VendaController.java` |
| Migration de catalogo | `resources/db/migration/V100__cria_catalogo_e_estoque.sql` |

### Endpoint

| Metodo | Rota | Sucesso | Erros |
|---|---|---|---|
| POST | `/api/vendas/{id}/finalizar` | 200 + venda concluida | 404, 409, 422, 503 |

### Regras

- Exige venda `EM_ABERTO`, pelo menos um item e forma de pagamento definida.
- Produto inexistente retorna 404; produto desativado ou saldo insuficiente impede
  a finalizacao. O estoque e validado e baixado antes de marcar a venda como
  `CONCLUIDA`, na mesma transacao.
- Repetir a finalizacao ou tentar finalizar uma venda cancelada gera conflito de
  transicao (409).
- A migration `V100` cria as tabelas `categorias` e `produtos`, mantendo a faixa
  `V1xx` da Trilha A. A movimentacao gerada pelo dominio ainda e mantida em
  memoria; persistir seu historico e trabalho da A3.

### Proximos passos

- **B8**: cancelamento e estorno de estoque foram entregues na proxima secao.
- **B9**: gerar comprovante da venda.
- O request de adicionar item ainda recebe `precoUnitario`; buscar e congelar o
  preco do produto no servidor permanece uma integracao pendente.

---

## B8 — Cancelamento da venda e estorno de estoque (entregue nesta branch)

Cancela uma venda concluida, registra data e motivo e devolve ao estoque os
produtos vendidos pela integracao I5, dentro da mesma transacao.

| Camada | Arquivo |
|---|---|
| Dominio | `venda/Venda.java` (`validarCancelamento`, `cancelar`) |
| Caso de uso | `aplicacao/ServicoVenda.java` |
| Porta e adaptador | `aplicacao/EstoqueDaVenda.java`, `aplicacao/AdaptadorEstoqueDaVenda.java` |
| DTO | `venda/dto/CancelarVendaRequest.java` |
| API REST | `venda/VendaController.java`, `venda/dto/VendaResponse.java` |
| Migration | `resources/db/migration/V205__adiciona_cancelamento_venda.sql` |

### Endpoint

| Metodo | Rota | Sucesso | Erros |
|---|---|---|---|
| POST | `/api/vendas/{id}/cancelar` | 200 + venda cancelada | 400, 404, 409, 422, 503 |

Corpo:

```json
{"data":"2026-11-06T10:30:00","motivo":"cliente desistiu"}
```

### Regras

- So uma venda `CONCLUIDA` pode ser cancelada. Venda aberta ou ja cancelada
  retorna **409**, sem estornar estoque novamente.
- Data e motivo sao obrigatorios; motivo aceita ate 500 caracteres. O resultado
  guarda `canceladaEm` e `motivoCancelamento` na venda e os expoe na resposta.
- O adaptador bloqueia os produtos, estorna todas as quantidades da venda e so
  entao registra a transicao para `CANCELADA`. Falhas na integracao revertem a
  operacao pela transacao.
- O estorno nao exige que o produto continue ativo; ele apenas devolve o saldo.

O teste de integracao cobre o ciclo finalizar/cancelar, a reposicao do saldo e a
protecao contra estorno duplicado.

---

## B9 — Comprovante textual da venda (entregue nesta branch)

Gera um comprovante em texto simples a partir dos dados congelados da venda.

| Camada | Arquivo |
|---|---|
| Dominio | `venda/Venda.java` (`gerarComprovante`) |
| Caso de uso | `aplicacao/ServicoVenda.java` |
| API REST | `venda/VendaController.java` |

### Endpoint

| Metodo | Rota | Sucesso | Erros |
|---|---|---|---|
| GET | `/api/vendas/{id}/comprovante` | 200 + `text/plain` | 404 |

O texto contem identificador e data da venda, cliente ou venda de balcao,
situacao, itens com produto/quantidade/preco unitario/subtotal, subtotal,
desconto, valor final e forma de pagamento. Para vendas canceladas, tambem
mostra data e motivo do cancelamento. Valores monetarios usam formato brasileiro.

O endpoint pode ser consultado em qualquer situacao: vendas em aberto aparecem
com o estado atual e a forma de pagamento indicada como nao definida.

---

## B10 — Testes das regras críticas de venda (entregue nesta branch)

Amplia a verificacao das regras de venda vazia, desconto acima do subtotal e
repeticao de finalizacao/cancelamento.

| Suite | Arquivo |
|---|---|
| Dominio | `src/test/java/br/com/atelie/venda/VendaTest.java` |
| API e estoque | `src/test/java/br/com/atelie/venda/VendaControllerIntegrationTest.java` |

Os testes de integracao garantem que venda vazia nao muda de estado, desconto
maior que o subtotal nao e aplicado, finalizar novamente nao baixa estoque outra
vez e cancelar novamente nao estorna estoque outra vez. Os testes de dominio
tambem cobrem as respectivas regras e transicoes invalidas.

---

## B11 — Histórico de vendas e compras de clientes (entregue nesta branch)

Disponibiliza consultas dedicadas para o historico completo de vendas e para as
vendas associadas a um cliente.

| Camada | Arquivo |
|---|---|
| Persistencia | `venda/VendaRepository.java` |
| Caso de uso | `aplicacao/ServicoVenda.java` |
| API REST | `venda/VendaController.java` |

### Endpoints

| Metodo | Rota | Sucesso | Erros |
|---|---|---|---|
| GET | `/api/vendas/historico` | 200 + vendas | — |
| GET | `/api/vendas/historico/clientes/{clienteId}` | 200 + vendas do cliente | 404 |

Os resultados incluem itens e totais e vêm da venda mais recente para a mais
antiga. O historico geral inclui todos os estados (aberta, concluida ou
cancelada); cada resposta informa a situacao. O historico de compras filtra pelo
`clienteId`, exclui vendas de balcao sem cliente e retorna 404 quando o cliente
nao existe.

O proximo item da Trilha B e B12 (relatorios de vendas).
