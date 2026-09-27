# Spec Delta

## Purpose

Permite administrar funcionários identificados pelos códigos empresariais usados nas planilhas, mantendo vínculo obrigatório com uma Empresa, histórico por exclusão lógica e isolamento entre organizações.

## ADDED Requirements

### Requirement: Employee registration and identity

The system SHALL allow a client to create, view, list with pagination, update, activate, and deactivate Funcionários within an Empresa scope. Each Funcionário MUST belong to exactly one Empresa, MUST have a non-blank String code and name, and MUST preserve the code as entered after trimming surrounding whitespace, including leading zeros.

#### Scenario: Create an employee with leading zeros
- **WHEN** a client submits employee code ` 00147 ` and a valid name for an existing Empresa
- **THEN** the system SHALL store and return the code as `00147`, create the employee active, and associate it with that Empresa

#### Scenario: Reject invalid employee data
- **WHEN** a client submits a blank code, blank name, or an unknown Empresa identifier
- **THEN** the system SHALL return a validation or not-found error and SHALL NOT create an employee

### Requirement: Employee code uniqueness per Empresa

The system SHALL reject duplicate employee codes within the same Empresa after normalization, while allowing the same code in different Empresas. This uniqueness SHALL be protected by both business validation and a database constraint on `(empresa_id, codigo)`.

#### Scenario: Duplicate employee code in one Empresa
- **WHEN** a client attempts to register or update an employee to code `00147` already used by the same Empresa
- **THEN** the system SHALL return conflict and preserve the existing employee records

#### Scenario: Same employee code in different Empresas
- **WHEN** two different Empresas each register employee code `00147`
- **THEN** the system SHALL accept both records and keep each employee scoped to its own Empresa

### Requirement: Employee search and lifecycle

The system SHALL support paginated employee listings with optional filters for active status, code, and name. Normal API operations SHALL use logical activation/deactivation and SHALL NOT physically delete employees.

#### Scenario: Search employees by code or name
- **WHEN** a client requests the Empresa employee collection with a code or name filter and pagination parameters
- **THEN** the system SHALL return only matching employees in a paginated response

#### Scenario: Deactivate and reactivate an employee
- **WHEN** a client invokes the scoped deactivate or activate operation for an existing employee
- **THEN** the system SHALL change only the active status and preserve the employee record and its code

### Requirement: Employee Empresa isolation

The system SHALL validate the Empresa-to-Funcionário relationship on every nested employee operation. An employee belonging to Empresa B MUST NOT be returned, updated, activated, or deactivated through Empresa A's URL scope.

#### Scenario: Cross-company employee access
- **WHEN** a client requests an employee ID belonging to Empresa B through Empresa A's endpoint
- **THEN** the system SHALL return not found and SHALL NOT disclose or modify the employee

### Requirement: Employee API contract

The system SHALL expose employee operations under `/api/v1/empresas/{empresaId}/funcionarios`, return DTOs rather than persistence entities, and use 201 for creation, 200 or 204 for successful changes, 400 for invalid input, 404 for missing scoped resources, and 409 for duplicate codes.

#### Scenario: Create and retrieve an employee through the API
- **WHEN** a client posts a valid employee request and then retrieves the returned resource in the same Empresa scope
- **THEN** the API SHALL return a DTO with the normalized code, Empresa identity, active status, and timestamps
