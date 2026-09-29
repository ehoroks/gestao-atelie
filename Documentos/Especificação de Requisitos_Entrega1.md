## PROJETO ATELIÊ - DOCUMENTO DE ESPECIFICAÇÃO DE REQUISITOS


Projeto Ateliê

## ÍNDICE

2 / 20


## 1 HISTÓRICO

| Data | Versão Responsável | Alteração |
| --- | --- | --- |
| 12/09/2026 1.0 | Edonisio, | - Requisitos Funcionais |
|   | Gustavo, | - Requisitos Não Funcionais |
|   | André Felipe, | - Descrição Geral Do Sistema |
|   |   | - Público alvo |
|   |   | - Objetivos |
|   |   | - Técnicas de elicitação |
| 14/09/2026 1,1 | Edonisio, | - Historias de usuario |
|   | André Felipe, | - Transcrição da entrevista gravada |
|   | Gustavo, | - Revisão a partir da entrevista com |
|   |   | cliente |
|   | Felipe Araujo |   |

## 2 INTRODUÇÃO

## 2.1 Objetivos

Este documento especifica os requisitos do sistema Cirinha Ateliê, fornecendo aos

desenvolvedores as informações necessárias para o projeto e implementação, assim como para a realização dos testes e homologação do sistema.

## 2.2 Público alvo deste documento

Pessoa idosa com necessidade de realizar o controle do próprio negócio, com

baixa experiência no uso de tecnologias atuais e dificuldade de enxergar.

## 2.3 Prioridade dos requisitos

- a. Essencial: são os requisitos indispensáveis de implementação, pois o sistema não deve ser implantado ou estar disponível com a ausência destes requisitos.

- b. Importante: são os requisitos que devem ser implementados, a ausência destes não torna o uso do sistema satisfatório, contudo pode haver a implantação.

- c. Desejável: são os requisitos que não afetam as funcionalidades básicas do sistema, ou seja, a aplicação funciona de maneira satisfatória.

## 2.4 Glossário

| Verbete | Definição |
| --- | --- |


| RF | Requisitos Funcionais |
| --- | --- |
| RNF | Requisitos Não Funcionais |
| HU | História de Usuário |
| Insumo | Matéria-prima utilizada na produção das peças (tecido, linha, aviamentos) |

## 2.5 Referências

[1] Wiegers, K. E. Software Requirements. 2 ed. Estados Unidos da América, 2003.

## 3 DESCRIÇÃO GERAL DO SISTEMA

O Cirinha Ateliê será um sistema destinado ao gerenciamento de um pequeno

ateliê que produz e comercializa peças de roupas feitas à mão.

O sistema permitirá controlar:

- cadastro das peças;

- categorias de produtos;

- estoque de produtos e insumos;

- entrada e saída de produtos e insumos;

- clientes;

- vendas;

- encomendas;

- formas de pagamento;

- descontos;

- histórico de movimentações;

- relatórios de vendas;

- faturamento;

- estoque baixo;

- produtos mais vendidos;

- cálculo de preço sugerido das peças

- backup das informações.

O principal objetivo é substituir controles manuais por um sistema simples,

organizado e de fácil utilização.

## 3.1 Plataforma

Conforme identificado na entrevista realizada com a cliente (Seção 4.1), a

plataforma definida para o sistema é um aplicativo mobile, por ser de uso mais prático no dia a dia do ateliê.

## 3.2 Itens fora do escopo

Durante a entrevista, a cliente mencionou outras necessidades que não fazem

parte do domínio do sistema Cirinha Ateliê e que, portanto, não serão contempladas por este projeto:


- Criação de um site ou loja virtual para venda das peças de brechó — optou-se por manter essa divulgação em redes sociais (como o Instagram), fora do sistema, devido ao custo mensal de manutenção de um site próprio;

- Geração de apostilas/e-books de receitas culinárias para venda;

- Gestão de um novo negócio de bordados, projeto pessoal futuro da cliente que envolve a aquisição de uma máquina de bordar.

Esses itens foram registrados para documentar o processo de elicitação, mas não

serão implementados como parte deste sistema.

