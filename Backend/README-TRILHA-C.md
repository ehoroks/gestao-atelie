# Trilha C — Encomendas, precificacao e infraestrutura

## Como executar

1. Instale **JDK 21** e **Maven 3.9+** (no IntelliJ IDEA ou no VSCode com extensoes Java).
2. Abra a pasta `Backend` como projeto Maven.
3. Rode `mvn test` para os testes unitarios/H2 sem Docker; `mvn verify` tambem executa os ITs PostgreSQL com Testcontainers e requer Docker.
4. Para subir a aplicacao no Spring Boot, defina `DB_JDBC_URL`, `DB_USERNAME` e `DB_PASSWORD`, em variaveis de ambiente, e rode `mvn spring-boot:run`.
5. No Supabase, obtenha a *connection string JDBC* do Postgres/pooler, incluindo `sslmode=require`, e mantenha as credenciais fora do Git.

## PostgreSQL / Supabase

As classes de composicao de preco da Trilha C continuam como dominio Java; a persistencia e a API de encomendas foram integradas pela Trilha D usando as entidades existentes de Cliente, Produto e Insumo. A migration `V401__cria_encomendas.sql` cria encomendas e linhas de insumo no schema atual, usando chaves estrangeiras para as tabelas de A/B.

## Divisao e integracoes

- **C1**: projeto Maven/Spring Boot, porta `Repository<T>`, implementacao fake em memoria, fixtures de Produto/Insumo e pipeline de testes.
- **C2-C5**: `ItemComposicao` e `ComposicaoPreco` no pacote `catalogo` para acessar o metodo `Produto.definirPreco()` (visibilidade de pacote) sem modificar o contrato da Trilha A.
- **C4 — Formula definida**: `preco = materiais * (1 + custos_indiretos/100) * (1 + margem/100)`. Aqui **margem** e um *markup sobre custo*, nao a margem percentual sobre preco de venda. Validar essa escolha com a cliente/professor.
- **C6-C7**: `Encomenda` valida a maquina de estados ja definida em `SituacaoEncomenda`. `ENTREGUE` e `CANCELADA` sao terminais.
- **C8/I2**: `ServicoEncomenda` coordena `ServicoEstoque.baixar()` com a transicao persistida para `EM_PRODUCAO`; a operacao e atomica na mesma transacao.
- **I3**: preco aplicado atraves de `ComposicaoPreco.aplicarComoPrecoDo()`, chamando o metodo de pacote `Produto.definirPreco()`.
- **I4**: `Encomenda.cliente` usa a entidade JPA `Cliente` da Trilha B; nao ha uma segunda classe de cliente.
- **I1/I5**: de responsabilidade da Trilha B, nao implementados nesta entrega.

## Limites desta etapa

`ServicoEncomenda` e um bean ativo no backend Spring Boot e usa o servico de estoque persistente. A API e as migrations de encomenda estao documentadas em `README-TRILHA-D.md`. A associacao Produto-encomenda continua obrigatoria para preservar o contrato C atual; tornar Produto opcional permanece uma decisao de negocio pendente. O Vue.js e a interface futura; a Trilha C e de backend e nao adiciona componentes de frontend.

A composicao descreve os materiais de **uma unidade de produto**; para encomendas de varias unidades, adicionar quantidade encomendada ao contrato de Encomenda e multiplicar as linhas antes da baixa.

## Cuidados de integracao

- `ServicoEncomenda` e `ServicoEstoque` usam `@Transactional`; uma falha de saldo reverte a mudanca de situacao e as baixas dentro do banco.
- Cancelar uma encomenda apos iniciar producao **nao** repoe automaticamente insumos; isso depende de decisao de negocio.
- `Repository<T>` e `RepositorioEmMemoria` continuam uteis para testes de dominio, mas nao substituem os repositorios Spring Data usados nos fluxos persistidos.
