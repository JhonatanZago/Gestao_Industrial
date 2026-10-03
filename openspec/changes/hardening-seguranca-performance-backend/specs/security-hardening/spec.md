# Spec Delta

## Purpose

Define a verifiable security boundary for every backend endpoint and industrial resource, preserving tenant and sector isolation while preventing common OWASP access, injection, upload, authentication, and secret-handling failures.

## ADDED Requirements

### Requirement: Endpoint access is explicitly inventoried and denied by default

The system SHALL maintain an inventory of every REST endpoint with method, path, authentication requirement, permission, company and sector scope, financial or audit sensitivity, pagination, and applicable limits. Any endpoint not explicitly classified SHALL be denied by default.

#### Scenario: Unclassified endpoint is requested
- **WHEN** an authenticated or anonymous client requests a route without an explicit access classification
- **THEN** the request is denied without invoking the business operation or revealing resource existence

### Requirement: Resource authorization SHALL prevent cross-company and cross-sector access

The system SHALL validate company ownership and, where applicable, sector scope for path IDs, query IDs, body IDs, uploads, reports, audit records, and RAG documents before returning or mutating data.

#### Scenario: User changes a resource ID to another company
- **WHEN** a valid token for company A requests or mutates a resource belonging to company B
- **THEN** the system returns the defined 403 or 404 policy and never returns the resource or applies the mutation

#### Scenario: User requests a sector outside the assigned scope
- **WHEN** a user with restricted sectors supplies an unauthorized sector in a path, filter, body, report, or document request
- **THEN** the system denies the operation and does not leak aggregates, names, or existence of that sector

### Requirement: Authentication and secrets SHALL be handled safely

The system SHALL reject malformed, invalid, expired, and incorrectly signed JWTs; SHALL use a strong password hash without exposing password or hash fields; and SHALL keep real secrets out of source, configuration examples, logs, responses, and documentation.

#### Scenario: Invalid or expired token is presented
- **WHEN** a client sends a malformed, expired, or invalidly signed bearer token
- **THEN** the system returns the uniform unauthorized error contract without disclosing token-validation details

#### Scenario: Sensitive value reaches a log or response boundary
- **WHEN** an authorization header, JWT, password, API key, cookie, secret, or password hash is processed
- **THEN** the value is redacted or excluded from logs, DTOs, errors, metrics, and documentation

### Requirement: Browser and management surfaces SHALL be hardened

The system SHALL accept only explicitly configured CORS origins, SHALL not combine wildcard origins with credentials, SHALL document the JWT/CSRF decision and HTTPS or forwarded-header assumptions, SHALL emit applicable security headers, and SHALL keep sensitive Actuator endpoints protected.

#### Scenario: Disallowed browser origin sends a request
- **WHEN** a browser request uses an origin outside the configured allowlist
- **THEN** CORS does not authorize the request or expose protected response data

#### Scenario: Anonymous client requests management details
- **WHEN** an anonymous client requests metrics, Prometheus, environment, configuration, heap, or bean details
- **THEN** the endpoint is denied or unavailable according to the management exposure policy

### Requirement: File and document inputs SHALL be constrained and non-executable

The system SHALL validate upload size, media type, structure, row or document limits, safe names, and archive expansion risk where applicable; SHALL reject path traversal and macro/script execution; and SHALL apply authorization before persisting or retrieving RAG content.

#### Scenario: Malformed or oversized Excel upload is submitted
- **WHEN** an upload has an unsupported type, invalid structure, excessive size/rows, or unsafe archive characteristics
- **THEN** the system returns a controlled validation error before partial business persistence

#### Scenario: RAG document crosses a tenant or sector boundary
- **WHEN** a search or document operation could retrieve content outside the caller's company or sector scope
- **THEN** authorization filters the content before retrieval and no cross-context result is returned

### Requirement: AI tools SHALL remain read-only and authorization-bound

The system SHALL keep AI/RAG tools restricted to authorized read operations, validate tool parameters as untrusted input, and prevent prompts or documents from changing backend authorization or executing arbitrary SQL or mutations.

#### Scenario: Prompt attempts to execute a mutation or free-form SQL
- **WHEN** an AI prompt or tool parameter requests insertion, update, deletion, or arbitrary SQL execution
- **THEN** the request is rejected or produces a safe unavailable response and no domain mutation occurs