## 4 TÉCNICAS DE ELICITAÇÃO DE REQUISITOS

## 4.1 Entrevista semiestruturada

Foi realizada uma entrevista diretamente com a proprietária do ateliê. A entrevista

teve perguntas previamente definidas, mas permitiu que novos assuntos fossem explorados durante a conversa.

Exemplos de perguntas:

- Como você controla atualmente as peças produzidas?

- Como sabe quantas peças ainda estão disponíveis?

- Como registra uma venda?

- Você registra o nome dos clientes?

- Quais informações precisa guardar sobre cada peça?

- Como controla as encomendas?

- Como sabe quanto vendeu durante um mês?

- Você precisa saber quais peças vendem mais?

- Existem situações em que uma venda precisa ser cancelada?

- Quais informações gostaria de encontrar rapidamente?

- Você costuma aplicar descontos?

- Quais formas de pagamento aceita?

- Quais dificuldades encontra no controle atual?

- Como você calcula o preço de venda de uma peça nova?

## Objetivo da técnica

## Identificar:

- necessidades da cliente;

- funcionalidades esperadas;

- problemas do processo atual;

- regras do negócio;

- informações que devem ser armazenadas.

## 4.2 Observação direta

Foi realizada a observação de como a proprietária executa atualmente suas

atividades no ateliê. Foram observadas atividades como:


Serão observadas atividades como:

- produção de uma nova peça;

- organização das peças;

- consulta das roupas disponíveis;

- atendimento de clientes;

- realização de vendas;

- anotação dos valores recebidos;

- controle de encomendas;

- conferência do estoque.

## Objetivo da técnica

Identificar processos que a cliente realiza diariamente e que podem não ser

lembrados durante uma entrevista. A observação também permite identificar dificuldades e tarefas repetitivas que podem ser simplificadas pelo sistema.

## 5 REQUISITOS DE NEGÓCIO

<Aqui são descritos os requisitos que refletem os objetivos de negócio de alto nível

da organização que solicitou o desenvolvimento do sistema>

## 6 REQUISITOS DE USUÁRIO

<Aqui são descritos os requisitos relacionados aos objetivos e tarefas que os perfis

de usuários devem ser capazes de executar no sistema>

## 7 REQUISITOS FUNCIONAIS

## RF01 — Autenticar usuário

autenticação.

O sistema deve permitir que a proprietária acesse o sistema por meio de

## RF02 — Cadastrar produto

O sistema deve permitir cadastrar uma nova peça de roupa. O cadastro deve

permitir informar:

- nome da peça;

- categoria;

- tamanho;

- cor;

- preço;

- quantidade;

- descrição;

- foto.

## RF03 — Editar produto

O sistema deve permitir alterar as informações de uma peça cadastrada.


## RF04 — Desativar produto

O sistema deve permitir desativar produtos que não são mais produzidos ou

comercializados, preservando o histórico das vendas anteriores.

## RF05 — Consultar produtos

O sistema deve permitir consultar os produtos cadastrados.

## RF06 — Pesquisar e filtrar produtos

O sistema deve permitir pesquisar produtos utilizando informações como:

- nome;

- categoria;

- tamanho;

- disponibilidade.

## RF07 — Cadastrar categorias

O sistema deve permitir cadastrar categorias de roupas, por exemplo:

- vestido;

- blusa;

- saia;

- conjunto;

- infantil;

- acessórios.

## RF08 — Controlar estoque

O sistema deve armazenar a quantidade disponível de cada produto e de cada

insumo (matéria-prima).

O controle deve contemplar tanto peças prontas quanto materiais utilizados na

produção, como tecido, linha e aviamentos, permitindo o registro em diferentes unidades de medida (unidade, metro, grama).

## RF09 — Registrar entrada de produto ou insumo

O sistema deve permitir registrar a entrada de novas peças produzidas e de novos

insumos adquiridos (tecido, linha, aviamentos) no estoque, informando a quantidade e a unidade de medida correspondente.

## RF10 — Registrar ajuste de estoque

O sistema deve permitir corrigir manualmente a quantidade de um produto ou

