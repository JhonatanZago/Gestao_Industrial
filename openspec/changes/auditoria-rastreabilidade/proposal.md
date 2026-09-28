# Proposal

## Why

As alterações de produção, cadastros, metas, valores industriais, usuários e importações ainda não possuem uma trilha de negócio persistente que identifique ator, contexto, origem e valores antes/depois. Isso dificulta explicar indicadores, investigar erros e comprovar isolamento entre empresas; após a estabilização das TASKS 01–11, o sistema precisa registrar essas mudanças de forma consistente e segura.

## What Changes

- Criar eventos de auditoria append-only persistidos em nova tabela Flyway, com empresa, ator autenticado, recurso, ação, origem, data/hora, correlation ID e snapshots controlados.
- Integrar a auditoria às operações de criação, alteração, ativação/desativação, alterações de senha/permissão, valores industriais e importação de produção, mantendo evento e alteração de negócio na mesma transação.
- Sanitizar e limitar snapshots before/after; nunca persistir senha, hash, tokens, secrets, credenciais ou cabeçalhos de autenticação.
- Expor consulta paginada da auditoria e histórico por recurso, com filtros, ordenação, isolamento por empresa e escopo setorial quando aplicável.
- Adicionar permissões para consulta geral, financeira e de usuários/permissões, sem criar caminho alternativo para vazamento de dados.
- Associar registros criados por importação ao evento de lote por `correlationId`, origem `IMPORTACAO_EXCEL` e metadados sanitizados do arquivo.
- Cobrir rollback transacional, imutabilidade da consulta, isolamento, permissões, paginação e ausência de dados sensíveis em testes.
- Documentar que retenção/arquivamento automático, frontend, IA/RAG e auditoria indiscriminada de cada requisição permanecem fora do escopo.

## Capabilities

### New Capabilities

- `auditoria`: persistência append-only de eventos de negócio, ator, empresa, ação, origem, snapshots seguros e consistência transacional.
- `historico-alteracoes`: consultas paginadas e histórico por recurso com filtros, ordenação, isolamento e permissões específicas.
- `rastreabilidade`: correlação entre importações, lotes e registros derivados, incluindo origem, metadados sanitizados e correlation ID.

### Modified Capabilities

- Nenhuma capability formal existente foi encontrada pelo inventário do OpenSpec; as TASKS 01–11 permanecem sem alteração de requisitos.

## Impact

- Backend Spring Boot: novas entidades, enums, repositórios, serviços, DTOs, recursos HTTP, permissões e migration Flyway.
- Serviços existentes de produção, paradas, cadastros, metas, tempos padrão, valores industriais, usuários/permissões e importação de Excel deverão emitir eventos próximos às operações de negócio.
- Segurança: reutilização do JWT e do `AuthorizationService`/`SectorScopeService`, sem aceitar `usuarioId` como autoria enviada pelo cliente.
- Banco: nova tabela `evento_auditoria` e índices por empresa/data, recurso, usuário, ação e correlation ID; nenhuma migration existente será editada.
- Relatórios da TASK 11 não terão seus cálculos alterados; apenas poderão utilizar a trilha futuramente.
