# Design

## Context

A TASK 15 propõe um assistente de linguagem natural para o ERP industrial. O
backend já possui Services de analytics, paradas, metas, desempenho, valor
industrial, qualidade, auditoria e segurança por empresa/setor, mas não possui
uma camada que transforme perguntas em consultas controladas nem um pipeline
para documentos internos.

A mudança cruza contrato HTTP, autorização, integração com provedores de IA,
grounding, observabilidade e eventual persistência de documentos/chunks. Por
isso, o desenho precisa separar claramente fatos estruturados do ERP e
conhecimento textual, mantendo a aplicação operável sem credenciais ou serviço
externo de IA.

## Goals / Non-Goals

### Goals

- Disponibilizar um endpoint read-only de perguntas com resposta estruturada e
  status explícito.
- Reutilizar os Services existentes por meio de um catálogo pequeno e tipado de
  tools, sempre depois da autorização de empresa, setor e permissão.
- Permitir adapters para chat, embeddings e vector store sem acoplar o domínio a
  um fornecedor específico.
- Garantir que números, períodos, avisos e conclusões estejam sustentados por
  resultados de tools ou trechos documentais autorizados.
- Isolar empresa, setor, dados financeiros, auditoria e documentos em todos os
  caminhos de consulta e recuperação.
- Oferecer ingestão textual validada, reindexação determinística e testes
  offline com fakes/mocks.
- Manter a IA desabilitada por padrão e preservar `clean test`/`build` sem
  dependência de API paga, segredo ou infraestrutura vetorial obrigatória.

### Non-Goals

- Previsões, manutenção preditiva, recomendações autônomas ou tomada de decisão
  operacional.
- Escrita no ERP, execução de SQL livre, acesso direto do modelo ao banco ou
  agente autônomo com ferramentas irrestritas.
- Frontend, streaming de tokens, exposição de chain-of-thought ou persistência
  obrigatória de conversas completas.
- Escolher agora um provedor comercial, um modelo específico ou pgvector como
  dependência obrigatória.
- Transformar documentos em fonte para substituir os números atuais do ERP.

## Decisions

### 1. Orquestração com contexto de requisição imutável

O controller recebe apenas a pergunta e um identificador opcional de conversa.
`empresaId`, `userId`, perfil, permissões e escopo setorial vêm do contexto
autenticado e são carregados em um objeto imutável de requisição. O orquestrador
resolve datas relativas no backend, classifica a intenção e decide entre tools
estruturadas, RAG textual ou ambos. Nenhuma autorização será delegada ao
prompt.

### 2. Catálogo explícito de tools read-only

Cada tool terá nome, schema de entrada, schema de saída, permissões necessárias,
escopo de dados e limite próprio. O catálogo inicial cobre produção,
comparações, séries temporais, setor, funcionário, peça, máquina, paradas,
metas, desempenho, valor industrial, qualidade e auditoria. Cada adapter
chamará os Services/facades existentes, ou uma porta interna bem definida
quando uma consulta ainda não tiver uma facade reutilizável.

O modelo nunca receberá conexão de banco, repositório ou SQL. O chamador da
tool valida novamente tenant, setor, período, filtros e permissão antes da
execução, mesmo que o orquestrador já tenha feito uma validação inicial.

### 3. Portas de IA e fallback operacional

Serão definidas portas equivalentes a `ChatModelGateway`,
`EmbeddingGateway` e `VectorStorePort`, com DTOs próprios para mensagens,
embeddings, documentos e resultados. Adapters de fornecedor ficam fora do
domínio. O provider configurado deve devolver saída estruturada ou erro
classificado; falhas transitórias podem ter retry limitado.

`AI_ENABLED=false` será o padrão. Sem provider, embedding ou credencial, a
aplicação inicia normalmente e o endpoint retorna `INDISPONIVEL` com código
`AI_DISABLED` ou `AI_PROVIDER_UNAVAILABLE`, sem tentar contornar a
configuração.

### 4. Grounding em duas camadas

Consultas de fatos atuais usam tools do ERP. Procedimentos, manuais e
explicações usam RAG. O contexto enviado ao modelo contém somente resultados e
trechos já filtrados, cada qual com identificador de fonte, período e
metadados. A resposta passa por validação estrutural e por uma checagem de
claims numéricos: todo número apresentado deve ser rastreável a uma fonte
retornada.

