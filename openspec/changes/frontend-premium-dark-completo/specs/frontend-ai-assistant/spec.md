# Spec Delta

## Purpose

Define a safe frontend experience for the Backend V1 industrial assistant and RAG features, including grounded responses, authorization states, and degraded availability.

## ADDED Requirements

### Requirement: Grounded assistant interaction
The frontend SHALL send assistant questions only through the real authorized backend contract and SHALL display the returned answer, status, period, warnings, and internal sources when provided, without exposing chain-of-thought.

#### Scenario: Answer with evidence
- **WHEN** an authorized user asks an operational question
- **THEN** the frontend displays the backend answer and available grounding context using the same metrics returned by the backend

### Requirement: Safe assistant failure states
The frontend SHALL distinguish and present `SEM_DADOS`, `SEM_PERMISSAO`, `PRECISA_CLARIFICACAO`, and `INDISPONIVEL` without guessing values or silently broadening company, sector, financial, or audit scope.

#### Scenario: AI unavailable
- **WHEN** the assistant backend reports degraded or unavailable AI
- **THEN** the ERP remains navigable and the assistant displays an unavailable state without fabricating a response

### Requirement: RAG scope visibility
The frontend SHALL preserve company and sector scope returned by the backend for RAG documents and SHALL not offer cross-company or cross-sector content as available.

#### Scenario: Unauthorized document context
- **WHEN** a question would require an unauthorized document scope
- **THEN** the frontend shows a permission or no-data state returned by the backend and does not expose document content
