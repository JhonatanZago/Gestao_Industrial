# Spec Delta

## Purpose

Define the official production workflow in which a standardized Excel file is imported into a selected sector and its validated records become the source for general, sector, employee, part, and simplified report views.

## ADDED Requirements

### Requirement: Standard production spreadsheet

The system SHALL accept a production workbook in XLSX format with exactly these required business columns: `Data`, `Código Funcionário`, `Código Peça`, `Quantidade`, `Hora Início`, and `Hora Fim`. The workbook SHALL NOT require a `Setor` column, and the employee and part codes SHALL be read as text so leading zeros are preserved.

#### Scenario: Valid standard workbook

- **WHEN** an authorized user submits an XLSX containing the six required columns and valid rows
- **THEN** the system SHALL accept the workbook for validation and report the number of rows read

#### Scenario: Missing required column

- **WHEN** a workbook omits one of the six required columns
- **THEN** the system SHALL reject it with a structured header error identifying the missing column and SHALL NOT persist production records

### Requirement: Sector selected by import context

The system SHALL assign every valid imported row to the active sector selected in the import request. It SHALL validate that the sector belongs to the company, is active, and is authorized for the current user, and SHALL NOT infer the sector from the filename or workbook content.

#### Scenario: Import into an authorized sector

- **WHEN** an authorized user submits a valid workbook with a selected sector
- **THEN** every persisted production record SHALL reference that sector and company

#### Scenario: Missing or unauthorized sector

- **WHEN** the sector does not exist, is inactive, belongs to another company, or is outside the user's scope
- **THEN** the system SHALL reject the request without importing any row and SHALL return a safe authorization or not-found error

### Requirement: Row validation and duration

The system SHALL validate every non-empty row: date must be valid, employee and part codes must be present, quantity must be an integer greater than zero, start and end times must be valid, and end time must be later than start time. The system SHALL calculate recorded duration as end minus start and SHALL NOT infer stops from gaps between adjacent rows.

#### Scenario: Invalid row with field-level errors

- **WHEN** a row contains an invalid date, unknown code, non-positive or fractional quantity, or invalid time interval
- **THEN** the validation result SHALL identify the line, field, supplied value when safe, and reason, and SHALL exclude the row from persistence

#### Scenario: Valid duration and gap

- **WHEN** two valid rows have a ten-minute gap between their recorded intervals
- **THEN** each row's duration SHALL be calculated from its own start and end and the ten-minute gap SHALL NOT be stored or reported as a stop

### Requirement: Atomic import result

The system SHALL support validation before persistence and SHALL use all-or-nothing behavior for the import operation: if any row or file-level validation fails, no production row from that operation SHALL be persisted. The response SHALL include status, rows read, valid rows, invalid rows, imported rows, sector, and structured line errors without stack traces.

#### Scenario: Successful import

- **WHEN** all non-empty rows pass file, sector, reference, and business validation
- **THEN** the system SHALL persist all valid records, mark the import as completed, and return the imported count

#### Scenario: Mixed-validity workbook

- **WHEN** at least one row fails validation
- **THEN** the system SHALL mark the import as rejected, persist no production row from it, and return errors for the invalid rows

### Requirement: Duplicate and reimport protection

The system SHALL detect duplicate or repeated imports using stable file/content and business context, including company and selected sector, and SHALL NOT use the filename alone as the identity. The behavior for a repeated identical import SHALL be deterministic and shall not create duplicate production records.

#### Scenario: Same workbook reimported

- **WHEN** the same workbook is submitted again for the same company and sector after a completed import
- **THEN** the system SHALL reject it as duplicate or return the existing import according to the documented API contract, and SHALL not duplicate production records

#### Scenario: Same filename with different content

- **WHEN** a different workbook uses the same filename
- **THEN** the system SHALL evaluate its content and business data rather than reject it solely because of the filename

### Requirement: Production record traceability