insumo em situações de erro, perda ou avaria.

## RF11 — Alertar estoque baixo

O sistema deve informar quando um produto ou insumo atingir uma quantidade

mínima previamente definida.


## RF12 — Registrar venda

O sistema deve permitir registrar uma venda contendo um ou mais produtos.

## RF13 — Atualizar estoque após venda

Ao concluir uma venda, o sistema deve diminuir automaticamente a quantidade dos

produtos vendidos.

## RF14 — Calcular valor da venda

O sistema deve calcular automaticamente:

- subtotal;

- desconto;

- valor final.

## RF15 — Registrar forma de pagamento

O sistema deve permitir informar a forma de pagamento utilizada.

Exemplos:

- dinheiro;

- PIX;

- cartão;

- outros.

## RF16 — Aplicar desconto

O sistema deve permitir aplicar desconto durante uma venda.

## RF17 — Cadastrar cliente

O sistema deve permitir cadastrar clientes. O cadastro poderá conter:

- nome;

- telefone;

- observações.

## RF18 — Consultar histórico do cliente

O sistema deve permitir visualizar as compras e encomendas relacionadas a

determinado cliente.

## RF19 — Registrar encomenda

O sistema deve permitir registrar uma encomenda feita por um cliente. A

encomenda deverá possuir:

- cliente;

- descrição;

- produto solicitado;

- valor;


- data do pedido;

- previsão de entrega;

- observações.

## RF20 — Controlar situação da encomenda

O sistema deve permitir definir o estado da encomenda como:

- registrada;

- em produção;

- pronta;

- entregue;

- cancelada.

## RF21 — Consultar histórico de vendas

O sistema deve permitir visualizar as vendas realizadas anteriormente.

## RF22 — Cancelar venda

O sistema deve permitir cancelar uma venda realizada incorretamente. Ao cancelar

a venda, os produtos devem retornar ao estoque quando aplicável.

## RF23 — Gerar comprovante da venda

O sistema deve permitir gerar um resumo ou comprovante contendo as principais

informações da venda.

## RF24 — Exibir painel inicial

O sistema deve possuir um painel apresentando informações importantes, como:

- vendas do dia;

- faturamento;

- quantidade de produtos disponíveis;

- produtos com estoque baixo;

- encomendas pendentes.

## RF25 — Gerar relatório de vendas

O sistema deve permitir consultar as vendas realizadas em determinado período.

## RF26 — Consultar faturamento

O sistema deve permitir consultar o faturamento por:

- dia;

- semana;

- mês;

- período personalizado.

## RF27 — Consultar produtos mais vendidos


Projeto Ateliê

O SISTEMA DEVE PERMITIR IDENTIFICAR OS PRODUTOS COM MAIOR QUANTIDADE DE VENDAS.

## RF28 — Registrar histórico de movimentação

O sistema deve registrar entradas, saídas e ajustes realizados no estoque.

## RF29 — Calcular preço sugerido da peça

O sistema deve permitir calcular o preço sugerido de venda de uma peça com

base:

- no consumo de materiais utilizados na peça (tecido, linha, aviamentos) e nos seus custos unitários registrados no estoque;

- em custos indiretos definidos pela proprietária, expressos em percentual (ex.: energia);

- em uma margem de lucro percentual definida pela proprietária.

O sistema deve apresentar o valor total sugerido para a peça, permitindo que a

proprietária ajuste manualmente esse valor antes de utilizá-lo como preço de cadastro do produto.

## 8 REQUISITOS NÃO FUNCIONAIS

## RNF01 — Usabilidade

O sistema deve possuir uma interface simples e intuitiva, adequada para uma

usuária com pouca experiência em informática. Deve utilizar:

- textos objetivos;

- botões claramente identificados;

- poucos passos para realizar operações;

- mensagens fáceis de compreender;

- confirmação antes de operações importantes.

## RNF02 — Acessibilidade visual

O sistema deverá utilizar:

- fontes de tamanho adequado;

- botões grandes;

- bom contraste entre texto e fundo;

● ícones acompanhados de texto sempre que possível. Essa característica é especialmente importante devido ao perfil da cliente.

