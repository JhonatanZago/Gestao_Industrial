# Spec Delta

## Purpose

Permite registrar, consultar e administrar interrupções dentro de uma produção, garantindo pertencimento empresarial, limites temporais, ausência de sobreposição e preservação histórica por exclusão lógica.

## ADDED Requirements

### Requirement: Production stop registration

The system SHALL allow a client to create a ParadaProducao only within an existing active Registro de Produção, using an active MotivoParada from the same Empresa. The stop interval MUST have end strictly after start and MUST be fully contained within the production interval, including exact production boundaries.

#### Scenario: Create a valid stop
- **WHEN** an active production runs from 08:00 to 12:00 and the client submits a stop from 09:10 to 09:30 with an active same-company reason
- **THEN** the system SHALL create the stop and return a duration of 20 minutes

#### Scenario: Allow exact production boundaries
- **WHEN** a stop starts at production start or ends at production end
- **THEN** the system SHALL accept it if its duration is positive and it remains contained

#### Scenario: Reject stop outside production
- **WHEN** a stop starts before production, ends after production, or covers the entire production plus outside time
- **THEN** the system SHALL return conflict and SHALL NOT persist the stop

### Requirement: Stop temporal consistency

The system SHALL reject zero or negative stop intervals and SHALL reject active stops overlapping another active stop of the same production. Adjacent stops SHALL be allowed. Overlap detection SHALL use a database existence query and SHALL ignore the stop itself on update.

#### Scenario: Reject overlapping stops
- **WHEN** an active stop exists from 09:00 to 09:30 and a new stop overlaps it from 09:20 to 09:45
- **THEN** the system SHALL return conflict and SHALL NOT persist the new stop

#### Scenario: Allow adjacent stops
- **WHEN** one stop ends at 09:30 and another starts at exactly 09:30
- **THEN** the system SHALL allow the second stop

### Requirement: Stop lifecycle and revalidation

The system SHALL allow scoped view, list, update, activation, and deactivation without physical deletion. Updates and reactivation MUST revalidate production activity, reason activity, containment, company ownership, and overlap. Deactivated stops SHALL remain persisted and SHALL not contribute to current stop-time calculations.

#### Scenario: Reactivate a stop with conflict
- **WHEN** a client reactivates a deactivated stop whose interval now conflicts with another active stop
- **THEN** the system SHALL reject reactivation and keep the stop inactive

#### Scenario: Do not add stop to inactive production
- **WHEN** a client attempts to create a stop for an inactive production
- **THEN** the system SHALL return conflict and SHALL NOT create the stop

### Requirement: Stop API and isolation

The system SHALL expose paginated stop endpoints under `/api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas`, return DTOs rather than JPA entities, and validate the Empresa–Produção–Parada scope on every operation. A stop from another Empresa MUST NOT be disclosed or changed.

#### Scenario: Access stop through wrong Empresa or production
- **WHEN** a client uses an Empresa or production scope that does not own the stop
- **THEN** the system SHALL return not found and SHALL NOT modify the stop
