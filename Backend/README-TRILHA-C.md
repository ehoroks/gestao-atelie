# Trilha C — Encomendas, precificacao e infraestrutura

## Como executar

1. Instale **JDK 21** e **Maven 3.9+** (no IntelliJ IDEA ou no VSCode com extensoes Java).
2. Abra a pasta `Backend` como projeto Maven.
3. Rode `mvn clean verify` para executar os testes JUnit 5 (sem banco de dados).
4. Para subir a aplicacao no Spring Boot, defina `DB_JDBC_URL`, `DB_USERNAME` e `DB_PASSWORD`, em variaveis de ambiente, e rode `mvn spring-boot:run`.
5. No Supabase, obtenha a *connection string JDBC* do Postgres/pooler, incluindo `sslmode=require`, e mantenha as credenciais fora do Git.

## PostgreSQL / Supabase

O arquivo `sql/trilha-c.sql` contem o **DDL preliminar** das tabelas proprias da Trilha C (`atelie.encomendas`, `atelie.composicoes_preco`, `atelie.itens_composicao`). A execucao e **manual e adiada** ate o alinhamento com A/B; nao ha chaves estrangeiras para entidades ainda ausentes nem repositorios JPA. O script usa um schema proprio, evitando exposicao automatica no schema `public` do Supabase.

## Divisao e integracoes

- **C1**: projeto Maven/Spring Boot, porta `Repository<T>`, implementacao fake em memoria, fixtures de Produto/Insumo e pipeline de testes.
- **C2-C5**: `ItemComposicao` e `ComposicaoPreco` no pacote `catalogo` para acessar o metodo `Produto.definirPreco()` (visibilidade de pacote) sem modificar o contrato da Trilha A.
- **C4 — Formula definida**: `preco = materiais * (1 + custos_indiretos/100) * (1 + margem/100)`. Aqui **margem** e um *markup sobre custo*, nao a margem percentual sobre preco de venda. Validar essa escolha com a cliente/professor.
- **C6-C7**: `Encomenda` valida a maquina de estados ja definida em `SituacaoEncomenda`. `ENTREGUE` e `CANCELADA` sao terminais.
- **C8/I2**: `ServicoEncomenda` chama `ServicoEstoque.baixar()` antes da mudanca de estado. A implementação real desse servico, que deve assegurar atomicidade, pertence a Trilha A.
- **I3**: preco aplicado atraves de `ComposicaoPreco.aplicarComoPrecoDo()`, chamando o metodo de pacote `Produto.definirPreco()`.
- **I4**: `Encomenda.clienteId` e a fronteira temporaria para a entidade `Cliente` da Trilha B, ainda ausente. Trocar pela associacao/JPA quando o contrato de `Cliente` estiver pronto, **sem duplicar a classe**.
- **I1/I5**: de responsabilidade da Trilha B, nao implementados nesta entrega.

## Limites desta etapa

O `ServicoEncomenda` so vira bean Spring quando o perfil `estoque-integrado` e ativado e existe uma implementacao de `ServicoEstoque` da Trilha A. Ativar com `SPRING_PROFILES_ACTIVE=estoque-integrado` depois dessa integracao.

O codigo atualmente na branch develop ainda nao possui mapeamentos JPA, controllers, migrations nem o servico de estoque concreto. O datasource para PostgreSQL/Supabase esta **configurado**, mas a persistencia SQL ainda depende da conclusao e integracao das Trilhas A e B. O Vue.js e a interface futura; a Trilha C e de backend e nao adiciona componentes de frontend.

A composicao descreve os materiais de **uma unidade de produto**; para encomendas de varias unidades, adicionar quantidade encomendada ao contrato de Encomenda e multiplicar as linhas antes da baixa.

## Cuidados de integracao

- A prevalidacao da transicao ocorre antes da baixa. A baixa de varios insumos deve ser **tudo ou nada** no ServicoEstoque da Trilha A, e numa transacao de banco quando a persistencia existir.
- `ServicoEncomenda` usa `@Transactional`, mas uma entidade alterada somente em memoria nao pode ser revertida por transacao JDBC. Quando as entidades JPA forem implementadas, carregar/salvar a encomenda dentro da mesma transacao do estoque.
- Cancelar uma encomenda apos iniciar producao **nao** repoe automaticamente insumos; isso depende de decisao de negocio.
- Nao confundir `Repository<T>` com um repositorio JPA pronto. O fake nao oferece durabilidade nem isolamento transacional.
