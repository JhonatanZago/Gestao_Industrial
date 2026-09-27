# TASK EXECUTÁVEL 02 --- Funcionários e Peças

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex**.

Esta task deve ser executada SOMENTE após a conclusão e estabilização da
TASK 01 --- Fundação: Empresa e Setores Dinâmicos.

Antes de alterar o código:

1.  leia o projeto completo;
2.  leia os artefatos OpenSpec existentes;
3.  leia a change anterior;
4.  identifique as convenções implementadas;
5.  preserve arquitetura, padrões, tratamento de erros, migrations e
    testes existentes;
6.  valide o estado atual do projeto executando os testes antes de
    iniciar.

Não responda apenas com um plano. Crie/atualize os artefatos OpenSpec,
implemente, teste, corrija e valide o build.

**Não implementar frontend.** **Não iniciar a próxima task
automaticamente.**

------------------------------------------------------------------------

# 1. Diretriz geral do projeto

O desenvolvimento seguirá a estratégia:

``` text
BACKEND COMPLETO E ESTÁVEL
        ↓
FRONTEND
```

Nenhuma tela Angular, HTML, CSS ou outro frontend deverá ser
implementado nesta task.

O backend será concluído primeiro, incluindo futuramente:

-   Empresa;
-   Setores;
-   Funcionários;
-   Peças;
-   Máquinas;
-   Turnos;
-   Produção;
-   Paradas;
-   importação Excel;
-   indicadores;
-   comparativos;
-   valorização da produção;
-   autenticação;
-   autorização;
-   auditoria;
-   relatórios;
-   APIs para Dashboard;
-   testes;
-   qualidade;
-   preparação para IA/RAG.

Somente após essa base estar concluída e estável deverá ser iniciada uma
fase específica de frontend.

------------------------------------------------------------------------

# 2. Objetivo desta task

Implementar as capacidades:

``` text
Funcionário
Peça
```

Essas duas entidades formarão a base necessária para o futuro Registro
de Produção.

Ao final, o domínio deverá permitir representar:

``` text
EMPRESA
│
├── SETORES
│    ├── Pintura a pó
│    ├── Solda
│    └── ...
│
├── FUNCIONÁRIOS
│    ├── 00147
│    ├── 00231
│    └── ...
│
└── PEÇAS
     ├── 54871
     ├── 98421
     └── ...
```

IMPORTANTE:

Nesta etapa ainda NÃO registrar produção.

A associação completa:

``` text
Funcionário + Setor + Peça + Quantidade + Horário
```

será implementada na próxima change de Registro de Produção.

------------------------------------------------------------------------

# 3. OpenSpec

Criar uma nova change seguindo as convenções OpenSpec já existentes.

Nome sugerido:

``` text
funcionarios-pecas
```

A change deverá conter os artefatos exigidos pela versão do OpenSpec
instalada.

Conceitualmente:

``` text
openspec/
└── changes/
    └── funcionarios-pecas/
        ├── proposal.md
        ├── design.md
        ├── tasks.md
        └── specs/
```

Não modificar specs anteriores de forma incompatível.

Se uma alteração nas specs anteriores for realmente necessária:

1.  justificar;
2.  registrar no design;
3.  preservar compatibilidade sempre que possível;
4.  executar novamente os testes relacionados.

------------------------------------------------------------------------

# 4. Proposal

O proposal deverá explicar:

## Problema

As planilhas da metalúrgica registram principalmente informações como:

``` text
código do funcionário
código da peça
quantidade produzida
horário
```

Antes de importar ou registrar produção, o sistema precisa possuir
cadastros confiáveis de Funcionários e Peças.

## Solução

Criar:

-   cadastro de Funcionário;
-   cadastro de Peça;
-   identificação por códigos empresariais;
-   associação organizacional correta;
-   ativação/desativação;
-   consultas;
-   validações;
-   APIs;
-   testes.

## Resultado

Preparar o sistema para a próxima capacidade:

``` text
REGISTRO DE PRODUÇÃO
```

------------------------------------------------------------------------

# 5. Funcionário --- conceito

`Funcionario` representa um colaborador que poderá aparecer nos
registros de produção.

A planilha identifica o funcionário principalmente através de um código.

Exemplo:

``` text
00147
```

Esse código deverá ser preservado como identificador empresarial.

NÃO utilizar número inteiro para o código se isso puder remover zeros à
esquerda.

Exemplo:

``` text
00147
```

deve continuar:

``` text
00147
```

e NÃO virar:

``` text
147
```

------------------------------------------------------------------------

# 6. Entidade Funcionário

Criar entidade `Funcionario`.

