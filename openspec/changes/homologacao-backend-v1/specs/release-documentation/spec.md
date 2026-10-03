# Spec Delta

## Purpose

Define the documentation and evidence package required to operate, review, reproduce, and approve the Backend V1 without documenting features that are absent from the code.

## ADDED Requirements

### Requirement: Public project documentation shall match implemented behavior
The repository SHALL document purpose, architecture, stack, prerequisites, execution, testing, configuration, database/migrations, API contracts, security, observability, AI/RAG, Excel import, operation, deployment, rollback, and limitations using real names and placeholders instead of secrets.

#### Scenario: Clean environment reproduction
- **WHEN** a developer follows the documented Windows execution and test instructions in a clean environment with the declared prerequisites
- **THEN** the documented build, test, startup, health, and smoke-test commands are executable or the missing prerequisite is explicitly identified

### Requirement: The homologation report shall provide a traceable acceptance matrix
The final report SHALL include version, date, environment, commit, scope, scenarios, evidence, failures, corrections, security, performance, database, limitations, pending items, and a matrix with functionality, scenario, result, and evidence. Results SHALL be only `APROVADO`, `REPROVADO`, or `NÃO APLICÁVEL`.

#### Scenario: Review of a failed gate
- **WHEN** a reviewer selects a failed or not-applicable matrix row
- **THEN** the report identifies the reproducible command or artifact and explains the defect, limitation, or reason it was not applicable

### Requirement: Defects and release readiness shall be classified explicitly
The documentation SHALL classify defects as CRÍTICO, ALTO, MÉDIO, or BAIXO, record regression evidence for corrected defects, classify TODO/FIXME/HACK items, report dependency/CVE analysis limitations, and describe release/tag preparation only when GO is justified.

#### Scenario: Release recommendation
- **WHEN** the report is generated after all applicable gates are evaluated
- **THEN** it contains exactly one explicit Backend V1 decision and does not claim publication, cloud deployment, or an unsupported API format was executed

### Requirement: API collection and smoke test shall be safe to share
The repository SHALL provide a Postman or equivalent collection for the implemented primary endpoints plus a secret-free environment example and a reproducible smoke test covering health, login, a basic query, and dashboard where available.

#### Scenario: Secret-free import
- **WHEN** a reviewer imports the collection/environment
- **THEN** no real token, password, key, or provider secret is present and required values are represented by placeholders or variables
