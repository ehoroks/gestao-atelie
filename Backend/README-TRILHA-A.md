# Trilha A — Catálogo e estoque (Cirinha Ateliê)

Implementação complementar ao backend **já existente** em `Backend`, alinhada aos requisitos **RF02–RF11 e RF28**. Não substitui entidades, migrations ou código das Trilhas B/C/D.

## Stack

- Java 21 + Spring Boot 4.1.2 (usa o `Backend/pom.xml` já existente);
- Spring Data JPA, PostgreSQL (local ou Supabase) e Flyway;
- JUnit 5 / Mockito, MockMvc e Testcontainers com PostgreSQL;
- Vue 3 + Vite no diretório `Frontend/`.

O banco **já possui** as migrations `V100__cria_catalogo_e_estoque.sql` e `V101__estoque_insumos_e_movimentacoes.sql`. **Não adicione** outro DDL nem rode `Backend/sql/trilha-c.sql` manualmente: Flyway administra o schema existente.

## Endpoints

### Categorias (RF07)

| Método | Caminho | Ação |
| --- | --- | --- |
| GET | `/api/categorias` | Lista por nome |
| GET | `/api/categorias/{id}` | Consulta |
| POST | `/api/categorias` | Cadastra |
| PUT | `/api/categorias/{id}` | Renomeia |
| DELETE | `/api/categorias/{id}` | Exclui se não houver produtos vinculados |

Exemplo: `POST /api/categorias` com `{ "nome": "Vestidos" }`.

### Produtos (RF02 a RF06)

| Método | Caminho | Ação |
| --- | --- | --- |
| POST | `/api/produtos` | Cadastro sem estoque inicial (use entrada para adicionar saldo) |
| GET | `/api/produtos` | Consulta e filtros opcionais `nome`, `categoriaId`, `ativo`, `cor`, `tamanho`, `precoMin`, `precoMax` |
| GET | `/api/produtos/{id}` | Consulta individual |
| PUT | `/api/produtos/{id}` | Atualiza nome/categoria/preço/atributos |
| PATCH | `/api/produtos/{id}/desativar` | Desativa sem apagar histórico |

Exemplo de criação:

```json
{
  "nome": "Vestido artesanal",
  "unidadeMedida": "UN",
  "estoqueMinimo": 2,
  "categoriaId": 1,
  "preco": 159.90,
  "tamanho": "M",
  "cor": "Areia",
  "descricao": "Peça feita à mão",
  "foto": "https://exemplo.com/foto.jpg"
}
```

A edição usa a mesma estrutura **sem** `unidadeMedida` e `estoqueMinimo`, pois essas propriedades são definidas no cadastro conforme o domínio atual. A foto é uma URL; upload no Supabase Storage pode ser integrado posteriormente, usando autenticação adequada.

### Insumos (apoio ao RF08/RF09 e precificação)

| Método | Caminho | Ação |
| --- | --- | --- |
| POST | `/api/insumos` | Cadastra insumo |
| GET | `/api/insumos` | Lista, aceita `nome` opcional |
| GET | `/api/insumos/{id}` | Consulta |
| PATCH | `/api/insumos/{id}/custo` | Atualiza custo unitário |

Exemplo: `{ "nome":"Tecido algodão", "unidadeMedida":"M", "estoqueMinimo":5, "custoUnitario":18.50 }`.

### Estoque e histórico (RF08–RF11, RF28)

| Método | Caminho | Ação |
| --- | --- | --- |
| GET | `/api/estoque` | Lista todos; filtros `tipo=PRODUTO/INSUMO`, `baixo=true/false`, `nome` |
| GET | `/api/estoque/baixo` | Lista itens com saldo menor ou igual ao estoque mínimo |
| GET | `/api/estoque/{id}` | Consulta saldo individual |
| GET | `/api/estoque/{id}/movimentacoes` | Consulta o histórico persistido |
| POST | `/api/estoque/{id}/entradas` | Aumenta saldo e registra entrada |
| POST | `/api/estoque/{id}/saidas` | Baixa saldo e registra saída |
| POST | `/api/estoque/{id}/ajustes` | Define saldo absoluto e registra ajuste com motivo |

Corpo de entrada/saída: `{ "quantidade": 2, "motivo": "Reposição" }`.
Corpo de ajuste: `{ "novaQuantidade": 8, "motivo": "Contagem física" }`.

- Quantidades negativas ou zero são rejeitadas em entradas/saídas; ajuste aceita zero.
- `UN` aceita apenas quantidades inteiras; `M` e `G` aceitam frações.
- Baixa acima do saldo retorna `409` sem alterar o banco.
- Todos os movimentos são gravados na mesma transação do saldo e com bloqueio pessimista, para evitar conflitos simultâneos.
- O sistema considera estoque baixo quando `quantidade <= estoqueMinimo`, conforme regra existente em `ItemEstoque`.
- As respostas seguem o `TratadorGlobalDeExcecoes` já existente: 400 payload inválido, 404 não encontrado, 409 conflito, 422 regra de negócio.

## Executar

No IntelliJ IDEA, importe a pasta `Backend` como projeto Maven. No VSCode, use Java Extension Pack e Maven for Java.

```bash
cd Backend
# Conectar localmente ao Postgres (exemplo)
export DB_JDBC_URL='jdbc:postgresql://localhost:5432/cirinha_atelie'
export DB_USERNAME='postgres'
export DB_PASSWORD='SUA_SENHA'
mvn spring-boot:run
```

Para usar Supabase, substitua `DB_JDBC_URL` pela URI JDBC do PostgreSQL/pooler da instância com `sslmode=require`. Não exponha as credenciais no Git nem no Vue.

```bash
# JUnit unitário / H2, sem Docker
mvn test
# JUnit integração real PostgreSQL + Testcontainers, exige Docker
mvn verify
```

O arquivo `.github/workflows/backend-tests.yml` já executa `mvn verify` nos pushes/pull requests que alterem `Backend/**`.

## Integração Vue.js

```bash
cd Frontend
npm install
npm run dev
```

Abra http://localhost:5173. O Vite encaminha `/api` para `http://localhost:8080`. Em produção, use um proxy do mesmo domínio ou configure a política CORS de forma restrita. A URL no `Frontend/.env.example` é opcional.

## Atenção à segurança e às dependências

O projeto existente realiza login por senha BCrypt mas **ainda não emite token nem protege endpoints**; antes de colocar o sistema em produção, a equipe deve implementar autorização (Spring Security/JWT/sessão) para **todos** os módulos, inclusive a Trilha A.

A Trilha B ainda aceita `precoUnitario` enviado pelo cliente ao adicionar item de venda. Essa correção de integração não é parte deste pacote e deve ser coordenada com a equipe da Trilha B. O frontend aqui entrega as áreas de **Produtos, Categorias, Insumos e Estoque**; não substitui o futuro frontend das Trilhas B/C/D.

## Limitações conhecidas

- Filtros de produtos usam `findAll()` e filtragem na aplicação, compatível com catálogos pequenos; para uma base grande, migrar para paginação/consulta no banco.
- Não há edição da unidade/estoque mínimo após cadastro, nem reativação de produtos. A decisão deve ser alinhada com os contratos do domínio e a cliente.
- Alertas de estoque baixo aparecem na interface e no endpoint; não há notificações push/email.
- Upload de fotos no Supabase Storage fica para uma integração separada, com segurança e políticas de acesso.
- Os testes JUnit foram escritos, mas não puderam ser executados neste ambiente sem o Maven/dependências; executar `mvn verify` após aplicar no repositório.
