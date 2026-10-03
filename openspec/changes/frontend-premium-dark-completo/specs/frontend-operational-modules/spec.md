# Spec Delta

## Purpose

Define the authenticated operational screens that consume real production, analytics, industrial master-data, import, quality, and performance contracts from Backend V1.

## ADDED Requirements

### Requirement: Backend-grounded operational views
The frontend SHALL render dashboard, production, sectors, employees, parts, machines, stops, targets, performance, and data-quality views from real API responses, preserving backend filters, pagination, permissions, and null/absence semantics.

#### Scenario: Filtered dashboard
- **WHEN** an authorized user changes period or supported operational filters
- **THEN** the frontend requests the corresponding backend data and updates cards, charts, lists, and comparisons without client-side business-rule reimplementation

### Requirement: Industrial data management
The frontend SHALL support the CRUD and active/inactive flows that actually exist for industrial resources and SHALL validate user input using the backend response as the source of truth.

#### Scenario: Duplicate or invalid resource
- **WHEN** the backend rejects a create or update operation
- **THEN** the frontend displays the field or business error safely and does not fabricate a successful state

### Requirement: Excel import workflow
The frontend SHALL support the real Excel validation/upload lifecycle and display processing status, counts, line errors, and import details without exposing stack traces.

#### Scenario: Invalid spreadsheet
- **WHEN** a user selects an unsupported or invalid spreadsheet
- **THEN** the frontend reports the backend validation result and prevents the UI from presenting the import as persisted

### Requirement: Protected financial presentation
The frontend SHALL render industrial-value data only when the backend-authorized response and user permissions allow it, and SHALL never relabel industrial value as profit.

#### Scenario: User without financial permission
- **WHEN** a user without financial permission opens an operational view
- **THEN** financial cards, columns, filters, and drill-down values are not rendered
