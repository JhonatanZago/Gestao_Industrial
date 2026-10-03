# Spec Delta

## Purpose

Define a reproducible performance review for the existing backend flows, using measured baselines to guide optimization of Dashboard, analytics, quality, audit, reports, import, AI/RAG, pools, and optional cache without inventing production SLAs.

## ADDED Requirements

### Requirement: Critical flows SHALL have a recorded baseline

The project SHALL record environment, synthetic data volume, latency, approximate query count, response size, errors, JVM/GC, and pool observations for Dashboard, drill-downs, production, quality, audit, reports, import, and mocked AI before applying optimizations.

#### Scenario: Baseline is executed on the documented dataset
- **WHEN** the critical endpoint suite runs against the documented synthetic dataset
- **THEN** results include reproducible measurements and explicitly state that local results are not a production SLA

### Requirement: Dashboard and reporting responses SHALL remain bounded

Dashboard, quality, reports, exports, and drill-downs SHALL aggregate or page at the database/application boundary, limit requested periods and top-N values, and avoid loading unbounded industrial data or sending full transactional datasets to an AI model.

#### Scenario: Client requests a large period or drill-down
- **WHEN** a request exceeds the configured period, top-N, page, or record limit
- **THEN** the system rejects or bounds the request before excessive memory, query, or response growth

### Requirement: Resource pools and cache SHALL be bounded and scoped

HikariCP and any executor SHALL have finite observable limits. Any future cache SHALL include company and required permission/sector scope in its key, define TTL and invalidation, and never share private or financial data across contexts.

#### Scenario: Two companies request equivalent cached data
- **WHEN** cache is enabled for a measured use case
- **THEN** each company receives only its own authorized result and expiry/invalidation behavior is observable

### Requirement: Optimizations SHALL preserve behavior and security

Performance changes SHALL be justified by baseline evidence, retain authorization, error, audit, and business semantics, and include before/after measurements for the affected flow.

#### Scenario: Optimization changes a query or aggregation
- **WHEN** an implementation replaces a query, fetch strategy, or aggregation
- **THEN** functional regression, tenant isolation, result equivalence, query count, and latency are compared before marking the optimization complete