Se a validação falhar, a resposta será rejeitada, regenerada uma única vez com
o contexto corrigido ou convertida em resposta segura sem o claim não
sustentado. O sistema nunca inventará números para preencher lacunas.

### 5. Autorização antes da recuperação

O contexto de segurança é aplicado antes de cada tool e antes de cada busca
vetorial. O filtro mínimo contém `empresaId`; quando houver escopo setorial,
contém o conjunto permitido de `sectorId`. Valor industrial exige
`FINANCEIRO_INDUSTRIAL_VISUALIZAR`; auditoria exige a permissão equivalente já
usada pelo módulo de auditoria. Resultado, prompt, cache e fontes não podem
misturar empresas, setores ou usuários incompatíveis.

### 6. RAG textual com ciclo de vida explícito

A ingestão valida tamanho, extensão, nome e conteúdo extraível, rejeitando
macros, scripts e conteúdo não suportado. O texto é normalizado, dividido por
chunking determinístico e indexado com hash de conteúdo. Cada chunk carrega
empresa, documento, tipo, título, versão, setor opcional e permissões.

Uma atualização invalida chunks antigos antes de disponibilizar a nova versão.
A busca filtra tenant/setor/permissão antes do ranking e antes do modelo. A
porta de vector store pode ser implementada inicialmente por um adapter local ou
stub; pgvector e provedores externos permanecem opções de infraestrutura.

### 7. Limites e telemetria mínima

Configurações limitam tamanho da pergunta, número de chamadas de tool, tempo
total, resultados, tokens, documentos recuperados, tamanho de upload e uso por
usuário/empresa. O sistema não cria loop autônomo. A telemetria registra
provider/modelo, latência, tokens, contagem e sucesso das tools, sem secrets,
prompts completos, respostas completas ou chain-of-thought por padrão.

### 8. Persistência incremental e compatibilidade

Persistência de conversa, documentos, chunks e telemetria só será adicionada se
necessária ao fluxo. Se houver tabelas, novas migrations serão versionadas sem
editar migrations existentes e todos os novos dados carregarão tenant/escopo.
O endpoint e suas DTOs são aditivos; as APIs existentes de analytics,
relatórios, qualidade e auditoria não mudam de contrato.

## Risks / Trade-offs

- Um provider pode retornar JSON inválido ou alegações não suportadas; schemas,
  timeout, retry limitado e validação de grounding reduzem o risco, mas não
  substituem autorização backend.
- Restringir o catálogo e os limites reduz flexibilidade, porém é necessário
  para manter previsibilidade, custo e segurança em um ERP multiempresa.
- RAG com filtro de setor pode retornar poucos trechos; o status deve indicar
  ausência de dados em vez de ampliar o escopo automaticamente.
- PDF amplia utilidade, mas também aumenta risco de parser vulnerável e prompt
  injection; será opcional e só poderá ser habilitado com extractor seguro.
- Fakes offline não reproduzem todos os comportamentos de um provider real;
  contratos, testes de integração isolados e health/status devem evidenciar a
  configuração real sem tornar o build dependente dela.
- Não persistir prompts e respostas reduz observabilidade forense; a auditoria
  registrará metadados mínimos e decisões de autorização, sem conteúdo
  sensível por padrão.

## Migration Plan

1. Introduzir contratos, configurações desabilitadas e fakes sem alterar os
   endpoints existentes.
2. Adicionar catálogo de tools e o endpoint read-only, primeiro com provider
   fake e respostas determinísticas para os testes.
3. Integrar adapters reais por configuração opt-in, health/status e limites,
   mantendo o comportamento `INDISPONIVEL` quando a IA estiver desligada.
4. Adicionar ingestão e indexação de TXT/MD; habilitar PDF somente quando o
   extractor seguro estiver disponível.
5. Executar a matriz de testes de autorização, isolamento, grounding, injection,
   indisponibilidade e regressão; só então disponibilizar a feature para
   usuários autorizados.

Rollback consiste em desligar `AI_ENABLED`, desabilitar as rotas de ingestão e
   remover a configuração do adapter. As APIs existentes permanecem utilizáveis.

## Open Questions

Não há pergunta bloqueante para implementação. A escolha do provider, do
modelo, do mecanismo vetorial e da persistência de conversa pode ser decidida
na configuração/adapters, desde que preserve as portas e os contratos desta
mudança.
