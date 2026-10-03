# Spec Delta

## Purpose

Define secure frontend access to operational reports, supported exports, and audit history while preserving backend authorization and traceability.

## ADDED Requirements

### Requirement: Reports match operational filters
The frontend SHALL request only report formats and filters supported by Backend V1 and SHALL present the selected period and scope alongside generated results or downloads.

#### Scenario: Supported report export
- **WHEN** an authorized user selects a supported format and valid filters
- **THEN** the frontend starts the real export and reports success or a safe failure without inventing unsupported formats

### Requirement: Permission-controlled audit view
The frontend SHALL make audit records available only to users authorized by the backend and SHALL display actor, timestamp, action, resource, company, and available before/after information without secrets.

#### Scenario: Unauthorized audit access
- **WHEN** a user without audit permission navigates directly to the audit route
- **THEN** the frontend shows a forbidden state and does not request or display audit data