## RNF03 — Desempenho

Operações comuns, como:

- abrir produtos;

- pesquisar produtos;

- consultar estoque;


- registrar uma venda;

normais de utilização.

Projeto Ateliê

devem apresentar resposta preferencialmente em até 2 segundos, em condições

## RNF04 — Integridade dos dados

O sistema não deve permitir que uma venda deixe a quantidade de um produto

negativa. Movimentações de estoque devem ser registradas para evitar inconsistências.

## 9 HISTÓRIAS DE USUÁRIO — PADRÃO 3C

## HU01 — Acessar o sistema

## Cartão

para que somente pessoas autorizadas tenham acesso às informações.

Como proprietária do ateliê, quero acessar o sistema utilizando minhas credenciais,

## Conversação

funcionalidades administrativas.

Caso as informações estejam incorretas, o sistema deverá apresentar uma

mensagem simples informando o problema.

A proprietária deverá informar suas credenciais antes de acessar as

## Confirmação

- Credenciais válidas devem permitir o acesso.

- Credenciais inválidas devem impedir o acesso.

- A senha não deve ser exibida durante a digitação.

- O sistema deve apresentar mensagem compreensível em caso de erro.

Requisito relacionado: RF01 e RNF04.

## HU02 — Cadastrar uma peça

## Cartão

ser controlada pelo sistema.

## Conversação

quantidade.

## Confirmação

- O sistema deve permitir preencher os dados da peça.

- Nome e preço devem ser obrigatórios.

- Após o cadastro, a peça deve aparecer na lista de produtos.

- O sistema deve informar que o cadastro foi realizado com sucesso. Requisito relacionado: RF02.

Como proprietária, quero cadastrar uma nova peça de roupa, para que ela possa

Devem ser informados dados como nome, categoria, tamanho, cor, preço e

## HU03 — Adicionar foto à peça

## Cartão

Como proprietária, quero adicionar uma foto às peças cadastradas, para


identificá-las mais facilmente.

## Conversação

A fotografia deverá aparecer junto às informações do produto.

## Confirmação

- O sistema deve permitir selecionar uma imagem.

- A imagem deve ficar vinculada ao produto.

- A imagem deve aparecer na consulta do produto.

Requisito relacionado: RF02.

## HU04 — Alterar informações de uma peça

## Cartão

suas informações.

Como proprietária, quero editar uma peça cadastrada, para corrigir ou atualizar

## Conversação

A proprietária poderá alterar informações como preço, descrição, categoria,

tamanho ou cor.

## Confirmação

- O sistema deve carregar os dados atuais.

- Deve permitir modificar os dados.

- As alterações devem ser salvas.

- O sistema deve apresentar mensagem de sucesso.

Requisito relacionado: RF03.

## HU05 — Desativar uma peça

## Cartão

ela não apareça entre os produtos disponíveis para venda.

Como proprietária, quero desativar uma peça que não comercializo mais, para que

## Conversação

Produtos com histórico de vendas não deverão ter seu histórico apagado.

## Confirmação

- O sistema deve solicitar confirmação antes da desativação.

- O produto desativado não deve aparecer como disponível para nova venda.

- Vendas antigas contendo o produto devem continuar disponíveis.

Requisito relacionado: RF04.

## HU06 — Pesquisar uma peça

## Cartão

rapidamente.

Como proprietária, quero pesquisar uma peça pelo nome, para encontrá-la

## Conversação

A pesquisa deverá evitar que a proprietária precise percorrer manualmente toda a

lista.


## Confirmação

- Deve existir campo de pesquisa.

- Produtos correspondentes devem ser apresentados.

- A consulta deve respeitar o requisito de desempenho.

Requisitos relacionados: RF05, RF06 e RNF03.

13 / 20

Projeto Ateliê

## HU07 — Filtrar produtos Cartão

Como proprietária, quero filtrar as peças por categoria, tamanho ou disponibilidade,

para facilitar a consulta do estoque.

## Conversação

Mais de um filtro poderá ser utilizado quando necessário.

## Confirmação