Campos mínimos:

``` text
id
codigo
nome
ativo
empresa
setorPrincipal (opcional conforme design)
createdAt
updatedAt
```

O campo `nome` poderá existir para facilitar administração e relatórios,
mas o código continuará sendo a referência principal para integração com
as planilhas.

------------------------------------------------------------------------

# 7. Funcionário e Empresa

Todo funcionário MUST pertencer a uma Empresa.

Não permitir funcionário sem empresa.

Conceitualmente:

``` text
Empresa
   │
   └── Funcionários
```

O código do funcionário deverá ser único dentro da empresa.

Permitido:

``` text
Empresa A → 00147
Empresa B → 00147
```

Não permitido:

``` text
Empresa A → 00147
Empresa A → 00147
```

Criar proteção:

-   na camada de negócio;
-   no banco de dados.

Constraint conceitual:

``` text
empresa_id + codigo
```

------------------------------------------------------------------------

# 8. Funcionário e Setor

IMPORTANTE:

Não criar uma modelagem que obrigue permanentemente um funcionário a
produzir apenas em um setor.

Um funcionário poderá eventualmente:

-   mudar de setor;
-   auxiliar outro setor;
-   produzir em setores diferentes em datas diferentes.

O futuro registro de produção deverá registrar em qual setor aquela
produção aconteceu.

Se existir `setorPrincipal`, ele deverá representar apenas o setor
padrão/lotação atual do funcionário, e NÃO limitar os futuros registros
de produção.

Registrar essa decisão no `design.md`.

------------------------------------------------------------------------

# 9. Código do funcionário

O código:

-   é obrigatório;
-   deve ser tratado como String;
-   deve preservar zeros à esquerda;
-   deve remover espaços externos;
-   não deve ser convertido automaticamente para número;
-   deve ser único dentro da empresa.

Entrada:

``` text
" 00147 "
```

Resultado:

``` text
00147
```

Não assumir tamanho fixo sem evidência das planilhas reais.

Aplicar limite de tamanho razoável.

------------------------------------------------------------------------

# 10. Nome do funcionário

Nome:

-   obrigatório para cadastro administrativo, salvo se a análise do
    projeto justificar importação provisória por código;
-   remover espaços externos;
-   impedir vazio;
-   aplicar tamanho máximo adequado.

Se houver necessidade futura de importar uma planilha contendo código
desconhecido, NÃO criar automaticamente funcionário nesta task.

Essa regra será definida na task de Importação Excel.

------------------------------------------------------------------------

# 11. Status do funcionário

Utilizar exclusão lógica.

``` text
ativo = true
ativo = false
```

Não remover fisicamente funcionários através da API operacional.

Isso é obrigatório porque futuramente registros históricos de produção
dependerão deles.

------------------------------------------------------------------------

# 12. Peça --- conceito

`Peca` representa um item produzido/processado pela metalúrgica.

A planilha identifica a peça principalmente através de um código.

Exemplos:

``` text
54871
98421
ABC-1020
```

O sistema NÃO deve assumir que códigos de peças são exclusivamente
numéricos.

Utilizar String.

------------------------------------------------------------------------

# 13. Entidade Peça

Criar entidade `Peca`.

Campos mínimos:

``` text
id
codigo
descricao
ativo
empresa
createdAt
updatedAt
```

Preparar a arquitetura para futuramente adicionar, em changes
específicas:

``` text
valorUnitario
tempoPadrao
metaProducao
unidadeMedida
cliente
roteiroProdutivo
```

NÃO adicionar esses campos agora sem necessidade.

------------------------------------------------------------------------

# 14. Peça e Empresa

Toda peça MUST pertencer a uma Empresa.

O código deverá ser único dentro da empresa.

Permitido:

``` text
Empresa A → 54871
Empresa B → 54871
```

Não permitido:

``` text
Empresa A → 54871
Empresa A → 54871
```

Criar constraint conceitual:

``` text
empresa_id + codigo
```

------------------------------------------------------------------------

# 15. Código da peça

O código deverá:

-   ser obrigatório;
-   ser String;
-   preservar zeros;
-   aceitar letras quando necessário;
-   aceitar hífen quando necessário;
-   remover espaços externos;
-   possuir tamanho máximo adequado.

NÃO converter automaticamente para número.

NÃO inventar formato rígido sem evidência dos dados reais.

------------------------------------------------------------------------

# 16. Descrição da peça

A descrição deverá permitir identificar a peça de maneira amigável.

Exemplo:

``` text
Código: 54871
Descrição: Suporte lateral modelo X
```

Aplicar validações básicas e tamanho máximo adequado.

