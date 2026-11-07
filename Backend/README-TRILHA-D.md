# Trilha D — Integração no backend atual

A Trilha D foi portada para a aplicação existente `br.com.atelie`, reutilizando
as entidades de Cliente, Produto, Insumo e estoque das Trilhas A/B. O ZIP
original permanece em `agents/` como referência e não é usado como uma segunda
aplicação Maven.

## Entregas

| Atividade | Estado nesta integração |
|---|---|
| D1 — projeto base | `pom.xml`, `application.yml` por perfil, Flyway e `docker-compose.yml` para o PostgreSQL local. |
| D2 — tratamento global de erros | Integrado ao `TratadorGlobalDeExcecoes` e ao contrato `ErroResposta`; conflitos de versão retornam 409. |
| D3 — migrations | Reutiliza as migrations A/B; D cria encomendas na faixa V4xx. |
| D4 — clientes | Reutiliza Cliente/CRUD da B2; exclusão de cliente com encomendas é bloqueada. |
| D5 — persistência | `Encomenda` e `EncomendaInsumo` usam JPA, versão otimista e máquina de estados. |
| D6 — consumo de insumos | A entrada em `EM_PRODUCAO` baixa todos os insumos em uma transação e registra movimentos. Cancelar depois da produção estorna o que já foi baixado. |
| D7 — API | Endpoints REST de encomenda com DTOs validados. |
| D8 — integração | Testes `*IT` exercitam transições, rollback de estoque e migrations em PostgreSQL real. |
| D9 — infraestrutura de teste | `IntegrationTestBase`, PostgreSQL Testcontainers e builders compartilhados. |

## API de encomendas

| Metodo | Rota | Descricao |
|---|---|---|
| POST | `/api/encomendas` | Cria encomenda e linhas de insumo. |
| GET | `/api/encomendas?situacao=&pendentes=` | Lista e filtra encomendas. |
| GET | `/api/encomendas/{id}` | Consulta encomenda, cliente, produto e insumos. |
| PATCH | `/api/encomendas/{id}/situacao` | Aplica transicao de estado. |

### Contrato preservado

- A encomenda requer cliente e data prevista. O produto é opcional para permitir
  peças sob medida; se houver produto e o valor for omitido, copia-se
  `Produto.preco` no momento da criação. Sem produto, o valor é obrigatório.
  Assim a encomenda mantém seu valor próprio mesmo que o preço do catálogo mude.
- Cliente usa o modelo e a tabela `clientes` da B2, sem duplicar a entidade ou
  alterar a validação de telefone.
- A baixa usa o contrato existente `ServicoEstoque.baixar(List<Linha>, motivo)`;
  a quantidade e as movimentações são persistidas junto com a transição.
- Exceções continuam retornando o formato existente `ErroResposta`. Conflitos
  otimistas são mapeados para HTTP 409.

## Pré-requisito dos testes

`mvn test` roda os testes unitários e os de H2 — não precisa de Docker.

`mvn verify` roda também os `*IT`, que sobem um PostgreSQL real via Testcontainers
e **exigem um Docker em execução**. Sem Docker, a subida do container falha com a
mensagem explicando o que fazer. Para pular os `*IT` sem rodar o Docker:

```
mvn verify -DskipITs
```

O `docker-compose.yml` é para rodar a aplicação localmente contra um PostgreSQL;
os testes não o utilizam, o Testcontainers cria o próprio banco.

## Banco e testes

- `V101__estoque_insumos_e_movimentacoes.sql` migra os produtos existentes para
  uma raiz de estoque compartilhada, preservando seus IDs, e adiciona insumos e
  movimentações persistidas.
- `V401__cria_encomendas.sql` cria encomendas e linhas de insumo ligadas a
  `clientes`, `produtos` e `insumos` existentes.
- `mvn test` executa os testes unitários e testes locais H2.
- `mvn verify` também executa os testes `*IT` usando PostgreSQL Testcontainers;
  requer Docker disponível no ambiente.
- As classes `*IT` levam `@Tag("integration")`, para permitir filtrar por grupo.

Os testes de integração herdam de `br.com.atelie.infra.teste.IntegrationTestBase`,
que sobe um PostgreSQL por execução, aplica Flyway e limpa os dados antes de
cada teste.
