# Spec Delta

## Purpose

Define the evidence, quality gates, and documentation needed to release the frontend as a reproducible consumer of Backend V1.

## ADDED Requirements

### Requirement: Reproducible frontend verification
The frontend SHALL provide deterministic commands and tests covering unit behavior, API integration boundaries, permissions, the main E2E journey, production build, accessibility basics, and responsive critical layouts.

#### Scenario: Validation gate
- **WHEN** the frontend validation workflow runs in a clean supported environment
- **THEN** test and build results are recorded, TypeScript errors fail the gate, and no external paid AI service is required

### Requirement: Contract integration inventory
The project SHALL document the mapping from each frontend feature to its real endpoint, method, request/response shape, permission, pagination/filter behavior, and known backend limitation.

#### Scenario: Backend contract divergence
- **WHEN** the source specification differs from the deployed backend contract
- **THEN** the implementation follows the real contract and records the divergence instead of inventing an endpoint or DTO

### Requirement: Frontend release evidence
The project SHALL document architecture, Design System, API integration, execution, test results, visual/functional homologation, limitations, and the explicit `FRONTEND: GO` or `FRONTEND: NO-GO` decision.

#### Scenario: Incomplete mandatory gate
- **WHEN** a mandatory test, build, integration, or permission gate fails
- **THEN** the report records `FRONTEND: NO-GO` and lists the exact blocker