- O filtro selecionado deve alterar a lista apresentada.

- Somente produtos correspondentes aos filtros devem aparecer.

- Deve ser possível remover os filtros.

Requisito relacionado: RF06.

## HU08 — Cadastrar categorias

## Cartão

organizadas. Conversação

classificações.

## Confirmação

- Deve ser possível criar uma categoria.

- A categoria criada deve aparecer no cadastro de produtos.

- O sistema não deve permitir categorias sem nome.

Requisito relacionado: RF07.

Como proprietária, quero criar categorias de produtos, para manter minhas peças

As categorias poderão representar vestido, blusa, saia, conjunto ou outras

## HU09 — Visualizar estoque

## Cartão

saber o que ainda posso vender.

## Conversação

A quantidade deverá aparecer junto às informações do produto.

## Confirmação

- Cada produto deve apresentar sua quantidade.

- Produtos sem estoque devem ser identificados como indisponíveis.

- As quantidades apresentadas devem refletir as movimentações registradas. Requisito relacionado: RF08.

Como proprietária, quero visualizar a quantidade disponível de cada peça, para


Projeto Ateliê

## HU10 — Registrar novas peças produzidas

## Cartão

atualizar meu estoque.

## Conversação

adicionada.

## Confirmação

- O sistema deve solicitar o produto e a quantidade.

- A quantidade informada deve ser adicionada ao estoque atual.

- A movimentação deve ser registrada no histórico.

Requisitos relacionados: RF09 e RF28.

Como proprietária, quero registrar a entrada de novas peças produzidas, para

Quando novas unidades forem produzidas, a proprietária informará a quantidade

## HU11 — Corrigir quantidade do estoque

## Cartão

diferenças causadas por perda, avaria ou erro de contagem.

Como proprietária, quero corrigir a quantidade de uma peça, para resolver

## Conversação

A alteração deverá ficar registrada para permitir a identificação posterior do ajuste.

## Confirmação

- Deve ser possível informar a nova quantidade.

- Deve ser informado um motivo para o ajuste.

- A movimentação deve ficar registrada no histórico.

Requisitos relacionados: RF10 e RF28.

## HU12 — Receber alerta de estoque baixo

## Cartão

determinada peça, para saber quais produtos precisam ser produzidos novamente.

## Conversação

definido.

## Confirmação

- Produtos abaixo do limite devem ser identificados.

- O alerta deve aparecer no painel.

- A quantidade do produto não deve ser alterada pelo alerta.

Requisitos relacionados: RF11 e RF24.

Como proprietária, quero ser avisada quando houver poucas unidades de

O sistema deverá destacar produtos cuja quantidade seja igual ou inferior ao limite

## HU13 — Cadastrar cliente

## Cartão

associados às compras e encomendas.

Como proprietária, quero cadastrar meus clientes, para manter seus dados


## Conversação

O cadastro poderá armazenar nome, telefone e observações.

Projeto Ateliê

## Confirmação

- O sistema deve permitir informar nome e telefone.

- O nome deverá ser obrigatório.

- O cliente deve aparecer na lista após o cadastro.

Requisito relacionado: RF17.

## HU14 — Consultar histórico de um cliente

## Cartão

acompanhar seu histórico no ateliê.

Como proprietária, quero consultar as compras anteriores de um cliente, para

## Conversação

Ao acessar o cliente poderão aparecer vendas e encomendas associadas a ele.

## Confirmação

- O sistema deve listar as vendas do cliente.

- Deve apresentar data e valor das compras.

- Deve apresentar encomendas relacionadas ao cliente.

Requisito relacionado: RF18.

## HU15 — Iniciar uma venda

## Cartão

peças vendidas e o dinheiro recebido.

Como proprietária, quero registrar uma venda, para controlar corretamente as

## Conversação

Uma venda poderá conter um ou mais produtos.

## Confirmação

- Deve ser possível iniciar uma nova venda.

- Deve ser possível adicionar produtos.

- Deve ser possível informar quantidade.

- O sistema deve apresentar o valor acumulado.

Requisito relacionado: RF12.

## HU16 — Calcular automaticamente o total

