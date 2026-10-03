# Spec Delta

## Purpose

Provide durable security, performance, dependency, and homologation documentation so operators and reviewers can reproduce decisions, understand limitations, and verify the Backend V1 hardening state.

## ADDED Requirements

### Requirement: Security hardening documentation SHALL cover the threat surface

`docs/SECURITY_HARDENING.md` SHALL document endpoint authorization, tenant/sector isolation, OWASP review, JWT and secrets, CORS/CSRF/HTTPS, security headers, uploads, AI/RAG, Actuator, rate limits, errors, dependencies/CVEs, OpenAPI policy, and real remaining risks.

#### Scenario: Reviewer evaluates a security control
- **WHEN** a reviewer follows the security document for an endpoint, upload, AI/RAG, Actuator, or dependency concern
- **THEN** the document identifies the control, evidence/test, configuration boundary, and any accepted or pending risk without exposing secrets

### Requirement: Performance documentation SHALL be reproducible and honest

`docs/PERFORMANCE_BACKEND.md` SHALL document dataset generation, environment, endpoints, baseline measurements, query/N+1 findings, indexes, optimizations, before/after results, load tool/scenarios, limitations, and reproduction commands.

#### Scenario: Operator repeats a performance measurement
- **WHEN** an operator follows the documented command and dataset procedure
- **THEN** the operator can reproduce the profile and distinguish local technical measurements from production SLAs

### Requirement: Pre-homologation evidence SHALL be checkable

The change SHALL provide a checklist covering endpoints, company/sector, financial and audit authorization, Actuator, CORS/CSRF, headers, uploads, AI/RAG, migrations, indexes, N+1, pagination, performance, dependencies, and secret/log review.

#### Scenario: Homologation checklist is reviewed
- **WHEN** a release candidate is evaluated
- **THEN** every checklist item has a pass, fail, not-applicable justification, or documented pending risk linked to evidence
