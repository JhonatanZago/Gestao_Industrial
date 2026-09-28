# Tasks

## 1. Preparação e contrato

- [x] 1.1 Executar `./gradlew.bat clean test` antes das alterações e registrar a linha de base verde para as TASKS 01–11.
- [x] 1.2 Revisar os serviços transacionais de produção, paradas, cadastros, metas, tempos padrão, valores, usuários/perfis e importação; documentar no código os pontos de emissão de evento e confirmar que nenhum controller será a única integração.
- [x] 1.3 Consolidar os enums estáveis de `TipoRecursoAuditoria`, `AcaoAuditoria` e `OrigemAuditoria`, incluindo ações de senha/permissão, e cobrir mapeamento por teste unitário.

## 2. Persistência dos eventos

- [x] 2.1 Criar migration Flyway nova para `evento_auditoria` com empresa, ator opcional, snapshot do nome, recurso, ação, origem, data/hora, before/after, metadados e correlation ID; verificar aplicação em banco de teste sem alterar migrations anteriores.
- [x] 2.2 Adicionar índices de consulta por empresa/data, recurso, usuário/data, ação/data e correlation ID; verificar pelo schema/migration que os índices e limites de colunas estão presentes.
- [x] 2.3 Criar entidade append-only, repository e conversores de snapshot sem endpoints de escrita/edição/exclusão; verificar persistência e leitura de eventos com null, enum, data, BigDecimal e acentos.
- [x] 2.4 Documentar no README a tabela, a política append-only e a ausência de retenção automática nesta task; verificar que a documentação não promete endpoints inexistentes.

## 3. Serviço central e segurança de snapshots

- [x] 3.1 Implementar contexto de auditoria derivado do JWT/segurança atual com empresa e ator autenticado, ignorando `usuarioId` recebido no body; verificar com teste que operação sem humano usa usuário nulo e origem `SISTEMA`.
- [x] 3.2 Implementar snapshots DTO/controlados com allowlist por recurso, sanitização defensiva de chaves sensíveis e limite de tamanho; verificar que entidades JPA não são serializadas integralmente.
- [x] 3.3 Implementar evento especializado de `ALTERACAO_SENHA` sem senha ou hash e proteção equivalente para tokens, secrets, Authorization, cookies, credenciais e chaves financeiras indevidas; verificar por teste de conteúdo persistido e resposta.
- [x] 3.4 Implementar `AuditoriaService` síncrono e transacional, com falha de auditoria propagada para rollback da alteração; verificar o cenário de erro antes do commit sem evento de sucesso falso.

## 4. Auditoria de produção e paradas

- [x] 4.1 Integrar criação, alteração e ativação/desativação de `RegistroProducao` com before/after, origem manual/API e contexto setorial; verificar quantidade 500→550, ator, empresa, recurso e ação em teste de integração.
- [x] 4.2 Integrar criação, alteração e ativação/desativação de `ParadaProducao`, incluindo vínculo com produção e setor derivado; verificar que operação rejeitada não cria evento.
- [x] 4.3 Integrar eventos de produção e parada à origem correta quando acionados por processamento interno; verificar distinção entre `MANUAL`, `API` e `SISTEMA`.
- [x] 4.4 Atualizar testes e documentação dos endpoints de produção/paradas para explicitar que eventos são emitidos somente após a operação efetivada.

## 5. Auditoria de cadastros e indicadores operacionais

- [x] 5.1 Integrar eventos de funcionário, peça, máquina e turno, incluindo criação, alteração e ativação/desativação; verificar snapshots mínimos e isolamento por empresa.
- [x] 5.2 Integrar metas de produção e tempos padrão, preservando valor, vigência e campos relevantes em before/after; verificar mudança de meta 20.000→22.000 e alteração de vigência.
- [x] 5.3 Integrar valores industriais com eventos sensíveis de criação/encerramento/alteração, sem mudar os cálculos da TASK 11; verificar nova vigência R$ 0,85→R$ 0,90 e controle financeiro.
- [x] 5.4 Adicionar testes de acentos, nulos, enums, datas e BigDecimal nos snapshots desses cadastros e atualizar a documentação de campos auditados.

## 6. Usuários, perfis e permissões

