# TASK EXECUTÁVEL 01 --- Fundação: Empresa e Setores Dinâmicos

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex** como fluxo de desenvolvimento.

Não responda apenas com análise, sugestões ou plano. Primeiro analise o
projeto e a estrutura OpenSpec existente, crie/atualize os artefatos
necessários da change, implemente o código, execute os testes, corrija
os erros encontrados e valide o build.

**Não iniciar a próxima task automaticamente.**

------------------------------------------------------------------------

# 1. Contexto do projeto

O sistema está sendo criado do zero para uma metalúrgica.

Atualmente, os dados de produção são registrados em planilhas Excel
pelos diversos setores da empresa.

Exemplos de setores atuais:

-   Pintura a pó
-   Solda
-   KTL
-   Estamparia
-   Zincagem
-   Dobradeira
-   Laser

A lista acima NÃO é fixa.

A empresa possui outros setores e deverá ser possível cadastrar novos
setores futuramente sem:

-   alterar código Java;
-   criar novos enums;
-   criar classes específicas;
-   recompilar regras de negócio;
-   alterar manualmente a estrutura do sistema.

O objetivo futuro do sistema será transformar os dados industriais em:

-   dashboards;
-   produção total;
-   produção por setor;
-   produção por funcionário;
-   produção por peça;
-   tempo produtivo;
-   tempo de parada;
-   produtividade;
-   desempenho;
-   comparativos entre períodos;
-   valorização da produção;
-   relatórios gerenciais para o proprietário;
-   resumos para usuários;
-   histórico de produção;
-   IA e RAG em etapas futuras.

Esta task deve criar apenas a fundação necessária para as próximas
etapas.

------------------------------------------------------------------------

# 2. Objetivo

Implementar a primeira fundação funcional do backend:

1.  configuração técnica do projeto;
2.  entidade Empresa;
3.  entidade Setor;
4.  setores dinâmicos;
5.  relacionamento Empresa → Setores;
6.  CRUD de Empresa;
7.  CRUD de Setor;
8.  validações;
9.  DTOs;
10. Mappers;
11. Services;
12. Repositories;
13. Resources/Controllers;
14. tratamento global de exceções;
15. migrations;
16. paginação básica;
17. testes automatizados;
18. documentação OpenSpec da change;
19. README inicial.

Ao final deverá ser possível representar:

``` text
EMPRESA
│
├── Pintura a pó
├── Solda
├── KTL
├── Estamparia
├── Zincagem
├── Dobradeira
├── Laser
└── qualquer novo setor cadastrado futuramente
```

------------------------------------------------------------------------

# 3. Stack obrigatória

Utilizar a stack já definida para o projeto:

-   Java 17;
-   Eclipse Temurin 17;
-   Spring Boot;
-   Gradle Groovy;
-   Spring Web;
-   Spring Data JPA;
-   Jakarta Validation;
-   Lombok;
-   PostgreSQL;
-   H2 para testes;
-   Flyway;
-   JUnit 5;
-   Mockito.

Não adicionar dependências sem necessidade real.

------------------------------------------------------------------------

# 4. OpenSpec --- diagnóstico obrigatório

Antes de alterar o código:

1.  leia a estrutura completa do projeto;
2.  localize e analise `openspec/`;
3.  identifique a versão e as convenções OpenSpec utilizadas;
4.  verifique se já existem `specs`, `changes`, templates ou instruções;
5.  preserve as convenções existentes;
6.  não invente uma estrutura incompatível com a instalação atual.

Criar uma change equivalente a:

``` text
foundation-empresa-setores
```

A change deverá possuir, conforme a convenção OpenSpec realmente
instalada, artefatos equivalentes a:

``` text
openspec/
└── changes/
    └── foundation-empresa-setores/
        ├── proposal.md
        ├── design.md
        ├── tasks.md
        └── specs/
```

Se a versão instalada utilizar outra estrutura oficial, adaptar para
ela.

------------------------------------------------------------------------

# 5. Proposal da change

O `proposal.md` deverá registrar claramente:

## Problema

A metalúrgica possui diversos setores produtivos e atualmente seus dados
são registrados em planilhas.

Precisamos estabelecer a raiz organizacional do sistema antes de
implementar funcionários, peças, produção e importação Excel.

