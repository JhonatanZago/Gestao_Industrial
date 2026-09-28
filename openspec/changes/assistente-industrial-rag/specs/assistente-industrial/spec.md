## Purpose

Oferecer uma interface de perguntas em linguagem natural para fatos industriais
autorizados, com resposta estruturada, estados explícitos e comportamento
read-only mesmo quando um provider de IA estiver configurado.

## ADDED Requirements

### Requirement: Perguntas devem usar um contrato HTTP estruturado

O sistema SHALL expor `POST /api/v1/empresas/{empresaId}/assistente/perguntar`
para usuários autenticados. O corpo SHALL aceitar a pergunta e, quando
suportado, um `conversationId` opcional; o corpo SHALL NOT aceitar `userId`,
permissões, setores ou qualquer campo que altere o contexto autorizado.

#### Scenario: Pergunta válida retorna contrato de assistente

- **WHEN** um usuário autenticado envia uma pergunta não vazia para uma empresa
  dentro do seu escopo
- **THEN** o sistema retorna uma resposta estruturada contendo `status`,
  `answer` quando houver resposta, `conversationId` quando aplicável,
  `dataUsed`, `period`, `qualityData`, `warnings`, `sources` e `generatedAt`

#### Scenario: Corpo tenta forjar autorização

- **WHEN** a requisição inclui `userId`, empresa diferente, permissões ou setores
  no corpo
- **THEN** o sistema ignora esses campos ou rejeita a requisição como inválida
  e usa somente o contexto autenticado e o `empresaId` da rota

#### Scenario: Pergunta inválida é rejeitada sem chamada ao provider

- **WHEN** a pergunta está ausente, vazia, acima do limite configurado ou o
  `conversationId` tem formato inválido
- **THEN** o sistema retorna `AI_INVALID_REQUEST` com erro estruturado e não
  executa tool nem chama provider externo

### Requirement: O assistente deve resolver intenção e período no backend

O sistema SHALL reconhecer perguntas sobre produção, comparações, séries
temporais, setores, funcionários, peças, máquinas, paradas, metas,
desempenho, valor industrial, qualidade e auditoria. Datas relativas SHALL ser
resolvidas com o relógio do backend e a ambiguidade SHALL ser explicitada ao
usuário.

#### Scenario: Período relativo é convertido em intervalo determinístico

- **WHEN** o usuário pergunta pelos dados de "ontem", "esta semana" ou outro
  período relativo
- **THEN** o sistema converte a expressão para datas explícitas segundo o
  timezone/configuração do backend, inclui o período na resposta e envia o
  intervalo resolvido à tool

#### Scenario: Pergunta ambígua pede clarificação

- **WHEN** a pergunta não identifica a métrica, período, entidade ou comparação
  necessários para uma consulta segura
- **THEN** o sistema retorna `PRECISA_CLARIFICACAO`, descreve o mínimo que falta
  e não inventa filtros nem executa consulta fora do escopo

### Requirement: Estados de negócio devem ser explícitos

O campo `status` SHALL usar pelo menos `RESPONDIDO`, `SEM_DADOS`,
`SEM_PERMISSAO`, `PRECISA_CLARIFICACAO` e `INDISPONIVEL`. Falhas técnicas
classificadas SHALL usar códigos coerentes, incluindo `AI_DISABLED`,
`AI_PROVIDER_UNAVAILABLE`, `AI_TIMEOUT`, `AI_RATE_LIMIT`,
`AI_PERMISSION_DENIED`, `AI_NO_DATA` e `AI_INVALID_REQUEST`.

#### Scenario: Consulta autorizada sem registros

- **WHEN** todas as autorizações passam, mas as tools não encontram dados no
  período solicitado
- **THEN** o sistema retorna `SEM_DADOS`/`AI_NO_DATA`, informa o período
  consultado e não preenche a resposta com valores estimados

#### Scenario: Provider indisponível

- **WHEN** a feature está habilitada, mas o provider falha, expira ou excede
  limite após as tentativas permitidas
- **THEN** o sistema retorna `INDISPONIVEL` com o código técnico apropriado e
  não expõe credenciais, payloads secretos ou detalhes internos do provider

### Requirement: O fluxo deve permanecer read-only

O assistente SHALL consultar apenas tools declaradas e SHALL NOT criar,
alterar, excluir ou aprovar registros do ERP, executar SQL livre, acessar
diretamente o banco ou expor chain-of-thought.

#### Scenario: Usuário solicita uma alteração operacional

- **WHEN** a pergunta pede para registrar produção, editar meta, excluir uma
  parada ou executar qualquer ação de escrita
- **THEN** o sistema informa que a operação não é suportada, retorna estado
  seguro e não chama nenhuma operação mutável

#### Scenario: Resposta não expõe raciocínio interno

- **WHEN** o provider retorna conteúdo identificado como chain-of-thought ou
  instruções internas
- **THEN** o sistema remove esse conteúdo e entrega somente resposta,
  evidências, avisos e fontes permitidos pelo contrato

### Requirement: Conversas são opcionais e não podem ampliar escopo

Se conversas forem persistidas, cada conversa SHALL ser vinculada a empresa,
usuário e escopo autorizado. Um `conversationId` inexistente, expirado ou de
outro escopo SHALL não permitir recuperação de contexto.

#### Scenario: Conversation ID pertence a outro usuário ou empresa

- **WHEN** o usuário envia um identificador de conversa fora do seu escopo
- **THEN** o sistema retorna `AI_PERMISSION_DENIED`/`SEM_PERMISSAO` sem revelar
  conteúdo nem metadados da conversa
