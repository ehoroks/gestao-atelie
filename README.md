# Cirinha Ateliê

Sistema web para gestão de um pequeno ateliê que produz e vende peças de roupa feitas à mão. O objetivo é substituir os controles manuais por uma ferramenta simples, organizada e fácil de usar.

## Funcionalidades

- Autenticação da proprietária
- Cadastro, edição, desativação, consulta e filtro de produtos e categorias
- Controle de estoque de produtos e insumos (entradas, ajustes, alerta de estoque baixo e histórico de movimentações)
- Registro de vendas, com desconto, forma de pagamento, cancelamento e comprovante
- Cadastro de clientes e histórico de compras e encomendas
- Registro e acompanhamento de encomendas
- Painel inicial com vendas, faturamento, estoque baixo e encomendas pendentes
- Relatórios de vendas, faturamento e produtos mais vendidos
- Cálculo do preço sugerido das peças (materiais, custos indiretos e margem de lucro)

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Frontend | Vue.js |
| Backend | Java com Spring Boot (API REST) |
| Banco de dados | PostgreSQL |

## Documentação

### Trilhas

| Trilha | Documento | Escopo |
|---|---|---|
| A | [`Backend/README-TRILHA-A.md`](Backend/README-TRILHA-A.md) | Catálogo, insumos e estoque (RF02–RF11, RF28) |
| B | [`Backend/README-TRILHA-B.md`](Backend/README-TRILHA-B.md) | Usuários, clientes, vendas, descontos, comprovantes e relatórios (B1–B12) |
| C | [`Backend/README-TRILHA-C.md`](Backend/README-TRILHA-C.md) | Encomendas, composição de preço e regras de negócio |
| D | [`Backend/README-TRILHA-D.md`](Backend/README-TRILHA-D.md) | Integração das trilhas A/B/C no backend atual |
| E | [`Backend/README-TRILHA-E.md`](Backend/README-TRILHA-E.md) | PostgreSQL no Supabase, Flyway, backup e keep-alive |
| F | [`Frontend/README-TRILHA-F.md`](Frontend/README-TRILHA-F.md) | Integração do Vue com a API, CORS e build do frontend |
| H | [`Backend/README-TRILHA-H.md`](Backend/README-TRILHA-H.md) | JWT, perfis ADMIN/ATENDENTE e administrador inicial |

Não há documentação de uma Trilha G neste repositório.

Na pasta **Documentos**, estão disponíveis os arquivos referentes às **Entregas 1 e 2**, os **diagramas de atividade e de classes**, além do **feedback da validação realizada com a cliente Cirinha**.

Logo abaixo, encontra-se o link para a pasta do **Google Drive**, que contém todos os diagramas e a comprovação, em áudio, da validação realizada com a cliente.

Link do protótipo no figma: `https://www.figma.com/proto/G11JyDS2Je7UZN1CiwnstD/Untitled?node-id=0-1&t=FXsMce8kGfhSbJDq-1`

Link: `https://drive.google.com/drive/folders/1M-Qgs_vqO21xj2suu5uTPPz3KIr8-BeN?usp=sharing`

## Equipe

- Edonisio
- Gustavo
- André Felipe
- Felipe Araujo