------------------------------------------------------------------------

# 17. Peça NÃO pertence fixamente a um único setor

Não modelar:

``` text
Peça → um único Setor obrigatório
```

Uma mesma peça poderá passar por diferentes processos industriais:

``` text
Laser
  ↓
Dobradeira
  ↓
Solda
  ↓
KTL
  ↓
Pintura a pó
```

O setor deverá fazer parte do futuro Registro de Produção.

Essa decisão é fundamental para evitar uma modelagem incorreta.

Registrar no `design.md`.

------------------------------------------------------------------------

# 18. DTOs

Criar DTOs separados para request e response.

Exemplo:

``` text
FuncionarioRequest
FuncionarioResponse

PecaRequest
PecaResponse
```

Se houver DTOs distintos para criação e atualização, manter convenção
consistente.

Aplicar Jakarta Validation nos requests.

Não retornar entidades JPA diretamente.

------------------------------------------------------------------------

# 19. Mappers

Criar mappers:

``` text
Funcionario ↔ DTO
Peca ↔ DTO
```

Seguir o padrão adotado na TASK 01.

Não espalhar conversões pelos Resources.

------------------------------------------------------------------------

# 20. Repositories

Criar:

``` text
FuncionarioRepository
PecaRepository
```

Adicionar consultas necessárias para:

### Funcionário

-   buscar por ID e empresa;
-   verificar código por empresa;
-   listar por empresa;
-   filtrar ativos;
-   pesquisar por código;
-   pesquisar por nome.

### Peça

-   buscar por ID e empresa;
-   verificar código por empresa;
-   listar por empresa;
-   filtrar ativos;
-   pesquisar por código;
-   pesquisar por descrição.

Evitar N+1.

------------------------------------------------------------------------

# 21. Services

Criar:

``` text
FuncionarioService
PecaService
```

ou seguir a nomenclatura existente.

Services serão responsáveis por:

-   normalização;
-   existência;
-   duplicidade;
-   relacionamento com Empresa;
-   relacionamento opcional com Setor Principal;
-   ativação;
-   desativação;
-   atualização.

Resources não devem possuir regra de negócio.

------------------------------------------------------------------------

# 22. API --- Funcionários

Utilizar contexto de Empresa.

Implementar:

``` http
POST /api/v1/empresas/{empresaId}/funcionarios

GET /api/v1/empresas/{empresaId}/funcionarios

GET /api/v1/empresas/{empresaId}/funcionarios/{funcionarioId}

PUT /api/v1/empresas/{empresaId}/funcionarios/{funcionarioId}

PATCH /api/v1/empresas/{empresaId}/funcionarios/{funcionarioId}/ativar

PATCH /api/v1/empresas/{empresaId}/funcionarios/{funcionarioId}/desativar
```

Não implementar DELETE físico.

------------------------------------------------------------------------

# 23. API --- Peças

Implementar:

``` http
POST /api/v1/empresas/{empresaId}/pecas

GET /api/v1/empresas/{empresaId}/pecas

GET /api/v1/empresas/{empresaId}/pecas/{pecaId}

PUT /api/v1/empresas/{empresaId}/pecas/{pecaId}

PATCH /api/v1/empresas/{empresaId}/pecas/{pecaId}/ativar

PATCH /api/v1/empresas/{empresaId}/pecas/{pecaId}/desativar
```

Não implementar DELETE físico.

------------------------------------------------------------------------

# 24. Busca por código

Preparar endpoints/listagens para busca por código.

Exemplo conceitual:

``` http
GET /api/v1/empresas/1/funcionarios?codigo=00147
```

``` http
GET /api/v1/empresas/1/pecas?codigo=54871
```

Também permitir buscas administrativas simples por nome/descrição quando
adequado.

Não criar mecanismo avançado de busca nesta task.

------------------------------------------------------------------------

# 25. Paginação

Todas as listagens devem ser paginadas.

Exemplo:

``` http
GET /api/v1/empresas/1/funcionarios?page=0&size=20
```

``` http
GET /api/v1/empresas/1/pecas?page=0&size=20
```

Utilizar `Pageable` seguindo o padrão existente.

------------------------------------------------------------------------

# 26. Isolamento por Empresa

Um funcionário da Empresa B não pode ser acessado através da Empresa A.

Uma peça da Empresa B não pode ser acessada através da Empresa A.

Criar testes específicos.

Exemplo:

``` text
Funcionario 10 pertence à Empresa 2
```

A chamada:

``` http
GET /api/v1/empresas/1/funcionarios/10
```

não deve retornar o funcionário.

O mesmo vale para peças.