## Cartão

para evitar erros de cálculo.

Como proprietária, quero que o sistema calcule o valor da venda automaticamente,

## Conversação

O total será calculado considerando produto, quantidade e eventuais descontos.

## Confirmação

- O sistema deve calcular o subtotal.

- Deve considerar a quantidade de cada item.


- Deve atualizar o valor quando um produto for incluído ou removido.

Requisito relacionado: RF14.

## HU17 — Aplicar desconto

## Cartão

especiais a determinados clientes.

Como proprietária, quero aplicar desconto em uma venda, para oferecer condições

## Conversação

O desconto poderá ser registrado antes da conclusão da venda.

## Confirmação

- O sistema deve permitir informar o desconto.

- O valor final deve ser recalculado.

- O valor final não poderá ser negativo.

Requisito relacionado: RF16.

## HU18 — Informar forma de pagamento

## Cartão

como recebi cada venda.

## Conversação

As opções poderão incluir dinheiro, PIX e cartão.

Como proprietária, quero registrar a forma de pagamento utilizada, para controlar

## Confirmação

- O sistema deve apresentar as formas cadastradas.

- Uma forma de pagamento deve ser selecionada antes da conclusão.

- A informação deve aparecer no histórico da venda.

Requisito relacionado: RF15.

## HU19 — Finalizar uma venda

## Cartão

automaticamente o estoque.

## Conversação

dos produtos.

## Confirmação

- O sistema não deve vender quantidade superior ao estoque.

- Ao concluir, a venda deve ser registrada.

- A quantidade dos produtos vendidos deve ser reduzida.

- A movimentação deve aparecer no histórico.

Como proprietária, quero finalizar uma venda, para registrar a operação e atualizar

Antes da confirmação, o sistema deverá verificar se existe quantidade suficiente

Requisitos relacionados: RF12, RF13, RF28 e RNF04.

## HU20 — Emitir comprovante


## Cartão

um resumo da compra.

## Conversação

de pagamento.

## Confirmação

- Deve ser possível gerar o comprovante após a venda.

- O comprovante deve apresentar os itens vendidos.

- O valor apresentado deve corresponder ao valor registrado na venda.

Requisito relacionado: RF23.

Projeto Ateliê

Como proprietária, quero gerar um comprovante da venda, para fornecer ao cliente

O comprovante deverá apresentar produtos, quantidade, valor total, data e forma

## HU21 — Cancelar venda

## Cartão

corrigir erros no sistema.

## Conversação

O cancelamento deverá devolver os produtos ao estoque quando aplicável.

## Confirmação

- O sistema deve solicitar confirmação.

- A venda deve ser identificada como cancelada.

- Os produtos devem retornar ao estoque.

- O cancelamento deve ficar registrado.

Requisito relacionado: RF22.

Como proprietária, quero cancelar uma venda registrada incorretamente, para

## HU22 — Registrar encomenda

## Cartão

peças que ainda precisam ser produzidas.

Como proprietária, quero registrar uma encomenda de roupa, para acompanhar as

## Conversação

previsão de entrega.

## Confirmação

- Deve ser possível escolher um cliente.

- Deve ser possível registrar a descrição.

- Deve ser possível informar a previsão de entrega.

- A encomenda deve ser criada com situação inicial definida.

Requisito relacionado: RF19.

A encomenda será vinculada a um cliente e poderá possuir descrição, valor e

## HU23 — Atualizar situação de encomenda

## Cartão

Como proprietária, quero atualizar o andamento de uma encomenda, para saber


quais pedidos ainda precisam ser produzidos ou entregues.

## Conversação

A encomenda poderá passar por diferentes situações durante seu ciclo.

## Confirmação

O sistema deve permitir alterar a situação para:

- registrada;

- em produção;

- pronta;

- entregue;

- cancelada.

A nova situação deverá permanecer salva.

Requisito relacionado: RF20.

## HU24 — Consultar encomendas pendentes

## Cartão

entregues, para organizar minha produção.

Como proprietária, quero visualizar as encomendas que ainda não foram

## Conversação