- [x] 6.1 Integrar criação, alteração, ativação/desativação e reset de usuários com ator autenticado e alvo correto; verificar que usuário inativado posteriormente continua identificável pelo snapshot mínimo.
- [x] 6.2 Integrar concessão/remoção de perfis e permissões como `ALTERACAO_PERMISSAO`, incluindo exemplo `FINANCEIRO_INDUSTRIAL_VISUALIZAR`; verificar ator, alvo, data e before/after autorizado.
- [x] 6.3 Adicionar permissões `AUDITORIA_VISUALIZAR`, `AUDITORIA_FINANCEIRA_VISUALIZAR` e `AUDITORIA_USUARIOS_VISUALIZAR` (ou modelo equivalente coerente) aos seeds sem quebrar perfis existentes; verificar autorização por perfil.
- [x] 6.4 Adicionar testes que pesquisem persistência e respostas por `password`, `senha`, `senhaHash`, `token`, `secret`, `authorization` e credenciais; verificar ausência em eventos de usuários, senhas e permissões.

## 7. Importação e rastreabilidade

- [x] 7.1 Integrar evento de lote ao fluxo de validação/importação Excel com contagens, setor, status, usuário, data e nome de arquivo sanitizado; verificar o cenário 100 linhas, 98 sucessos e 2 erros.
- [x] 7.2 Propagar `correlationId` da importação e origem `IMPORTACAO_EXCEL` aos registros de produção criados pelo lote; verificar que registros de uma importação são localizáveis sem logs técnicos.
- [x] 7.3 Garantir que validação/rejeição sem persistência de produção não emita eventos de criação de produção; verificar que falha parcial/rollback não deixa lote concluído incorretamente.
- [x] 7.4 Atualizar README com o contrato de correlação, metadados permitidos e exemplos sem caminhos locais; verificar nomes de arquivo sanitizados e ausência de conteúdo sensível.

## 8. Consultas e DTOs de auditoria

- [x] 8.1 Criar `AuditoriaFiltro`, `EventoAuditoriaResumoResponse`, `EventoAuditoriaDetalheResponse` e `AlteracaoCampoResponse` sem expor entidade JPA; verificar tipos de data, enum, nulos, acentos e BigDecimal.
- [x] 8.2 Implementar consulta paginada `GET /api/v1/empresas/{empresaId}/auditoria` com filtros de período, usuário, recurso, ação, origem e correlation ID, tamanho máximo e `dataHora DESC`; verificar paginação, ordenação e filtros combinados.
- [x] 8.3 Implementar histórico paginado `GET /api/v1/empresas/{empresaId}/auditoria/{tipoRecurso}/{recursoId}` e evento individual se necessário, derivando diferenças por campo; verificar histórico do registro 100 e recurso inexistente.
- [x] 8.4 Implementar limites e validações de consulta para impedir página/tamanho inválidos ou consultas sem limite; verificar erro de validação e ausência de consulta irrestrita.

## 9. Isolamento, escopo e proteção de dados

- [x] 9.1 Integrar rotas de auditoria ao filtro/permissões sem permitir bypass por lista, filtro ou detalhe; verificar Empresa A não acessa eventos da Empresa B.
- [x] 9.2 Aplicar escopo setorial a recursos com setor e verificar que gestor de Pintura não acessa eventos de Laser por nenhum endpoint ou filtro.
- [x] 9.3 Aplicar mascaramento/bloqueio financeiro e administrativo conforme permissões específicas; verificar que usuário sem permissão não obtém valores industriais nem histórico de usuários/permissões.
- [x] 9.4 Verificar que eventos não possuem PUT/PATCH/DELETE comum e que nenhuma rota de auditoria aceita autoria informada pelo cliente.

## 10. Integração e entrega

- [x] 10.1 Criar testes de integração cobrindo produção, meta, valor industrial, senha, permissão, importação, rollback, correlação, usuário inativado e recurso inativado.
- [x] 10.2 Criar teste de volume sintético de auditoria/importação dentro dos limites definidos, verificando tempo e memória aceitáveis sem benchmark complexo.
- [x] 10.3 Executar `./gradlew.bat clean test` e confirmar regressão verde das TASKS 01–11 mais os novos testes de auditoria.
- [x] 10.4 Executar `./gradlew.bat build`, revisar migration/contratos/documentação e executar `openspec validate auditoria-rastreabilidade --type change --strict`; verificar que todos os artefatos e tarefas estão completos.