## Solução

Criar:

-   Empresa;
-   Setor dinâmico;
-   relacionamento Empresa → Setores;
-   APIs e regras necessárias para administrar esses registros.

## Impacto futuro

A fundação deverá suportar posteriormente:

``` text
Empresa
   ↓
Setor
   ↓
Funcionário
   ↓
Produção
   ↓
Peça
```

e ainda:

-   máquinas;
-   turnos;
-   paradas;
-   importações;
-   indicadores;
-   dashboards;
-   relatórios;
-   usuários;
-   permissões;
-   IA/RAG.

Não implementar esses módulos agora.

------------------------------------------------------------------------

# 6. Design obrigatório

Registrar no `design.md` as decisões arquiteturais relevantes.

## 6.1 Setores são dinâmicos

É PROIBIDO representar os setores produtivos através de enum fixo.

Não criar:

``` java
enum Setor {
    PINTURA,
    SOLDA,
    KTL,
    LASER
}
```

Também não criar regras como:

``` java
if (setor == PINTURA) {
}
```

ou `switch` baseado nos nomes dos setores.

`Setor` deve ser uma entidade persistida.

O sistema deverá aceitar futuramente, por exemplo:

``` text
Usinagem
Montagem
Polimento
Expedição
Embalagem
```

sem alteração no código.

## 6.2 Exclusão lógica

Empresa e Setor não deverão ser fisicamente apagados nas operações
normais.

Utilizar status ativo/inativo.

Isso será necessário para preservar histórico de produção futuramente.

## 6.3 Empresa como raiz

Cada Setor pertence exatamente a uma Empresa.

Uma Empresa pode possuir vários Setores.

## 6.4 API não expõe entidade JPA

Utilizar DTOs de entrada e saída.

Controllers/Resources não devem retornar entidades JPA diretamente.

## 6.5 Regra de negócio no Service

Controllers devem permanecer finos.

Normalização, duplicidade, existência, status e relacionamentos devem
ser tratados na camada de serviço/domínio apropriada.

------------------------------------------------------------------------

# 7. Estrutura inicial

Organizar aproximadamente:

``` text
src/main/java/com/metalurgica/gestao/
│
├── config/
├── domain/
├── dto/
│   ├── request/
│   └── response/
├── exception/
├── mapper/
├── repository/
├── resource/
└── service/
```

Respeitar o package real criado no projeto caso seja diferente.

Não criar classes vazias apenas para preencher diretórios.

------------------------------------------------------------------------

# 8. Empresa

Criar entidade `Empresa`.

Campos mínimos:

``` text
id
razaoSocial
nomeFantasia
cnpj
ativo
createdAt
updatedAt
```

## 8.1 Regras

`razaoSocial`:

-   obrigatória;
-   não aceitar `null`;
-   não aceitar vazio;
-   não aceitar somente espaços;
-   normalizar espaços externos.

`nomeFantasia`:

-   obrigatório;
-   mesmas validações básicas.

`cnpj`:

-   obrigatório;
-   armazenar somente números;
-   impedir duplicidade.

Exemplo:

``` text
Entrada:
12.345.678/0001-90

Persistência:
12345678000190
```

Empresa nova deverá iniciar com:

``` text
ativo = true
```

Registrar automaticamente:

``` text
createdAt
updatedAt
```

------------------------------------------------------------------------

# 9. Setor

Criar entidade `Setor`.

Campos mínimos:

``` text
id
codigo
nome
descricao
ativo
empresa
createdAt
updatedAt
```

Exemplo:

``` text
codigo: PINT
nome: Pintura a pó
descricao: Setor responsável pelo processo de pintura eletrostática.
ativo: true
```

------------------------------------------------------------------------

# 10. Código do setor

O código deverá ser normalizado.

Entrada:

``` text
" pint "
```

Resultado:

``` text
PINT
```

O código deverá ser único dentro da empresa.

Permitido:

``` text
Empresa A → PINT
Empresa B → PINT
```

Não permitido:

``` text
Empresa A → PINT
Empresa A → PINT
```

Criar proteção:

1.  na regra de negócio;
2.  através de constraint no banco.

A constraint deverá considerar conceitualmente:

``` text
empresa_id + codigo
```

------------------------------------------------------------------------

# 11. Nome do setor

O nome é obrigatório.

Normalizar espaços externos.

Entrada:

``` text
"   Pintura a pó   "
```

Resultado:

``` text
"Pintura a pó"
```

Impedir duplicidade lógica de setores equivalentes dentro da mesma
empresa de acordo com a estratégia de normalização adotada.

------------------------------------------------------------------------

# 12. Relacionamento Empresa → Setor

Modelar corretamente:

``` text
Empresa 1 ---- N Setores
```

Cada setor pertence exatamente a uma empresa.

Evitar:

-   serialização circular;
-   exposição direta de entidades;
-   carregamentos desnecessários;
-   N+1 quando houver consulta relacionada.

------------------------------------------------------------------------

# 13. DTOs

Criar DTOs separados para request e response.

Exemplo:

``` text
EmpresaRequest
EmpresaResponse

SetorRequest
SetorResponse
```

Utilizar Jakarta Validation nos DTOs de entrada.

Não colocar regra de negócio complexa dentro dos DTOs.

------------------------------------------------------------------------

# 14. Mapper

Criar mappers responsáveis por:

``` text
Empresa ↔ DTO
Setor ↔ DTO
```

Não espalhar conversões pelos controllers.

Não adicionar MapStruct somente por conveniência nesta primeira change.

Mapper manual é suficiente.

------------------------------------------------------------------------

# 15. Repositories

Criar:

``` text
EmpresaRepository
SetorRepository
```

Implementar consultas necessárias para:

-   verificar CNPJ existente;
-   verificar código de setor dentro da empresa;
-   verificar nome duplicado dentro da empresa;
-   buscar setores pertencentes à empresa;
-   filtrar ativos/inativos quando necessário.

------------------------------------------------------------------------

# 16. Services

Criar serviços responsáveis pelas regras de negócio.

O fluxo esperado é:

``` text
HTTP
 ↓
Resource
 ↓
Service
 ↓
Repository
 ↓
Database
```

O Resource não deve decidir:

-   duplicidade;
-   normalização;
-   pertencimento;
-   status;
-   existência de recurso.

------------------------------------------------------------------------

# 17. API --- Empresa

Implementar:

``` http
POST /api/v1/empresas
GET /api/v1/empresas
GET /api/v1/empresas/{id}
PUT /api/v1/empresas/{id}
PATCH /api/v1/empresas/{id}/ativar
PATCH /api/v1/empresas/{id}/desativar
```

Não implementar `DELETE` físico.

------------------------------------------------------------------------

# 18. API --- Setores

Implementar:

``` http
POST /api/v1/empresas/{empresaId}/setores
GET /api/v1/empresas/{empresaId}/setores
GET /api/v1/empresas/{empresaId}/setores/{setorId}
PUT /api/v1/empresas/{empresaId}/setores/{setorId}
PATCH /api/v1/empresas/{empresaId}/setores/{setorId}/ativar
PATCH /api/v1/empresas/{empresaId}/setores/{setorId}/desativar
```

------------------------------------------------------------------------

# 19. Isolamento entre empresas

Sempre validar a relação:

``` text
empresaId + setorId
```

Exemplo:

``` text
Setor 10 pertence à Empresa 2.
```

A requisição:

``` http
GET /api/v1/empresas/1/setores/10
```

não poderá retornar o setor como se pertencesse à Empresa 1.

Criar teste específico para isso.

------------------------------------------------------------------------

# 20. Paginação e filtros básicos

Utilizar `Pageable` nas listagens principais.

Exemplos:

``` http
GET /api/v1/empresas?page=0&size=20
```

``` http
GET /api/v1/empresas/1/setores?page=0&size=20
```

Quando apropriado, permitir filtros básicos por:

``` text
ativo
nome
codigo
```

Não criar mecanismo avançado de filtros nesta change.

------------------------------------------------------------------------

# 21. Tratamento global de exceções

Criar tratamento centralizado com:

``` java
@RestControllerAdvice
```

Criar exceções específicas quando necessário, por exemplo:

``` text
ResourceNotFoundException
BusinessException
DuplicateResourceException
```

