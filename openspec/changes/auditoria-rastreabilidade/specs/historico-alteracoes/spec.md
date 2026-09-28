# Spec Delta

## Purpose

Esta capability oferece consultas seguras e paginadas para investigar eventos de auditoria e reconstruir o histórico de um recurso sem expor dados de outra empresa, setor ou domínio financeiro não autorizado.

## ADDED Requirements

### Requirement: Consulta paginada de auditoria
O sistema SHALL expor `GET /api/v1/empresas/{empresaId}/auditoria` com paginação obrigatória, tamanho máximo controlado, ordenação padrão por `dataHora DESC` e filtros opcionais por intervalo, usuário, tipo de recurso, recurso, ação, origem e `correlationId`.

#### Scenario: Lista filtrada e paginada
- **WHEN** um usuário autorizado consulta auditoria com período, ação e página informados
- **THEN** o sistema retorna somente eventos da empresa autorizada, na ordem decrescente de data, com metadados de paginação e sem exceder o tamanho máximo

#### Scenario: Paginação inválida é rejeitada
- **WHEN** o cliente solicita tamanho zero, tamanho acima do limite ou parâmetros de período inválidos
- **THEN** o sistema rejeita a consulta com erro de validação e não executa uma consulta sem limite

### Requirement: Histórico por recurso
O sistema SHALL expor `GET /api/v1/empresas/{empresaId}/auditoria/{tipoRecurso}/{recursoId}` com paginação e a mesma proteção de escopo da consulta geral. O sistema MAY expor consulta por evento individual, mas ela SHALL validar empresa e permissões antes de retornar detalhes.

#### Scenario: Histórico de registro de produção
- **WHEN** um usuário autorizado solicita o histórico de `REGISTRO_PRODUCAO` 100
- **THEN** o sistema retorna eventos daquele recurso, incluindo ações, origem, ator e alterações de campos derivadas de before/after

#### Scenario: Recurso inexistente ou de outra empresa
- **WHEN** o cliente solicita um recurso que não pertence à empresa do caminho ou não pode ser acessado pelo escopo do usuário
- **THEN** o sistema não revela seus eventos e responde com erro de acesso ou recurso não encontrado conforme a política de segurança

### Requirement: Autorização e escopo da consulta
O sistema SHALL reutilizar a identidade autenticada e as permissões existentes para garantir que administrador acesse apenas sua empresa, gestor respeite os setores autorizados e consultas de usuários/permissões e dados financeiros exijam permissões específicas. O sistema SHALL ignorar qualquer `usuarioId` de autoria enviado pelo cliente.

#### Scenario: Isolamento entre empresas
- **WHEN** um usuário da Empresa A tenta listar ou consultar evento da Empresa B
- **THEN** o sistema bloqueia a operação e não retorna dados, snapshots ou metadados da Empresa B

#### Scenario: Gestor não acessa setor fora do escopo
- **WHEN** um gestor autorizado apenas para Pintura consulta eventos de um recurso da Laser
- **THEN** a lista, os filtros e o histórico individual bloqueiam o acesso ao evento da Laser

#### Scenario: Financeiro sem permissão não vê valores
- **WHEN** um usuário sem permissão financeira consulta um evento que contém mudança de valor industrial
- **THEN** o sistema omite ou bloqueia o snapshot financeiro conforme a política definida, sem permitir obtenção por filtro, histórico ou evento individual

### Requirement: Resposta de auditoria sem entidade persistente
O sistema SHALL retornar DTOs de resumo e detalhe, incluindo alterações por campo com `campo`, `valorAnterior` e `valorNovo` quando deriváveis, e SHALL preservar tipos de data, enum, nulos, acentos e BigDecimal de forma compatível com a API. Entidades JPA não SHALL ser expostas diretamente.

#### Scenario: Alteração de campo é derivada
- **WHEN** um evento possui `quantidade` 500 no before e 550 no after
- **THEN** a resposta de detalhe inclui a alteração de campo com valores anterior e novo sem exigir que o cliente desserialize o snapshot bruto
