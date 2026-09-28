# Spec Delta

## Purpose

Define focused production, stop, and performance reports that expose compact operational aggregations and filters while reusing the system's existing analytics semantics, permissions, tenant isolation, and sector scope.

## ADDED Requirements

### Requirement: Provide focused report endpoints

The system SHALL provide `GET /api/v1/empresas/{empresaId}/relatorios/producao`, `/relatorios/paradas`, and `/relatorios/desempenho`. Each endpoint SHALL require the report-view permission in addition to permissions needed by sensitive sections and SHALL accept an explicit valid period.

#### Scenario: Production report returns filtered operational aggregations

- **WHEN** an authorized user requests the production report with period and optional setor, funcionario, peca, maquina, or turno filters
- **THEN** the response SHALL return compact aggregations for the requested dimensions and SHALL not return the complete historical detail of every employee

#### Scenario: Stop report returns reasons and durations

- **WHEN** an authorized user requests the stop report for a valid period
- **THEN** the response SHALL include total stopped time, percentage of stopped time, occurrence count, and reason-level code, name, category, occurrences, minutes, and percentage

#### Scenario: Performance report preserves coverage

- **WHEN** an authorized user requests the performance report
- **THEN** the response SHALL include actual quantity, expected quantity, performance index, records with and without standard, and standard coverage percentage without hiding partial coverage

### Requirement: Apply filters before aggregation

Focused reports SHALL apply company, sector, period, and dimension filters before aggregation and SHALL reject an explicit sector outside the authenticated user's scope with 403.

#### Scenario: Forbidden sector is rejected before querying report data

- **WHEN** a gestor requests any focused report with an explicit sector outside the user's associations
- **THEN** the system SHALL return 403 and SHALL not return an aggregate containing data from that sector

### Requirement: Limit ranked dimensions

The production and executive reports MAY accept `top`; when provided, the system SHALL validate a positive bounded value and SHALL apply it only to explicitly ranked dimensions such as parts or sectors.

#### Scenario: Excessive top value is rejected

- **WHEN** a caller supplies a `top` value above the configured maximum
- **THEN** the system SHALL return a validation error rather than allocating an unbounded result