Não usar `RuntimeException` genérica para representar regras conhecidas.

------------------------------------------------------------------------

# 22. Resposta padronizada de erro

Exemplo de conflito:

``` json
{
  "timestamp": "2026-09-26T15:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Já existe um setor com o código PINT nesta empresa.",
  "path": "/api/v1/empresas/1/setores"
}
```

Para validação:

``` json
{
  "status": 400,
  "message": "Dados inválidos.",
  "fields": {
    "nome": "Nome é obrigatório.",
    "codigo": "Código é obrigatório."
  }
}
```

Não expor stack trace ao cliente.

------------------------------------------------------------------------

# 23. Status HTTP

Utilizar códigos coerentes:

``` text
POST criado            → 201 Created
GET válido             → 200 OK
PUT válido             → 200 OK
PATCH válido           → 200 OK ou 204 No Content
dados inválidos        → 400 Bad Request
não encontrado         → 404 Not Found
duplicidade/conflito   → 409 Conflict
```

------------------------------------------------------------------------

# 24. Banco e Flyway

PostgreSQL será o banco principal.

Utilizar Flyway para migrations.

Criar migrations versionadas, por exemplo:

``` text
src/main/resources/db/migration/

V1__create_empresa.sql
V2__create_setor.sql
```

Adicionar constraints importantes diretamente no banco:

-   CNPJ único;
-   `empresa_id + codigo` único;
-   FKs necessárias;
-   campos obrigatórios.

Não depender de:

``` properties
spring.jpa.hibernate.ddl-auto=create
```

como estratégia definitiva de schema.

------------------------------------------------------------------------

# 25. H2 para testes

Configurar perfil de teste separado.

Utilizar H2 quando apropriado nos testes automatizados.

Não misturar configuração de teste com ambiente principal.

------------------------------------------------------------------------

# 26. Testes obrigatórios --- Empresa

Testar pelo menos:

-   criar empresa válida;
-   normalizar CNPJ;
-   rejeitar razão social inválida;
-   rejeitar nome fantasia inválido;
-   rejeitar CNPJ duplicado;
-   consultar empresa existente;
-   retornar erro para empresa inexistente;
-   atualizar empresa;
-   desativar empresa;
-   reativar empresa.

------------------------------------------------------------------------

# 27. Testes obrigatórios --- Setor

Testar pelo menos:

-   criar setor válido;
-   normalizar código;
-   normalizar nome;
-   associar setor à empresa;
-   rejeitar empresa inexistente;
-   rejeitar código duplicado na mesma empresa;
-   permitir mesmo código em empresas diferentes;
-   consultar setores da empresa;
-   consultar setor específico;
-   impedir acesso através da empresa errada;
-   atualizar setor;
-   desativar setor;
-   reativar setor.

------------------------------------------------------------------------

# 28. Cenário de integração obrigatório

Criar cenário equivalente a:

``` text
Empresa A
├── PINT
├── SOLDA
└── KTL

Empresa B
├── PINT
└── LASER
```

Comprovar que:

``` text
Empresa A → PINT
Empresa B → PINT
```

é permitido.

Comprovar que:

``` text
Empresa A → PINT
Empresa A → PINT
```

é rejeitado.

Comprovar também que um setor da Empresa B não pode ser acessado através
da Empresa A.

------------------------------------------------------------------------

# 29. Fora do escopo

NÃO implementar nesta change:

-   Funcionário;
-   Peça;
-   Produção;
-   Máquina;
-   Turno;
-   Parada;
-   Importação Excel;
-   indicadores;
-   Dashboard;
-   Analytics;
-   relatório gerencial;
-   autenticação;
-   usuários;
-   permissões;
-   auditoria avançada;
-   IA;
-   RAG;
-   frontend.

Não antecipar a próxima change.

------------------------------------------------------------------------

# 30. Preparação para evolução

A arquitetura deve permitir posteriormente:

``` text
EMPRESA
   │
   └── SETOR
        │
        ├── FUNCIONÁRIOS
        ├── MÁQUINAS
        │
        └── PRODUÇÕES
              │
              ├── PEÇA
              ├── QUANTIDADE
              ├── HORÁRIOS
              └── PARADAS
```

Não implementar essas entidades agora.

