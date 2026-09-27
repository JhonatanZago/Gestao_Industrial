# Spec Delta

## Purpose

Permite administrar peças identificadas pelos códigos industriais das planilhas, aceitando códigos numéricos ou alfanuméricos, preservando seu valor textual e mantendo cada peça vinculada a uma Empresa.

## ADDED Requirements

### Requirement: Part registration and identity

The system SHALL allow a client to create, view, list with pagination, update, activate, and deactivate Peças within an Empresa scope. Each Peça MUST belong to exactly one Empresa, MUST have a non-blank String code and description, and MUST trim surrounding whitespace without converting the code to a number.

#### Scenario: Create a numeric-looking part code
- **WHEN** a client submits part code ` 0054871 ` and a valid description for an existing Empresa
- **THEN** the system SHALL store and return the code as `0054871`, create the part active, and retain it as text

#### Scenario: Accept alphanumeric and hyphenated codes
- **WHEN** a client submits codes such as `ABC-1020` or `54871-A`
- **THEN** the system SHALL accept them as String values when they satisfy the configured length and non-blank validation

#### Scenario: Reject invalid part data
- **WHEN** a client submits a blank code, blank description, or an unknown Empresa identifier
- **THEN** the system SHALL return a validation or not-found error and SHALL NOT create a part

### Requirement: Part code uniqueness per Empresa

The system SHALL reject duplicate part codes within the same Empresa after trimming, while allowing equal codes in different Empresas. This uniqueness SHALL be protected by business validation and a database constraint on `(empresa_id, codigo)`.

#### Scenario: Duplicate part code in one Empresa
- **WHEN** a client attempts to register or update part code `54871` already used by the same Empresa
- **THEN** the system SHALL return conflict and preserve the existing part records

#### Scenario: Same part code in different Empresas
- **WHEN** two different Empresas each register part code `54871`
- **THEN** the system SHALL accept both records and keep each part scoped to its own Empresa

### Requirement: Part search and lifecycle

The system SHALL support paginated part listings with optional filters for active status, code, and description. Normal API operations SHALL use logical activation/deactivation and SHALL NOT physically delete parts.

#### Scenario: Search parts by code or description
- **WHEN** a client requests the Empresa part collection with a code or description filter and pagination parameters
- **THEN** the system SHALL return only matching parts in a paginated response

#### Scenario: Deactivate and reactivate a part
- **WHEN** a client invokes the scoped deactivate or activate operation for an existing part
- **THEN** the system SHALL change only the active status and preserve the part record and its code

### Requirement: Part Empresa isolation and production independence

The system SHALL validate the Empresa-to-Peça relationship on every nested part operation. A part belonging to Empresa B MUST NOT be returned, updated, activated, or deactivated through Empresa A's URL scope. A Peça MUST NOT require a permanent association with one Setor; its production sector SHALL remain a concern of the future production record.

#### Scenario: Cross-company part access
- **WHEN** a client requests a part ID belonging to Empresa B through Empresa A's endpoint
- **THEN** the system SHALL return not found and SHALL NOT disclose or modify the part

#### Scenario: Part can be used across processes
- **WHEN** a client registers a valid part without specifying a fixed Setor
- **THEN** the system SHALL accept the part and leave future sector association available to production records

### Requirement: Part API contract

The system SHALL expose part operations under `/api/v1/empresas/{empresaId}/pecas`, return DTOs rather than persistence entities, and use 201 for creation, 200 or 204 for successful changes, 400 for invalid input, 404 for missing scoped resources, and 409 for duplicate codes.

#### Scenario: Create and retrieve a part through the API
- **WHEN** a client posts a valid part request and then retrieves the returned resource in the same Empresa scope
- **THEN** the API SHALL return a DTO with the preserved code, Empresa identity, active status, and timestamps
