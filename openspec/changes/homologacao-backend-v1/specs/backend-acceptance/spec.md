# Spec Delta

## Purpose

Define the observable acceptance contract for the completed Backend V1, including its industrial calculations, authorization boundaries, error handling, and explicit GO/NO-GO decision.

## ADDED Requirements

### Requirement: Backend V1 acceptance shall cover the implemented product surface
The system SHALL be evaluated across authentication, company and sector scope, industrial registrations, Excel import, production, stoppages, analytics, goals, performance, industrial value, data quality, dashboard, reports, audit, and AI/RAG using only capabilities present in the implementation.

#### Scenario: Complete acceptance scope
- **WHEN** the acceptance suite is executed
- **THEN** every implemented module is represented by at least one reproducible scenario and unsupported formats or features are marked not applicable rather than approved

### Requirement: Official deterministic calculations shall agree across consumers
For the official homologation periods, the system SHALL preserve the configured results of 10,000 pieces, 540 productive minutes, 90.91% goal attainment, 10% stoppage percentage, and approximately 21.43% industrial-value growth, while preserving null/coverage semantics for missing denominators or data.

#### Scenario: Current versus previous period
- **WHEN** the official deterministic records are queried through analytics, dashboard, report, and authorized assistant flows
- **THEN** each consumer returns equivalent values for production growth of 25%, stoppage reduction of 25%, goal attainment, stoppage percentage, and industrial-value growth

### Requirement: Authorization and isolation shall be demonstrable
The system SHALL reject unauthenticated, invalid-token, expired-token, unauthorized-financial, unauthorized-audit, cross-company, and cross-sector access without exposing protected data.

#### Scenario: Cross-company identifier substitution
- **WHEN** a user from Company A replaces a path, query, or body identifier with a resource belonging to Company B
- **THEN** the request is denied or returns the safe not-found contract and no Company B data is disclosed

#### Scenario: Restricted sector aggregation
- **WHEN** a user restricted to Painting and KTL requests any authorized aggregation or drill-down
- **THEN** only those sectors and their permitted resources are included across dashboard, analytics, registrations, reports, quality, and AI/RAG

### Requirement: Homologation shall produce an evidence-backed decision
The release decision SHALL be `BACKEND V1: GO` only when mandatory scenarios, required tests, build, OpenSpec validation, migrations, and documented security/performance gates pass; otherwise it SHALL be `BACKEND V1: NO-GO` with blocking defects and pending evidence listed.

#### Scenario: Blocking defect remains
- **WHEN** a critical or high-severity defect blocks security, integrity, isolation, or the main flow
- **THEN** the decision is `BACKEND V1: NO-GO` and the defect remains explicitly classified with reproduction evidence

#### Scenario: Mandatory evidence passes
- **WHEN** all mandatory acceptance gates pass and no critical/high blocking defect remains
- **THEN** the report may declare `BACKEND V1: GO` and identify the evidence for each gate