Each imported production record SHALL retain company, sector, employee, part, quantity, start, end, calculated recorded duration, import reference, source/origin, and source line when available. Employee and part codes SHALL remain queryable without losing leading zeros.

#### Scenario: Trace a dashboard value to an import

- **WHEN** a user opens a production detail or import-line result for a dashboard record
- **THEN** the system SHALL expose enough authorized context to identify the sector, employee code, part code, period, quantity, duration, import, and source line

### Requirement: Core dashboard aggregations

The system SHALL provide authorized, filterable aggregations for total production, number of sectors with data, employees involved, part codes produced, production by sector, production over time, total recorded duration, production by employee, and production by part. Supported filters SHALL include period and, where applicable, sector, employee, and part.

#### Scenario: General dashboard after import

- **WHEN** the fixture records for one sector are imported and the general dashboard is queried for their period
- **THEN** total quantity SHALL be 350, employees involved SHALL include `00147` and `00152`, parts SHALL include `PEC-001` and `PEC-002`, and sector aggregation SHALL total 350

#### Scenario: Sector dashboard filters

- **WHEN** a user queries an authorized sector dashboard with a period and optional employee or part filter
- **THEN** all returned totals and time series SHALL be limited to that sector and filter, without leaking other sectors or companies

### Requirement: Employee and part drill-downs

The system SHALL provide drill-down results for an authorized employee and an authorized part, including code, sector, selected period, quantity, temporal evolution, and recorded duration. Results SHALL not assign subjective performance evaluations or unsupported efficiency scores.

#### Scenario: Employee drill-down

- **WHEN** the user opens employee `00147` for the fixture period
- **THEN** the result SHALL show quantity 200 and the associated parts and sector within the authorized scope

#### Scenario: Part drill-down

- **WHEN** the user opens part `PEC-001` for the fixture period
- **THEN** the result SHALL show quantity 270 and its authorized employee/sector associations

### Requirement: Simplified report

The system SHALL provide a simplified report or summary containing selected period, total production, production by sector, production by employee, production by part, time evolution, total recorded duration, and a concise operational summary. It SHALL not present OEE, efficiency, profit, revenue, capacity, or inferred stops unless those values are independently supported by the selected data and contract.

#### Scenario: Report agrees with dashboards

- **WHEN** a report is generated for the same period and filters as the fixture dashboards
- **THEN** its total and group totals SHALL equal the corresponding dashboard totals

### Requirement: Resilience of secondary modules

The core path from Excel validation through production persistence and dashboard aggregation SHALL not depend on machines, shifts, AI/RAG, goals, advanced quality, financial calculations, or other secondary resources that are absent from the standard workbook. Existing secondary data and endpoints SHALL remain available where functional.

#### Scenario: Standard workbook without secondary columns

- **WHEN** a valid standard workbook contains no machine, shift, quality, AI, goal, or financial data
- **THEN** the core import and dashboard flow SHALL complete without requiring or fabricating those resources

### Requirement: Security and safe file handling

The system SHALL preserve authentication, company isolation, sector authorization, upload size/type limits, malformed-file protection, path traversal protection, and spreadsheet formula-injection protections. Error responses SHALL not disclose stack traces, secrets, or local file paths.

#### Scenario: Unauthorized cross-company access

- **WHEN** a user attempts to import into or query a company or sector outside the authorized scope
- **THEN** the system SHALL deny the operation and SHALL not reveal whether unrelated records exist

### Requirement: Performance and incremental compatibility

The system SHALL execute dashboard aggregations in the backend/database, support indexes for period, sector, employee, and part access patterns, and use incremental migrations only. Existing historical migrations, functional tables, and unrelated modules SHALL not be deleted or rewritten.

#### Scenario: Existing historical data

- **WHEN** the simplified dashboard is queried over existing and newly imported records
- **THEN** it SHALL use the same company/sector authorization rules and return consistent totals without requiring destructive data migration