------------------------------------------------------------------------

# 27. Setor principal --- validação

Se `setorPrincipal` for implementado:

-   deverá pertencer à mesma empresa do funcionário;
-   setor de outra empresa deve ser rejeitado;
-   setor inativo deve seguir regra explicitamente definida;
-   o vínculo não deverá impedir produções futuras em outros setores.

Não criar relacionamento se ele gerar complexidade desnecessária para
esta etapa. Registrar a decisão no design.

------------------------------------------------------------------------

# 28. Tratamento de exceções

Reutilizar o padrão global criado na TASK 01.

Não criar um segundo sistema de erros paralelo.

Utilizar as exceções existentes quando adequadas.

Exemplos de mensagens:

``` text
Funcionário não encontrado.
Peça não encontrada.
Já existe um funcionário com o código 00147 nesta empresa.
Já existe uma peça com o código 54871 nesta empresa.
```

------------------------------------------------------------------------

# 29. Migrations

Criar migrations Flyway para:

``` text
funcionario
peca
```

Adicionar:

-   PK;
-   FK para empresa;
-   FK opcional para setor principal, caso adotado;
-   NOT NULL;
-   constraints de unicidade;
-   índices úteis.

Exemplos conceituais:

``` text
UNIQUE (empresa_id, codigo)
```

para Funcionário e Peça.

Não alterar migrations antigas já aplicadas de forma destrutiva.

Criar novas versões.

------------------------------------------------------------------------

# 30. Testes --- Funcionário

Criar testes automatizados para:

1.  cadastrar funcionário válido;
2.  preservar código `00147`;
3.  remover espaços externos;
4.  rejeitar código vazio;
5.  rejeitar nome inválido;
6.  rejeitar empresa inexistente;
7.  rejeitar código duplicado na mesma empresa;
8.  permitir mesmo código em empresas diferentes;
9.  consultar funcionário;
10. listar funcionários;
11. pesquisar por código;
12. atualizar funcionário;
13. desativar funcionário;
14. reativar funcionário;
15. impedir acesso cruzado entre empresas;
16. validar setor principal, caso implementado.

------------------------------------------------------------------------

# 31. Testes --- Peça

Criar testes automatizados para:

1.  cadastrar peça válida;
2.  preservar zeros no código;
3.  aceitar código alfanumérico;
4.  aceitar código com hífen quando válido;
5.  normalizar espaços externos;
6.  rejeitar código vazio;
7.  rejeitar empresa inexistente;
8.  rejeitar código duplicado na mesma empresa;
9.  permitir mesmo código em empresas diferentes;
10. consultar peça;
11. listar peças;
12. pesquisar por código;
13. atualizar peça;
14. desativar peça;
15. reativar peça;
16. impedir acesso cruzado entre empresas.

------------------------------------------------------------------------

# 32. Cenário de integração obrigatório

Criar cenário:

``` text
Metalúrgica A
│
├── Setor: PINT
│
├── Funcionário: 00147
│
└── Peça: 54871

Metalúrgica B
│
├── Funcionário: 00147
│
└── Peça: 54871
```

O sistema deve permitir códigos iguais em empresas diferentes.

Entretanto:

``` text
Metalúrgica A
├── Funcionário 00147
└── Funcionário 00147
```

deve ser rejeitado.

Também:

``` text
Metalúrgica A
├── Peça 54871
└── Peça 54871
```

deve ser rejeitado.

------------------------------------------------------------------------

# 33. Preparação para planilhas Excel

Embora Excel NÃO seja implementado agora, a modelagem deve considerar
que futuramente uma linha da planilha poderá conter:

``` text
codigoFuncionario
codigoPeca
quantidade
horario
setor
data
```

Portanto:

-   códigos devem ser estáveis;
-   zeros à esquerda devem ser preservados;
-   códigos não devem depender do ID técnico do banco;
-   registros não devem ser fisicamente apagados;
-   isolamento por empresa deve ser consistente.

------------------------------------------------------------------------

# 34. Preparação para Registro de Produção

A próxima change deverá conseguir criar algo conceitualmente semelhante
a:

``` text
RegistroProducao

empresa
setor
funcionario
peca
quantidade
data
horarioInicio
horarioFim
```

NÃO criar `RegistroProducao` nesta task.

Apenas garantir que `Funcionario` e `Peca` estejam preparados para serem
referenciados.

------------------------------------------------------------------------

# 35. Fora do escopo

NÃO implementar:

