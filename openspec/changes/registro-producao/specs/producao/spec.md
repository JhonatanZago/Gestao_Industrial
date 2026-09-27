# Spec Delta

## Purpose

Estrutura o primeiro registro transacional de produção industrial, conectando Empresa, Setor, Funcionário e Peça a uma quantidade e um intervalo de evento auditável, com consistência temporal e isolamento organizacional.

## ADDED Requirements

### Requirement: Production record registration

The system SHALL allow a client to create a Registro de Produção under an Empresa using a Setor, Funcionário, Peça, positive quantity, start time, end time, and optional observation. The three referenced resources MUST exist, belong to the same Empresa, and be active when the record is created. The record SHALL start active and preserve creation and update audit timestamps.

#### Scenario: Create a valid production record
- **WHEN** a client submits Empresa A's active Setor PINT, Funcionário `00147`, Peça `54871`, quantity 350, start `2026-09-26T08:14:00`, and end `2026-09-26T09:42:00`
- **THEN** the system SHALL create an active record scoped to Empresa A and return the linked resource data and a raw duration of 88 minutes

#### Scenario: Reject a resource from another Empresa
- **WHEN** a client submits a Setor, Funcionário, or Peça that exists but belongs to another Empresa
- **THEN** the system SHALL reject the request with a business error and SHALL NOT persist the production record

#### Scenario: Reject missing or inactive resources
- **WHEN** a client submits an unknown resource or a resource that is inactive
- **THEN** the system SHALL return not-found or conflict according to the condition and SHALL NOT create a new production record

### Requirement: Quantity and temporal validity

The system SHALL require quantity greater than zero and SHALL require end time strictly after start time. The system SHALL accept intervals crossing midnight and SHALL calculate raw duration as end minus start without labeling it productive time.

#### Scenario: Reject invalid quantity or interval
- **WHEN** quantity is zero or negative, start equals end, or end precedes start
- **THEN** the system SHALL return a validation error and SHALL NOT persist the record

#### Scenario: Accept an interval crossing midnight
- **WHEN** start is `2026-09-26T22:00:00` and end is `2026-09-27T02:00:00`
- **THEN** the system SHALL accept the record and calculate raw duration as 240 minutes

### Requirement: Employee overlap prevention

The system SHALL reject a production interval that overlaps an existing active or inactive record for the same Funcionário, except when updating that same record. Overlap detection MUST be performed as a database existence query rather than loading all production records into application memory. Adjacent intervals SHALL be allowed, and equal times for different Funcionários SHALL be allowed.

#### Scenario: Reject partial or containing overlap
- **WHEN** the same Funcionário has `08:00-10:00` and a new record is `09:30-11:00`, `07:00-11:00`, or `08:30-09:00`
- **THEN** the system SHALL return conflict and SHALL NOT persist the new record

#### Scenario: Allow adjacent interval
- **WHEN** the same Funcionário has `08:00-10:00` and a new record starts exactly at `10:00`
- **THEN** the system SHALL allow the new record

#### Scenario: Allow simultaneous different employees
- **WHEN** two different Funcionários have production records with equal or overlapping intervals in the same Empresa
- **THEN** the system SHALL allow both records

### Requirement: Production update and lifecycle

The system SHALL allow scoped retrieval, update, activation, and deactivation of a production record, and SHALL NOT expose physical deletion. Updates MUST revalidate resource ownership and activity, quantity, interval, and employee overlap while ignoring the record being updated in the overlap query. Existing historical records SHALL remain queryable after related master resources are later deactivated.

#### Scenario: Update a production record without self-conflict
- **WHEN** a client updates an existing record while keeping or changing its interval and no other record for the same Funcionário overlaps it
- **THEN** the system SHALL save the updated data and preserve its identity and audit history

#### Scenario: Deactivate and reactivate a production record
- **WHEN** a client invokes the scoped deactivate or activate endpoint
- **THEN** the system SHALL change only the record status and SHALL preserve the record for future history queries

### Requirement: Production API and DTO contract

The system SHALL expose production operations under `/api/v1/empresas/{empresaId}/producoes`: POST, paginated GET, scoped GET by ID, PUT, and PATCH activation/deactivation. Requests and responses MUST use DTOs, and responses SHALL include enough linked Setor, Funcionário, and Peça data plus raw duration. The API SHALL return 201 for creation, 200 or 204 for successful changes, 400 for validation, 404 for missing scoped resources, and 409 for business conflicts.

#### Scenario: Retrieve a production record in its Empresa scope
- **WHEN** a client requests a production ID through its owning Empresa
- **THEN** the system SHALL return the DTO with resource identifiers/codes, quantity, interval, observation, status, timestamps, and raw duration

#### Scenario: Prevent cross-company production access
- **WHEN** a client requests, updates, activates, or deactivates a record belonging to Empresa B through Empresa A's endpoint
- **THEN** the system SHALL return not found and SHALL NOT disclose or modify the record

### Requirement: Production filtering and pagination

The system SHALL paginate production listings and SHALL support optional filters for setorId, funcionarioId, pecaId, start/end period, and active status. The default ordering SHALL be newest start time first. Filtering SHALL remain implementable without a combinatorial set of repository methods.

#### Scenario: Filter production by resource and period
- **WHEN** a client requests a company's production collection with employee, part, sector, period, or active filters and page parameters
- **THEN** the system SHALL return only records satisfying all supplied filters in the requested page

### Requirement: Production persistence and audit

The system SHALL persist the record with required foreign keys to Empresa, Setor, Funcionário, and Peça, non-null quantity and interval fields, status, observation, creation/update timestamps, and indexes supporting company-period and resource-period queries. The schema SHALL be added through a new versioned migration without modifying prior migrations.

#### Scenario: Apply production schema
- **WHEN** migrations run against a clean supported test database
- **THEN** the production table, foreign keys, required constraints, and useful indexes SHALL be created successfully
