# Design

## Context

The project is a Spring Boot 4.1.1 API with JPA, Flyway, Spring Security/JWT, Apache POI, and analytics resources already covering production, stops, dimensions, performance, goals, and industrial finance. The new report layer must preserve the existing tenant/sector authorization and must not duplicate formulas from the analytics services.

## Goals / Non-Goals

**Goals:**

- Compose executive and focused report models from existing analytics contracts.
- Keep period normalization, comparisons, coverage, and financial authorization explicit.
- Expose JSON plus bounded CSV/XLSX exporters from one authorized report model.
- Make deterministic insights and audit metadata testable.

**Non-Goals:**

- Frontend, PDF generation, email delivery, scheduling, AI/RAG, subjective recommendations, fiscal revenue, or new cost/profit modeling.
- Persisting complete report snapshots or export files indefinitely.

## Decisions

### Report facade over analytics services

Introduce report query/facade services that receive a normalized report context and orchestrate the existing `AnalyticsService`, `FinanceiroIndustrialService`, `DesempenhoService`, and goal services. The facade maps existing responses into composed DTOs; it does not recalculate quantity, duration, stops, performance, goals, or industrial value. This is preferred over direct repository duplication because consistency with existing analytics is an acceptance criterion.

### One authorization context for JSON and exports

Resolve company, sector scope, report permission, and financial visibility once in the report application service. Pass the resulting context to every section query and exporter. Exporters consume the already-authorized report model rather than querying repositories independently, preventing JSON/export drift and permission bypasses.

### Explicit comparison context

Represent current and previous intervals in a normalized value object. If the previous interval is absent, derive the immediately preceding interval with equal duration and return the effective interval in the response. Reject invalid or ambiguous intervals at the boundary.

### Composed DTO sections

Use small response records for identity, period, production, comparison, sector, employee, part, machine, shift, stop, goal, performance, finance, coverage, and attention points. Keep employee detail compact and retain existing analytics detail endpoints for drill-down.

### Export strategy

Use a CSV writer implemented with explicit UTF-8, separator, quoting, and formula-prefix sanitization. Use Apache POI for XLSX with a bounded row policy, reusable sheet writers, typed cells, and no indiscriminate auto-sizing. Keep the report model independent of format so a future PDF exporter can reuse it.

### Authorization and permission mapping

Add/confirm `RELATORIO_VISUALIZAR` in the permission seed and map `/relatorios/**` to it in the request authorization layer. Financial report routes and financial sections additionally require `FINANCEIRO_INDUSTRIAL_VISUALIZAR`. Existing company and sector checks remain authoritative; explicit sector filters are rejected before report aggregation when unauthorized.

### Audit and limits

Log generation metadata through a dedicated small audit logger, excluding report content and secrets. Centralize maximum period duration, maximum rows/top value, insight thresholds, and export limits in configuration so they are not scattered magic numbers. Generate files in memory only within the configured bound or through a controlled temporary resource that is deleted after response handling.

## Risks / Trade-offs

- [Risk] Many analytics calls can make executive reports slow. → Reuse normalized filters, prefer existing aggregate methods, cap Top N/rows, and add deterministic integration tests; optimize with projections only after measuring.
- [Risk] Existing analytics DTOs may not expose every field needed by reports. → Add narrowly scoped analytics projections or service methods, keeping formulas in the analytics layer rather than copying them into the facade.
- [Risk] CSV and XLSX can diverge from JSON. → Build both exporters from the same authorized report model and test representative fields, scope, permissions, and financial omission in both formats.
- [Risk] Financial or sector data can leak through totals or hidden cells. → Apply scope at query time, omit unauthorized sections entirely, and assert absence of forbidden values in serialized files.
- [Risk] XLSX memory use grows with volume. → Enforce period/row limits, avoid global auto-size, and reject oversized requests before workbook construction.

## Migration Plan

1. Add report permissions/configuration and report DTO/facade code without changing existing analytics contracts.
2. Add JSON endpoints and tests for consistency, authorization, scope, comparison, and deterministic insights.
3. Add CSV/XLSX exporters and tests for headers, filenames, types, injection, limits, and permission parity.
4. Run the full TASKS 01–10 regression suite, `clean test`, `build`, and strict OpenSpec validation.

Rollback is application-level: remove or disable report routes/exporters while retaining existing analytics and authorization data. Do not edit prior migrations; any seed adjustment must be a new forward migration.