-   Registro de Produção;
-   quantidade produzida;
-   horário de produção;
-   produtividade;
-   tempo produtivo;
-   parada;
-   máquina;
-   turno;
-   importação Excel;
-   valor da peça;
-   faturamento;
-   indicadores;
-   Dashboard;
-   relatórios;
-   frontend;
-   autenticação;
-   autorização;
-   IA;
-   RAG.

Esses itens terão changes próprias.

------------------------------------------------------------------------

# 36. OpenSpec --- specs esperadas

Criar/atualizar specs correspondentes às capacidades:

``` text
funcionarios
pecas
```

Os requisitos deverão ser expressos com cenários verificáveis.

Incluir especialmente:

### Funcionários

-   cadastro;
-   código empresarial;
-   unicidade por empresa;
-   preservação de zeros;
-   ativação/desativação;
-   isolamento por empresa.

### Peças

-   cadastro;
-   código empresarial;
-   código alfanumérico;
-   unicidade por empresa;
-   ativação/desativação;
-   isolamento por empresa;
-   ausência de vínculo obrigatório com um único setor.

------------------------------------------------------------------------

# 37. Qualidade e regressão

Antes de considerar a task concluída:

1.  executar os testes existentes ANTES da implementação;
2.  implementar;
3.  executar testes novos;
4.  executar testes antigos;
5.  corrigir regressões;
6.  executar suite completa;
7.  executar build.

A TASK 02 não poderá quebrar a TASK 01.

------------------------------------------------------------------------

# 38. Comandos obrigatórios

No Windows:

``` powershell
.\gradlew.bat clean test
```

Depois:

``` powershell
.\gradlew.bat build
```

Se houver comandos de validação OpenSpec disponíveis na instalação
atual, executá-los também.

Não inventar comandos OpenSpec. Descobrir os comandos realmente
suportados pela versão instalada.

------------------------------------------------------------------------

# 39. Não mascarar falhas

É proibido:

-   apagar testes;
-   desabilitar testes;
-   ignorar exceções;
-   remover constraints para passar testes;
-   mudar regras da spec silenciosamente;
-   criar `try/catch` vazio;
-   retornar sucesso em operação inválida;
-   considerar build quebrado como concluído.

Corrigir a causa real.

------------------------------------------------------------------------

# 40. Critérios de aceite

A task estará concluída somente quando:

1.  Empresa puder possuir vários funcionários;
2.  Empresa puder possuir várias peças;
3.  funcionário `00147` preservar zeros;
4.  peça puder usar código numérico ou alfanumérico;
5.  código de funcionário for único por empresa;
6.  código de peça for único por empresa;
7.  mesmos códigos puderem existir em empresas diferentes;
8.  funcionários puderem ser ativados/desativados;
9.  peças puderem ser ativadas/desativadas;
10. registros não forem fisicamente apagados;
11. funcionário não ficar permanentemente limitado a um setor produtivo;
12. peça não ficar permanentemente limitada a um setor produtivo;
13. APIs utilizarem DTOs;
14. isolamento por empresa funcionar;
15. migrations funcionarem;
16. testes da TASK 01 continuarem passando;
17. novos testes passarem;
18. `clean test` passar;
19. `build` passar;
20. OpenSpec refletir fielmente a implementação.

------------------------------------------------------------------------

# 41. Entrega esperada do Codex

Ao finalizar, apresentar relatório contendo:

## OpenSpec

-   change criada;
-   proposal;
-   design;
-   tasks;
-   specs criadas/alteradas;
-   resultado da validação OpenSpec, se disponível.

## Código

Listar arquivos criados/alterados.

## Banco

Listar migrations e constraints.

## APIs

Listar endpoints implementados.

## Regras

Resumir decisões importantes, principalmente:

-   códigos como String;
-   unicidade por empresa;
-   vínculo de funcionário com setor;
-   ausência de setor fixo na peça;
-   exclusão lógica.

## Testes

Informar:

-   testes executados;
-   testes novos;
-   testes antigos/regressão;
-   falhas encontradas;
-   correções realizadas;
-   resultado final.

## Build

Informar resultado de:

``` text
clean test
build
```

## Pendências

Informar apenas pendências reais.

Não iniciar a próxima change.

------------------------------------------------------------------------

# 42. Próxima etapa prevista

Após aprovação desta task, a próxima change será:

``` text
TASK 03 — Registro de Produção
```

Ela deverá começar a conectar:

``` text
SETOR
  +
FUNCIONÁRIO
  +
PEÇA
  +
QUANTIDADE
  +
DATA/HORÁRIO
       ↓
REGISTRO DE PRODUÇÃO
```

Essa será a primeira etapa diretamente relacionada aos dados existentes
nas planilhas da metalúrgica.

Aguardar aprovação antes de implementá-la.