As encomendas pendentes deverão aparecer de forma simples e ordenadas

preferencialmente pela previsão de entrega.

## Confirmação

- O sistema deve listar encomendas não entregues.

- Deve apresentar o cliente.

- Deve apresentar a previsão de entrega.

- Deve apresentar a situação atual.

Requisitos relacionados: RF20 e RF24.

## HU25 — Consultar histórico de vendas

## Cartão

operações que já realizei.

## Conversação

Será possível localizar vendas utilizando período ou cliente.

## Confirmação

- O sistema deve listar as vendas.

- Cada venda deve apresentar data e valor.

- Deve ser possível acessar os detalhes da venda.

- Vendas canceladas devem ser identificadas.

Como proprietária, quero consultar minhas vendas anteriores, para verificar as

Requisito relacionado: RF21.

## HU26 — Consultar faturamento Cartão


19 / 20

Projeto Ateliê

Como proprietária, quero visualizar quanto vendi em determinado período, para

acompanhar o desempenho financeiro do ateliê.

## Conversação

A proprietária poderá selecionar períodos como dia, semana, mês ou intervalo

personalizado.

## Confirmação

- O sistema deve permitir escolher o período.

- Deve somar as vendas válidas do período.

- Vendas canceladas não devem fazer parte do faturamento.

Requisito relacionado: RF26.

## HU27 — Consultar produtos mais vendidos

## Cartão

modelos devo continuar produzindo.

Como proprietária, quero visualizar quais peças vendem mais, para decidir quais

## Conversação

O sistema deverá analisar o histórico das vendas e apresentar um ranking.

## Confirmação

- Deve ser possível informar um período.

- Os produtos devem ser classificados pela quantidade vendida.

- As vendas canceladas não devem participar do cálculo.

Requisito relacionado: RF27.

## HU28 — Visualizar painel inicial Cartão

Como proprietária, quero visualizar as principais informações do ateliê assim que

entrar no sistema, para acompanhar rapidamente a situação do negócio.

## Conversação

O painel deve evitar excesso de informações e priorizar dados importantes.

## Confirmação

O painel deverá apresentar, pelo menos:

- vendas recentes;

- faturamento;

- estoque baixo;

- encomendas pendentes.

As informações devem ser apresentadas de forma visualmente simples.

Requisitos relacionados: RF24, RNF01 e RNF02.

## HU29 — Utilizar uma interface simples

## Cartão

simples e de fácil leitura, para conseguir utilizar o sistema sem dificuldades.

Como proprietária com pouca experiência em sistemas, quero uma interface

## Conversação


Projeto Ateliê

Os comandos utilizados com maior frequência devem ser facilmente identificados. A interface deverá evitar termos técnicos desnecessários.

## Confirmação

- Os botões principais devem possuir texto indicando sua função.

- O tamanho das letras deverá permitir leitura confortável.

- Operações importantes deverão apresentar confirmação.

- As telas deverão manter um padrão de navegação.

● As informações deverão apresentar contraste adequado. Requisitos relacionados: RNF01 e RNF02.

## HU30 — Calcular preço sugerido de uma peça Cartão

Como proprietária, quero que o sistema calcule o preço sugerido de uma peça nova

a partir dos materiais utilizados, para não precisar fazer os cálculos manualmente.

## Conversação

Atualmente a proprietária calcula o preço somando o custo de cada material

utilizado (por exemplo, metros de tecido e quantidade de botões), acrescentando um percentual para custos indiretos como energia e, por fim, aplicando uma margem de lucro.

O sistema deverá reproduzir esse mesmo raciocínio automaticamente, bastando

que a proprietária informe quais materiais e quantidades foram usados na peça.

## Confirmação

- O sistema deve permitir selecionar os materiais utilizados e suas quantidades.

- O sistema deve calcular o custo total dos materiais informados.

- O sistema deve permitir informar um percentual de custos indiretos.

- O sistema deve permitir informar uma margem de lucro percentual.

- O sistema deve apresentar o preço sugerido final.

- A proprietária deve poder ajustar manualmente o preço sugerido antes de salvar. Requisito relacionado: RF29.
