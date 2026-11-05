# Entrega para integrar no repositorio gestao-atelie

Fonte: branch `develop`, pasta `Backend`, em 04/11/2026.
Este arquivo ZIP e **sobreposicao**: contem somente arquivos NOVOS da Trilha C.
Nao substitui os codigos ja existentes das Trilhas A e B.

## Aplicacao

1. `git clone https://github.com/ehoroks/gestao-atelie.git`
2. `cd gestao-atelie && git checkout develop`
3. Crie uma branch: `git checkout -b feat/trilha-c`
4. Extraia o ZIP **na raiz do repositorio** (mesmo nivel do README.md), preservando pastas.
5. Abra a pasta `Backend` no IntelliJ IDEA ou VSCode.
6. Com JDK 21 e Maven 3.9+, execute `mvn clean verify` dentro da pasta `Backend`.
7. Revise a formula do preco e alinhe as integracoes I2, I3, I4 antes de mesclar a branch.

## Correspondencia C1-C9

- C1: Spring Boot/Maven, estrutura de camadas, `Repository<T>`, `RepositorioEmMemoria`, fakes, CI, configuracao Supabase/Postgres.
- C2: `ItemComposicao.calcularCusto`.
- C3: `ComposicaoPreco.adicionarMaterial` e `calcularCustoMateriais`.
- C4: `calcularPrecoSugerido` pela formula de markup documentada.
- C5: `ajustarPreco` e `aplicarComoPrecoDo` usando acesso de pacote existente em Produto.
- C6/C7: `Encomenda` com maquina de estados e `estaPendente`.
- C8: `ServicoEncomenda` baixa insumos ao iniciar producao por `ServicoEstoque`.
- C9: 19 metodos de testes em cinco classes JUnit 5 (com fakes em teste).

## Status da verificacao

Compilacao local com `javac` JDK 21: PASSOU usando contratos de dependencia equivalentes
as classes existentes em `develop` e stubs da API JUnit/Spring (ambiente sem Maven).
Execucao de 19 metodos de teste por harness local: 19 PASSARAM, 0 FALHARAM.
**Nao foi possivel executar `mvn verify` real nem subir Spring/Supabase** neste ambiente.
O GitHub retornou 403 ao tentar criar branch remotamente, logo o repositorio remoto nao foi alterado.
