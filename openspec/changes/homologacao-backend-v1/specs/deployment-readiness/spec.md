# Spec Delta

## Purpose

Define the release-readiness evidence for running the Backend V1 safely, including configuration, migrations, health, observability, recovery guidance, and non-destructive release preparation.

## ADDED Requirements

### Requirement: Configuration and profiles shall be inventoryable without secrets
The release documentation SHALL list the real environment variables and profiles used by the application, including database, JWT, CORS, AI, operational limits, and monitoring settings, with placeholders and explicit defaults/requirements.

#### Scenario: Production configuration review
- **WHEN** a reviewer compares the documented variables with the application profiles and properties
- **THEN** every required production value is identified, no secret is exposed, and test/dev defaults are not presented as production credentials

### Requirement: Database readiness shall be proven from an empty database
The release validation SHALL execute all available migrations against an empty supported database when available, start the application, run a smoke test, and verify constraints, indexes, native queries, and compatibility without modifying historical migrations.

#### Scenario: Clean PostgreSQL startup
- **WHEN** an empty PostgreSQL environment is provisioned using the declared configuration
- **THEN** migrations complete, readiness becomes healthy, and the basic API smoke flow succeeds; unavailable target environments are recorded as pending evidence

### Requirement: Operational readiness shall expose safe health and recovery procedures
The release package SHALL document health/liveness/readiness, logs and correlation IDs, metrics, graceful shutdown, backup/rollback guidance, AI degradation behavior, and bounded pool/timeout settings without exposing sensitive details.

#### Scenario: AI dependency unavailable
- **WHEN** the configured AI provider or fake gateway is unavailable during a supported operation
- **THEN** the backend remains operational for non-AI flows, reports the degraded state and metrics safely, and does not fabricate a successful answer

### Requirement: Release preparation shall be non-destructive
The project SHALL provide build, version/tag, migration, health, and rollback instructions for a tag such as `backend-v1.0.0`, but SHALL NOT publish remotely, delete data, or execute a destructive deployment automatically.

#### Scenario: Release candidate preparation
- **WHEN** the Backend V1 receives a GO decision
- **THEN** a reviewer can follow the documented release checklist and prepare the tag/build while retaining explicit control over remote publication and production changes
