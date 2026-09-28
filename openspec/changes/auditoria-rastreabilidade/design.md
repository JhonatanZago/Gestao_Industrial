# Design

## Context

O backend é um monólito Spring Boot com entidades JPA, migrations Flyway, serviços transacionais e autorização baseada em JWT, empresa e escopo setorial. As operações de negócio já estão concentradas em serviços como produção e importação, enquanto o filtro de autorização resolve a permissão por rota. A importação possui entidade de lote e associa registros de produção ao lote; isso permite adicionar correlação sem criar infraestrutura distribuída.

## Goals / Non-Goals

**Goals:**

- Criar uma entidade/tabela append-only de eventos com índices para empresa, tempo, recurso, usuário, ação e correlação.
- Centralizar a criação de eventos em um serviço de auditoria que receba contexto autenticado e snapshots explícitos, com sanitização e limite de tamanho.
- Integrar o serviço próximo às operações transacionais existentes, incluindo importação e alterações sensíveis.
- Expor consultas com Specifications/repositórios paginados, DTOs separados e autorização por empresa, setor, finanças e usuários.
- Preservar tipos e permitir derivação de alterações por campo sem expor entidades JPA.

**Non-Goals:**

- Capturar todo HTTP, logs técnicos, tentativas de login ou cada leitura comum como evento de negócio.
- Frontend, timeline visual, IA/RAG, SIEM, blockchain, assinatura digital, alertas e retenção automática.
- Processamento assíncrono sem garantia de entrega ou mecanismo de arquivamento externo.

## Decisions

### Persistência e contrato

Adicionar uma migration Flyway nova para `evento_auditoria`, usando colunas textuais controladas para JSON unless a stack atual demonstrar suporte seguro e portátil a JSON nativo. Os enums persistidos serão strings estáveis: `tipo_recurso`, `acao` e `origem`. Índices cobrirão `(empresa_id, data_hora)`, `(empresa_id, tipo_recurso, recurso_id)`, `(empresa_id, usuario_id, data_hora)`, `(empresa_id, acao, data_hora)` e `correlation_id`.

Alternativa rejeitada: usar apenas logs estruturados, pois logs não são persistência de negócio nem garantem consulta/isolamento. Também não será usada uma tabela por recurso, evitando duplicação de esquema e consultas inconsistentes.

### Serviço central com snapshots explícitos

Um `AuditoriaService` receberá o contexto autenticado, tipo de recurso, id, ação, origem, before/after e metadados. Cada domínio fornecerá snapshot DTO controlado, em vez de serializar entidade JPA. Um sanitizador removerá chaves sensíveis por denylist reforçada e aplicará limite de tamanho; operações de senha usarão evento especializado sem snapshot.

Alternativas rejeitadas: AOP genérico, porque não captura com segurança o estado anterior e pode auditar tentativas que sofreram rollback; decorator de controller, porque perde alterações oriundas de importação/processamento interno.

### Transação e contexto

As chamadas de auditoria ocorrerão dentro dos métodos `@Transactional` dos serviços de negócio, depois que a alteração válida estiver preparada e antes do commit. Falha ao persistir evento propagará exceção para reverter a transação. O ator será extraído do contexto JWT/serviço de segurança; `usuarioId` vindo do body será ignorado. Operações sem humano terão usuário nulo e origem `SISTEMA`.

### Integração com importação

O evento do lote será emitido quando o estado da importação for persistido, e os eventos de produção criados no loop receberão origem `IMPORTACAO_EXCEL` e o id da importação como correlation ID. O nome original será normalizado para basename seguro e limitado. A auditoria de validação/rejeição não afirmará criação de produção.

### Consultas e autorização

Um serviço de consulta construirá filtros paginados com tamanho máximo e ordenação descendente. A empresa do path será validada pelo filtro atual; o serviço repetirá o predicado de empresa e escopo setorial para impedir bypass interno. Permissões novas serão semeadas com compatibilidade aos perfis existentes, separando auditoria geral, financeira e de usuários/permissões. DTOs de resumo/detalhe serão os únicos contratos públicos.

### Retenção e imutabilidade

Não haverá endpoints de alteração ou exclusão de eventos. Esta entrega não apagará dados automaticamente; a futura política de retenção/arquivamento será documentada e tratada separadamente.

## Risks / Trade-offs

- [Snapshots divergentes entre serviços] → usar builders/DTOs explícitos por recurso e testes de contrato para before/after.
- [Falha de auditoria bloquear operações legítimas] → manter payloads pequenos, persistência síncrona na mesma transação e testes de rollback; a consistência é preferida à perda silenciosa.
- [Vazamento financeiro ou sensível] → permissões por campo/domínio, sanitização defensiva, testes que pesquisam nomes proibidos e nunca serialização de entidade.
- [Crescimento da tabela] → índices voltados às consultas obrigatórias, paginação máxima e registrar como decisão futura a retenção/arquivamento.
- [Volume de importação] → limitar snapshots, reutilizar um correlation ID por lote e evitar payload completo por linha; medir teste sintético antes de liberar.

## Migration Plan

1. Adicionar migration nova, entidades, enums e repositórios sem alterar migrations anteriores.
2. Implementar serviço/sanitização e integrar primeiro produção/importação, depois cadastros e domínios sensíveis.
3. Adicionar permissões e endpoints de consulta com testes de isolamento, setor e financeiro.
4. Executar `./gradlew.bat clean test`, `./gradlew.bat build` e validação estrita do OpenSpec.
5. Rollback de aplicação: desabilitar as integrações/rotas em release compatível; rollback de banco somente antes de dados de auditoria serem necessários, preferindo manter a tabela para preservar evidências.

## Open Questions

- Nenhuma questão bloqueante permanece para o planejamento; detalhes de nomes físicos de colunas JSON podem ser definidos na implementação mantendo o contrato dos snapshots.
