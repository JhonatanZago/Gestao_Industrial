# Spec Delta

## Purpose

Restringir dados e operações de gestores e usuários operacionais aos setores autorizados, aplicando o escopo dentro das consultas para evitar vazamentos diretos ou indiretos em analytics.

## ADDED Requirements

### Requirement: Associação usuário-setor
O sistema MUST permitir associar usuários não administrativos a setores da própria empresa e MUST validar que cada setor autorizado pertence à empresa do usuário.

#### Scenario: Gestor com setores
- **WHEN** gestor recebe Pintura e KTL como setores autorizados
- **THEN** o sistema MUST permitir operações nesses setores e MUST negar Laser

#### Scenario: Setor de outra empresa
- **WHEN** administrador tenta associar setor de empresa diferente
- **THEN** o sistema MUST rejeitar a associação

### Requirement: Escopo aplicado na consulta
Consultas de produção, funcionários, importações, metas, desempenho, analytics e financeiro MUST aplicar o escopo setorial antes de formar o resultado; filtros solicitando setor não autorizado MUST retornar 403 ou erro de acesso consistente.

#### Scenario: Analytics sem vazamento
- **WHEN** gestor consulta analytics geral com acesso apenas a Pintura e KTL
- **THEN** o resultado MUST excluir Solda e Laser sem depender de filtragem posterior no JSON

#### Scenario: Setor explícito proibido
- **WHEN** gestor de Pintura envia `setorId` de Laser
- **THEN** o sistema MUST retornar 403 e MUST não retornar dados de Laser

### Requirement: Administrador dentro da empresa
Administrador MUST possuir escopo global dos setores da própria empresa sem associações individuais obrigatórias, mas MUST continuar impedido de acessar setores de outra empresa.

#### Scenario: Administrador consulta setores próprios
- **WHEN** administrador consulta qualquer setor da própria empresa
- **THEN** o sistema MUST permitir conforme a permissão funcional

#### Scenario: Administrador cruza empresa
- **WHEN** administrador usa token válido da empresa A para consultar empresa B
- **THEN** o sistema MUST negar o acesso
