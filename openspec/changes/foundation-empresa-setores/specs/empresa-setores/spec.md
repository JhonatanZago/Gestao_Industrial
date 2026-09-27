# Spec Delta

## Purpose

Estabelece a raiz organizacional do sistema industrial, permitindo administrar empresas e setores produtivos cadastráveis dinamicamente, com integridade, isolamento entre empresas e preservação do histórico.

## ADDED Requirements

### Requirement: Empresa lifecycle and data integrity

The system SHALL allow an Empresa to be created, viewed, listed with pagination, updated, activated, and deactivated. New Empresas SHALL start active, SHALL contain razão social, nome fantasia, and a normalized numeric CNPJ, and SHALL reject duplicate CNPJs. Normalized values SHALL trim surrounding whitespace, and the system SHALL preserve creation and update timestamps.

#### Scenario: Create a valid Empresa
- **WHEN** a client submits required textual fields and a valid CNPJ containing punctuation
- **THEN** the system SHALL create an active Empresa, persist the CNPJ using only digits, and return the created representation with timestamps

#### Scenario: Reject invalid or duplicate Empresa data
- **WHEN** a client submits a blank required field, an invalid CNPJ, or a CNPJ already registered
- **THEN** the system SHALL reject the request with a validation or conflict error and SHALL NOT create a second Empresa

#### Scenario: Deactivate and reactivate an Empresa
- **WHEN** an authorized API request targets an existing Empresa's deactivate or activate operation
- **THEN** the system SHALL change only its active status and return the resulting representation without physically deleting the record

### Requirement: Dynamic sector management

The system SHALL allow a client to create, view, list with pagination and basic filters, update, activate, and deactivate Setores under an Empresa. A Setor SHALL belong to exactly one Empresa and SHALL include a normalized code, a required name, an optional description, an active status, and timestamps. Sector codes SHALL be dynamic data rather than a fixed list of product-specific enum values.

#### Scenario: Create a sector for an Empresa
- **WHEN** a client submits a valid code and name for an existing Empresa
- **THEN** the system SHALL trim the code and name, normalize the code according to the defined canonical format, associate the Setor with that Empresa, and create it active

#### Scenario: Register a future sector without code changes
- **WHEN** a client submits a previously unknown sector such as `USIN` / `Usinagem`
- **THEN** the system SHALL accept and persist it without requiring a new enum, class, or business-rule deployment

#### Scenario: Reject a sector for an unknown Empresa
- **WHEN** a client submits or queries a sector using an Empresa identifier that does not exist
- **THEN** the system SHALL return a not-found error and SHALL NOT persist an orphan Setor

### Requirement: Sector uniqueness per Empresa

The system SHALL prevent equivalent active or inactive sectors from being duplicated within the same Empresa by enforcing normalized code uniqueness and logical name uniqueness scoped to the Empresa. The persistence layer SHALL enforce the code uniqueness constraint for the pair Empresa plus normalized code.

#### Scenario: Duplicate code in the same Empresa
- **WHEN** a client attempts to create or update a Setor to a code already used by the same Empresa after normalization
- **THEN** the system SHALL return a conflict error and SHALL preserve the existing sector data

#### Scenario: Same code in different Empresas
- **WHEN** two different Empresas each register a Setor with the same normalized code
- **THEN** the system SHALL accept both registrations and keep each sector associated only with its own Empresa

### Requirement: Empresa-scoped sector access

The system SHALL validate the Empresa-to-Setor relationship on every nested sector operation. A sector belonging to one Empresa MUST NOT be returned, changed, activated, or deactivated through another Empresa's URL scope.

#### Scenario: Access a sector through the correct Empresa
- **WHEN** a client requests a sector using the identifier of its owning Empresa
- **THEN** the system SHALL return or operate on that sector according to the requested action

#### Scenario: Access a sector through the wrong Empresa
- **WHEN** a client requests a sector identifier belonging to Empresa B through Empresa A's endpoint
- **THEN** the system SHALL return not found and SHALL NOT disclose or modify the sector as a member of Empresa A

### Requirement: REST API and response contracts

The system SHALL expose versioned REST endpoints for Empresa operations under `/api/v1/empresas` and nested Setor operations under `/api/v1/empresas/{empresaId}/setores`. Endpoints SHALL return DTO representations rather than persistence entities, use coherent HTTP statuses (201 for creation, 200 or 204 for successful updates, 400 for invalid input, 404 for missing resources, and 409 for conflicts), and support pagination on primary list operations with basic active/name/code filters where applicable.

#### Scenario: Paginate Empresa and Setor lists
- **WHEN** a client requests either list endpoint with `page` and `size`
- **THEN** the system SHALL return a paginated response limited to the requested page and size, applying supported filters when provided

#### Scenario: Return a standardized error
- **WHEN** validation, not-found, or uniqueness failure occurs in an API request
- **THEN** the system SHALL return a structured error containing status and message, include field details for validation errors when available, and SHALL NOT expose a stack trace

### Requirement: Persistence and operational validation

The system SHALL create the Empresa and Setor schema through versioned database migrations, including required fields, foreign key integrity, CNPJ uniqueness, and Empresa-plus-code uniqueness. Automated tests SHALL cover the critical Empresa, Setor, normalization, isolation, and duplicate scenarios, and the project SHALL provide separate test configuration using H2 while PostgreSQL remains the primary runtime database.

#### Scenario: Apply a clean database schema
- **WHEN** the application starts against an empty supported database with migrations enabled
- **THEN** the Empresa and Setor tables and their required constraints SHALL be created without relying on automatic Hibernate schema creation

#### Scenario: Validate the foundation build
- **WHEN** the project runs its clean test and build commands
- **THEN** automated tests SHALL execute successfully and the build SHALL complete without compilation or packaging errors
