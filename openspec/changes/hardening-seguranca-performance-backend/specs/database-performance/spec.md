# Spec Delta

## Purpose

Define evidence-based database behavior for safe query construction, tenant-aware indexing, bounded aggregation, concurrency, transaction scope, migrations, and removal of critical N+1 patterns without changing industrial business rules.

## ADDED Requirements

### Requirement: Database queries SHALL be parameterized and tenant-scoped

Native SQL, JPQL, Specifications, filters, and AI/RAG queries SHALL never concatenate untrusted input, and every query returning tenant data SHALL include the required company and sector authorization constraints.

#### Scenario: Filter contains SQL metacharacters
- **WHEN** a client submits a filter, search term, sort value, or AI-derived parameter containing SQL syntax
- **THEN** the value is treated as data, the query remains parameterized, and authorization constraints remain active

### Requirement: Critical query paths SHALL avoid measurable N+1 behavior

Dashboard, production, quality, audit, import, report, and RAG query paths SHALL use measured projections, bounded fetches, or equivalent query strategies when related data is required, without making unrelated associations globally eager.

#### Scenario: Critical endpoint loads related rows
- **WHEN** a measured endpoint returns related entities or aggregates for a bounded request
- **THEN** query count and latency remain within the recorded baseline target without one query per returned row

### Requirement: Indexes SHALL follow observed predicates and the target database

The system SHALL review indexes for company, sector, date/time, resource, status, and audit predicates using actual query plans when the target database is available, and SHALL avoid indiscriminate indexing.

#### Scenario: Migration is evaluated on a clean database
- **WHEN** all migrations run from an empty target-compatible database
- **THEN** schema, constraints, indexes, and application validation complete successfully without modifying an already-applied migration

### Requirement: Transactions SHALL be bounded and concurrency-safe where risk exists

Read-only queries SHALL use appropriate read-only boundaries; external AI or integration calls SHALL not hold database transactions open; and concurrent imports, configuration changes, vigencies, and goals SHALL preserve consistency without adding versioning where no lost-update risk exists.

#### Scenario: Concurrent operations update the same protected configuration
- **WHEN** two valid requests race to change a configuration with a demonstrated lost-update risk
- **THEN** the system detects or prevents the conflict according to the documented policy and does not silently overwrite one valid change