------------------------------------------------------------------------

# 31. README

Criar ou atualizar o README contendo:

-   objetivo do sistema;
-   contexto da metalúrgica;
-   stack;
-   Java 17;
-   configuração do PostgreSQL;
-   como executar;
-   como executar testes;
-   migrations;
-   estrutura inicial;
-   endpoints disponíveis.

Registrar explicitamente:

> Os setores industriais são cadastráveis dinamicamente e não são
> definidos através de enum fixo.

------------------------------------------------------------------------

# 32. Testes e build obrigatórios

Antes de considerar a task concluída, executar no Windows:

``` powershell
.\gradlew.bat clean test
```

Depois:

``` powershell
.\gradlew.bat build
```

Se estiver em ambiente Unix:

``` bash
./gradlew clean test
./gradlew build
```

Todos os testes devem passar.

Se algum teste falhar:

1.  investigar a causa;
2.  corrigir;
3.  executar novamente;
4.  somente continuar quando estiver verde.

------------------------------------------------------------------------

# 33. Não mascarar problemas

É proibido:

-   apagar teste porque falhou;
-   desabilitar teste;
-   ignorar exception;
-   usar `try/catch` vazio;
-   remover validação para fazer teste passar;
-   retornar sucesso quando ocorreu erro;
-   criar dados falsos apenas para satisfazer endpoint;
-   alterar a spec para esconder implementação incorreta;
-   considerar a task concluída com build quebrado.

Corrigir a causa real.

------------------------------------------------------------------------

# 34. Critérios de aceite

A task estará concluída somente quando for possível:

1.  cadastrar uma empresa;
2.  consultar a empresa;
3.  atualizar a empresa;
4.  desativar e reativar a empresa;
5.  cadastrar `PINT / Pintura a pó`;
6.  cadastrar `SOLDA / Solda`;
7.  cadastrar `KTL / KTL`;
8.  cadastrar `LASER / Laser`;
9.  cadastrar futuramente `USIN / Usinagem` sem alteração de código;
10. consultar setores da empresa;
11. atualizar setor;
12. desativar setor;
13. reativar setor;
14. impedir código duplicado na mesma empresa;
15. permitir o mesmo código em empresas diferentes;
16. impedir acesso cruzado entre empresas;
17. migrations funcionarem corretamente;
18. testes automatizados passarem;
19. `clean test` passar;
20. `build` passar;
21. artefatos OpenSpec refletirem a implementação.

------------------------------------------------------------------------

# 35. Entrega esperada do Codex

Ao terminar, não responder apenas "implementado".

Entregar um relatório contendo:

## Arquivos

Listar:

-   arquivos criados;
-   arquivos alterados;
-   migrations;
-   testes;
-   artefatos OpenSpec.

## Implementação

Resumir:

-   entidades;
-   relacionamentos;
-   regras de negócio;
-   endpoints;
-   validações;
-   tratamento de erros.

## Testes

Informar:

-   testes executados;
-   quantidade de testes, se disponível;
-   resultado;
-   correções realizadas após falhas.

## Build

Informar resultado de:

``` text
clean test
build
```

## OpenSpec

Informar:

-   change criada;
-   specs/deltas criados ou atualizados;
-   proposal;
-   design;
-   tasks;
-   validação OpenSpec executada, caso a instalação forneça comando de
    validação.

## Limitações

Informar somente limitações reais restantes.

Não inventar funcionalidades como concluídas.

------------------------------------------------------------------------

# 36. Instrução final

Execute a task integralmente.

Fluxo obrigatório:

``` text
ANALISAR PROJETO
       ↓
ANALISAR OPENSPEC
       ↓
CRIAR/ATUALIZAR CHANGE
       ↓
VALIDAR SPECS
       ↓
IMPLEMENTAR
       ↓
TESTAR
       ↓
CORRIGIR
       ↓
TESTAR NOVAMENTE
       ↓
BUILD
       ↓
REVISAR CONTRA A SPEC
       ↓
RELATÓRIO FINAL
```

Não iniciar automaticamente Funcionários, Peças ou Produção.

Aguardar aprovação da **TASK 01 --- Fundação: Empresa e Setores
Dinâmicos** antes da próxima change.
