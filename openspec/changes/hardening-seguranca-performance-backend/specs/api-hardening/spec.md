# Spec Delta

## Purpose

Define defensive API behavior for untrusted inputs, expensive operations, pagination, sorting, rate limits, and safe errors so normal clients remain compatible while abusive or ambiguous requests fail predictably.

## ADDED Requirements

### Requirement: DTOs and query parameters SHALL enforce bounded validation

The system SHALL validate nullability, lengths, formats, enumerations, numeric ranges, IDs, dates, date intervals, page numbers, page sizes, top-N values, and operation-specific limits at the API boundary.

#### Scenario: Client sends an invalid range or page size
- **WHEN** a request contains an invalid date interval, negative page, excessive page size, invalid sort field, or out-of-range top-N
- **THEN** the system returns a stable validation error and does not execute an unbounded query

### Requirement: List endpoints SHALL be paginated with an ordering allowlist

Every potentially large listing SHALL define a finite default and maximum page size and SHALL accept sorting only over explicitly allowed fields with deterministic tie-breaking.

#### Scenario: Client requests an oversized page
- **WHEN** a client requests a page size above the configured maximum
- **THEN** the system rejects or clamps it according to the documented policy and never loads an unlimited result set

#### Scenario: Client supplies an unsafe sort expression
- **WHEN** a client sends a field or direction outside the sort allowlist
- **THEN** the system returns a validation error without concatenating the value into SQL or JPQL

### Requirement: Expensive operations SHALL have explicit limits and rate protection

The system SHALL apply bounded payload, upload, date-window, export, report, import, AI, and RAG limits, and SHALL rate-limit critical login and AI operations without permanent insecure lockout.

#### Scenario: Client exceeds an operation limit
- **WHEN** a payload, upload, report interval, export size, AI request, or RAG top-K exceeds its configured limit
- **THEN** the system returns a controlled error before expensive or partial processing

#### Scenario: Client exceeds the AI request window
- **WHEN** a user/company exceeds the configured AI rate window
- **THEN** the AI operation is rejected with an explicit rate-limit response while production, Dashboard, and reports remain available

### Requirement: API errors SHALL not disclose implementation details

The system SHALL preserve the uniform error contract with correlation ID and SHALL exclude stack traces, SQL, table names, local paths, tokens, secrets, and internal configuration from client responses.

#### Scenario: Unexpected exception reaches the API boundary
- **WHEN** an unhandled runtime failure occurs
- **THEN** the client receives a generic 500 response with correlation ID while diagnostic details remain only in internal logs

### Requirement: Exported user-controlled text SHALL be safe from formula interpretation

CSV and spreadsheet exports SHALL neutralize text beginning with formula-control characters when that text can be interpreted by spreadsheet software.

#### Scenario: Export contains formula-like user text
- **WHEN** an exported field begins with `=`, `+`, `-`, or `@`
- **THEN** the exported value is escaped so spreadsheet software treats it as text
