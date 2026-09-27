# Spec Delta

## Purpose

Permite que cada Empresa mantenha um catálogo dinâmico e historicamente estável de motivos de parada, sem transformar causas específicas da operação em enumerações fixas no código.

## ADDED Requirements

### Requirement: Dynamic stop reason management

The system SHALL allow an Empresa to create, list with pagination and filters, view, update, activate, and deactivate Motivos de Parada. Each reason SHALL have a String code, name, optional description, optional stable high-level category, active status, and audit timestamps. Codes SHALL be unique per Empresa and SHALL be protected by business validation and database constraint.

#### Scenario: Create a stop reason
- **WHEN** a client submits code ` MAN-001 `, a name, description, and optional category for an existing Empresa
- **THEN** the system SHALL trim the code, create an active reason, and return its DTO with audit fields

#### Scenario: Reject duplicate reason within an Empresa
- **WHEN** a client submits a code already used by the same Empresa
- **THEN** the system SHALL return conflict and SHALL NOT create a duplicate reason

#### Scenario: Allow equal reason code across Empresas
- **WHEN** two Empresas register the same reason code
- **THEN** the system SHALL accept both records and keep them scoped to their respective Empresas

### Requirement: Stop reason lifecycle and isolation

The system SHALL use logical activation/deactivation for reasons and SHALL prevent a reason belonging to another Empresa from being viewed, changed, activated, or deactivated through the wrong scope. An inactive reason MUST NOT be selected for a new stop, while historical stops may continue to display it.

#### Scenario: Deactivate a reason
- **WHEN** a client deactivates a reason
- **THEN** the system SHALL preserve the record and reject its use in newly created stops

#### Scenario: Cross-company reason access
- **WHEN** a client accesses a reason ID through another Empresa's endpoint
- **THEN** the system SHALL return not found and SHALL NOT disclose or modify it
