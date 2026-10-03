# Spec Delta

## Purpose

Define reproducible end-to-end validation using deterministic synthetic data, real API contracts, valid and invalid Excel fixtures, isolated users, and environment-independent test doubles.

## ADDED Requirements

### Requirement: The official E2E flow shall reuse data produced by previous steps
The validation flow SHALL execute login, permissions, industrial registrations, Excel import, production, stoppages, analytics, goals, performance, industrial value, data quality, dashboard, reports, audit, and authorized AI/RAG in dependency order, using outputs from prior steps where applicable.

#### Scenario: Successful full flow
- **WHEN** the deterministic dataset and valid fixture are loaded into a clean test environment
- **THEN** each stage consumes the previous stage's identifiers and completes without relying on internet access or a paid AI provider

### Requirement: Homologation data shall be deterministic and isolated
The validation assets SHALL include the named company, sectors, employees, pieces, machines, official mathematical values, and a second company for isolation checks, with stable identifiers or lookup keys that do not use real personal or industrial data.

#### Scenario: Repeatable seed
- **WHEN** the dataset setup is repeated in a clean environment
- **THEN** the same logical records, relationships, quantities, times, and expected outcomes are produced without committing an oversized generated artifact

### Requirement: Excel import validation shall cover accepted and rejected inputs
The import contract SHALL be exercised with a real compatible XLSX fixture and negative fixtures for empty, malformed, missing-column, invalid employee, invalid piece, invalid quantity, invalid time, and over-limit files.

#### Scenario: Valid all-or-nothing import
- **WHEN** a valid fixture is uploaded to the permitted company and sector
- **THEN** validation, persistence, analytics, dashboard, and report results reflect the imported rows and all expected import metadata is available

#### Scenario: Invalid or partially invalid import
- **WHEN** an invalid or over-limit fixture is uploaded
- **THEN** the API returns its documented safe error, records the rejected/failed import state, and persists no partial production rows

### Requirement: Automated validation shall cover contract, security, integration, E2E, performance, and regression gates
The project SHALL provide reproducible commands or test artifacts for each applicable category and SHALL report unavailable tools or environments as limitations rather than silently passing them.

#### Scenario: Tool unavailable
- **WHEN** k6, a dependency scanner, or a target PostgreSQL/Testcontainers profile is unavailable
- **THEN** the report records the exact command, environment limitation, and unperformed evidence without presenting an SLA or equivalence claim

### Requirement: AI and RAG validation shall use deterministic providers and preserve grounding
Authorized assistant answers SHALL use the same backend numbers, return explicit no-data or unauthorized outcomes, resist prompt/tool injection, and enforce company/sector document isolation when RAG is implemented.

#### Scenario: Unauthorized question or document
- **WHEN** a user asks about another company/sector, financial data without permission, audit data without permission, or submits an instruction-injection document
- **THEN** the operation is denied or safely constrained and no protected or fabricated answer is returned
