# Proposal

## Why

O ERP já possui analytics, relatórios, qualidade de dados, auditoria,
autenticação e escopo por empresa/setor, mas ainda não oferece uma forma
controlada de consultar esses fatos em linguagem natural. A TASK 15 cria um
assistente read-only que transforma perguntas em consultas estruturadas sobre
as mesmas Services autorizadas, sem liberar SQL, acesso direto ao banco ou
inferência de dados protegidos.

## What Changes

- Criar `POST /api/v1/empresas/{empresaId}/assistente/perguntar` com resposta
  estruturada, status explícito, período, avisos e fontes internas.
- Criar uma camada de orquestração read-only que resolva períodos relativos no
  backend, classifique perguntas e invoque apenas tools com schemas explícitos.
- Reutilizar analytics, paradas, metas, desempenho, financeiro, qualidade e
  auditoria existentes, aplicando autorização de empresa, setor e permissão
  antes de cada consulta.
- Criar portas/adapters desacoplados para chat, embeddings e vector store;
  manter a aplicação funcional com `AI_ENABLED=false` e sem credencial externa.
- Implementar grounding com evidências das tools/documentos, respostas
  determinísticas para ausência/ambiguidade/permissão e rejeição de números
  não sustentados pelas fontes retornadas.
- Preparar RAG textual controlado para documentos internos, com metadados de
  empresa/setor/permissão, ingestão validada, hash de conteúdo e recuperação
  filtrada antes do envio ao modelo.
- Impor limites de tools, timeout, tokens, resultados, documentos e uso por
  usuário/empresa; não criar agente autônomo nem operações de escrita no ERP.
- Registrar apenas telemetria minimizada de IA e auditar operações
  administrativas de documentos quando aplicável, sem secrets ou
  chain-of-thought.
- Adicionar testes offline com fakes/mocks para provider, embeddings e vector
  store, além dos cenários de isolamento, prompt injection, tool injection,
  grounding, indisponibilidade e regressão.

## Capabilities

### New Capabilities

- `assistente-industrial`: endpoint, contratos, status de resposta, resolução
  de perguntas e orquestração read-only.
- `ai-tooling`: portas de provider, tools estruturadas e integração segura com
  os Services e facades existentes.
- `ai-grounding`: fontes internas, evidências, validação de respostas,
  ausência de dados e proteção contra números inventados.
- `ai-security`: autorização real, isolamento empresarial/setorial/financeiro,
  prompt/tool injection, limites, privacidade e operação sem escrita.
- `rag-industrial`: documentos internos, chunking, embeddings, vector store,
  filtros pré-retrieval, reindexação e ciclo de vida administrativo.

### Modified Capabilities

Nenhuma capability principal existente está registrada em `openspec/specs/`.
O assistente consumirá os contratos atuais sem alterar o comportamento
obrigatório das APIs de analytics, relatórios, qualidade ou auditoria.

## Impact

- Novos pacotes de domínio/aplicação, DTOs, resources, ports e adapters de IA
  sob o backend Spring Boot existente.
- Novas configurações `AI_*` e limites operacionais, sem segredo versionado;
  a IA ficará desabilitada por padrão.
- Possíveis tabelas Flyway para documentos, chunks, conversas ou telemetria
  somente se a decisão de persistência for necessária; nenhuma migration
  existente será editada.
- Dependências externas de modelo/embeddings não serão obrigatórias para
  `clean test`, `build` ou funcionamento do ERP.
- O contrato do assistente será novo e não exigirá frontend; consumidores
  futuros receberão fontes e avisos sem acesso a prompts internos.
