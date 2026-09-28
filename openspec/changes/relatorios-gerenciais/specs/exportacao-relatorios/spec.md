# Spec Delta

## Purpose

Define secure CSV and XLSX exports of report models with the same authorized scope and financial visibility as JSON, predictable filenames and content types, correct cell types, and bounded resource usage.

## ADDED Requirements

### Requirement: Export reports in CSV and XLSX

The system SHALL provide a coherent export endpoint for supported reports, including `GET /api/v1/empresas/{empresaId}/relatorios/executivo/exportar?formato=CSV|XLSX`, and SHALL return the requested report using the same filters, period, scope, and permission decisions as JSON.

#### Scenario: CSV export has a safe tabular response

- **WHEN** an authorized user requests CSV
- **THEN** the response SHALL use UTF-8, documented separator and escaping, clear headers, correct `Content-Type`, and a sanitized `Content-Disposition` filename

#### Scenario: XLSX export has typed workbook cells

- **WHEN** an authorized user requests XLSX
- **THEN** the workbook SHALL contain documented useful sheets, typed dates/numbers/percentages/monetary values, and no financial sheet unless financial permission is present

### Requirement: Prevent export data leakage and formula injection

Exports SHALL contain exactly the rows, dimensions, sections, and financial visibility permitted in the JSON report. Controlled text fields beginning with `=`, `+`, `-`, or `@` SHALL be neutralized when exported to CSV if they could be interpreted as formulas.

#### Scenario: Sector-scoped export excludes forbidden data

- **WHEN** a gestor scoped to Pintura requests CSV or XLSX
- **THEN** neither format SHALL contain Laser rows, names, totals, or derived aggregates

#### Scenario: Non-financial user receives no financial columns

- **WHEN** a user without `FINANCEIRO_INDUSTRIAL_VISUALIZAR` requests an export
- **THEN** the file SHALL omit financial columns, sheets, totals, and hidden financial cells

#### Scenario: Formula-like text is sanitized

- **WHEN** a controlled text value starts with a spreadsheet formula prefix
- **THEN** the CSV representation SHALL neutralize the prefix while preserving the visible value semantics

### Requirement: Bound export work and record generation

The system SHALL enforce documented period/row limits, SHALL avoid indefinite server-side file retention, and SHALL log user ID, company ID, report type, period, format, and generation timestamp without logging the full report content.

#### Scenario: Oversized export is rejected

- **WHEN** the requested period or result volume exceeds the configured limit
- **THEN** the system SHALL return a clear client error without constructing an unbounded workbook in memory
