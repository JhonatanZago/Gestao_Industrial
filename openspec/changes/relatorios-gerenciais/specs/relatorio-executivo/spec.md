# Spec Delta

## Purpose

Define a compact, auditable executive report that consolidates validated production indicators without duplicating analytical business rules or exposing data outside the authenticated company and sector scope.

## ADDED Requirements

### Requirement: Generate an executive report for an explicit period

The system SHALL provide `GET /api/v1/empresas/{empresaId}/relatorios/executivo` with required `inicio` and `fim` parameters and SHALL return the effective company, period, generation timestamp, authenticated generator, production summary, comparison, operational dimensions, stops, goals, performance, coverage, and objective attention points.

#### Scenario: Executive report uses the requested period

- **WHEN** an authorized user requests a report with a valid `inicio` and `fim`
- **THEN** the response SHALL identify the same effective interval and SHALL contain only data belonging to that interval and authorized scope

#### Scenario: Invalid period is rejected

- **WHEN** `inicio` or `fim` is missing, malformed, or `fim` precedes `inicio`
- **THEN** the system SHALL return a validation error without generating a partial report

### Requirement: Preserve analytical consistency

The executive report SHALL use the same definitions and effective filters as the corresponding analytics endpoints, so shared base values such as produced quantity, gross duration, stopped time, productive time, rates, and industrial value SHALL match for the same context.

#### Scenario: Summary matches analytics

- **WHEN** a user requests analytics summary and executive report with the same company, period, and filters
- **THEN** the shared metrics SHALL be numerically equal, subject only to the documented response representation and rounding

### Requirement: Support explicit comparison periods

The system SHALL accept optional `inicioAnterior` and `fimAnterior`; when they are absent, it SHALL use the immediately preceding interval with the same duration and SHALL return both effective intervals in the response.

#### Scenario: Comparison is calculated from two documented intervals

- **WHEN** current and previous periods are valid
- **THEN** the response SHALL include current value, previous value, absolute or percentage variation as defined by the contract, and both effective periods for quantity, productive time, stopped time, and authorized industrial value

### Requirement: Enforce report scope and financial visibility

The report SHALL enforce authenticated company and sector scope at query time. Financial sections and fields SHALL be omitted unless the user has `FINANCEIRO_INDUSTRIAL_VISUALIZAR`; omission SHALL not be replaced by masked values or totals that permit financial inference.

#### Scenario: Sector-scoped user receives only authorized sectors

- **WHEN** a gestor is associated with Pintura and KTL and requests an executive report
- **THEN** all report sections and derived aggregations SHALL be computed only from Pintura and KTL data, excluding Laser at the source

#### Scenario: User without financial permission receives no financial data

- **WHEN** an otherwise authorized user lacks `FINANCEIRO_INDUSTRIAL_VISUALIZAR`
- **THEN** the JSON SHALL omit the financial section and all financial fields, including industrial value, monetary coverage, and financial comparisons

### Requirement: Produce deterministic attention points

The report SHALL expose optional attention points generated only from centralized, documented thresholds over verified metrics and SHALL NOT make subjective personnel or business judgments or call industrial value real revenue.

#### Scenario: Low standard-time coverage creates an objective point

- **WHEN** standard-time coverage for the period is 60% and the configured threshold is crossed
- **THEN** the report MAY include a point stating the measured coverage and SHALL NOT infer a cause or assign a subjective ranking
