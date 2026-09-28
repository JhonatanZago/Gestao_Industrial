## Purpose

Aplicar as fronteiras de segurança do ERP ao assistente, garantindo isolamento
por empresa e setor, proteção de dados financeiros e de auditoria, resistência a
prompt/tool injection, limites operacionais e execução sem escrita.

## ADDED Requirements

### Requirement: Autorização deve preceder toda consulta ou recuperação

O sistema SHALL derivar identidade, empresa, perfil, permissões e setores do
contexto autenticado. A autorização SHALL ser verificada antes de cada tool e
antes de cada busca de documento; filtros fornecidos pelo usuário ou pelo
modelo não poderão ampliar o escopo.

#### Scenario: Usuário tenta consultar outra empresa

- **WHEN** o usuário autenticado envia uma rota de empresa fora do seu escopo
- **THEN** o sistema retorna `SEM_PERMISSAO`/`AI_PERMISSION_DENIED` sem chamar
  provider, tool ou vector store para essa empresa

#### Scenario: Usuário possui somente alguns setores

- **WHEN** uma pergunta solicita todos os setores, mas o usuário tem escopo
  limitado
- **THEN** a consulta usa somente os setores autorizados e informa o escopo
  parcial, sem revelar a existência ou os valores dos demais setores

### Requirement: Dados sensíveis devem exigir permissões específicas

Valor industrial/financeiro SHALL exigir `FINANCEIRO_INDUSTRIAL_VISUALIZAR`.
Consultas de auditoria SHALL exigir a permissão equivalente do módulo de
auditoria. Falha de uma permissão não poderá ser contornada por RAG,
comparação, conversa ou reformulação da pergunta.

#### Scenario: Financeiro sem permissão

- **WHEN** o usuário pergunta sobre valor industrial sem
  `FINANCEIRO_INDUSTRIAL_VISUALIZAR`
- **THEN** o sistema retorna `SEM_PERMISSAO`/`AI_PERMISSION_DENIED` e não
  envia números financeiros ao provider ou à resposta

#### Scenario: Auditoria sem permissão

- **WHEN** o usuário sem permissão de auditoria pede eventos ou trilhas
- **THEN** o sistema recusa a consulta sem revelar contagens, nomes, ids ou
  existência de eventos protegidos

### Requirement: Instruções não confiáveis não podem mudar a política

Perguntas, documentos e resultados de tools SHALL ser tratados como entradas
não confiáveis. Prompt injection, tool injection, tentativa de alterar o
system prompt, pedido de segredo ou pedido de chamada não catalogada SHALL ser
ignorados ou rejeitados.

#### Scenario: Pergunta tenta liberar SQL ou permissão

- **WHEN** a pergunta instrui o assistente a ignorar autorização, executar SQL,
  revelar prompt ou usar uma tool administrativa
- **THEN** o sistema mantém as políticas do backend e não executa a operação

#### Scenario: Resultado de tool contém instrução maliciosa

- **WHEN** um campo textual retornado por uma fonte contém instruções para
  chamar outra tool ou exfiltrar dados
- **THEN** o conteúdo é tratado como dado, não como comando, e o catálogo não é
  expandido

### Requirement: O assistente deve operar dentro de limites defensivos

O sistema SHALL limitar pergunta, contexto, tokens, tempo total, chamadas de
tool, resultados, documentos recuperados, upload e uso por usuário/empresa.
Retry SHALL ocorrer somente para erros transitórios e dentro de uma quantidade
limitada. Não haverá loop autônomo sem limite ou cache compartilhado inseguro.

#### Scenario: Usuário excede rate limit

- **WHEN** o usuário ou empresa excede o limite configurado de requisições
- **THEN** o sistema retorna `AI_RATE_LIMIT`, não inicia novas tools e não
  revela dados da requisição bloqueada

#### Scenario: Contexto excede o limite

- **WHEN** tools e documentos autorizados ultrapassam o limite de contexto
- **THEN** o sistema reduz de forma determinística ou retorna estado seguro,
  sem descartar filtros de segurança para caber no prompt

### Requirement: Segredos e dados de IA devem ter exposição mínima

Credenciais SHALL vir apenas de ambiente, secret manager ou configuração
externa. Elas SHALL NOT ser versionadas, logadas, retornadas por health/status
ou enviadas ao modelo. Telemetria SHALL preferir metadados mínimos e não
persistir prompts, respostas completas ou chain-of-thought por padrão.

#### Scenario: Segredo está ausente

- **WHEN** a feature está habilitada, mas a credencial necessária não existe
- **THEN** o sistema inicia, marca o provider como indisponível e retorna erro
  operacional seguro sem imprimir o valor ausente nem qualquer segredo

#### Scenario: Health/status é consultado

- **WHEN** um administrador autorizado consulta o estado da integração de IA
- **THEN** a resposta informa habilitação, conectividade classificada, limites e
  versão lógica, mas nunca URL secreta, token, prompt ou conteúdo de usuário

### Requirement: A integração não pode executar escrita no ERP

Todos os adapters do assistente SHALL expor somente operações de consulta,
indexação controlada de documentos e metadados administrativos autorizados.
Nenhuma tool de produção, meta, parada, usuário ou auditoria poderá alterar o
ERP por meio do assistente.

#### Scenario: Provider pede uma operação mutável

- **WHEN** o provider devolve uma chamada para criar, editar, remover ou aprovar
  um registro
- **THEN** a chamada é recusada antes da execução e o evento é classificado
  como tentativa de operação não suportada
