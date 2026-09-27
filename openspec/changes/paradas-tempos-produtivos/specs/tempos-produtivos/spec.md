# Spec Delta

## Purpose

Disponibiliza cálculos derivados e consistentes para duração bruta, tempo total parado e tempo produtivo de cada Registro de Produção, preparando indicadores futuros sem antecipar paradas planejadas ou métricas avançadas.

## ADDED Requirements

### Requirement: Derived production time calculations

The system SHALL expose production duration metrics as derived values: raw duration equals production end minus start; stopped time equals the sum of active contained stops; productive time equals raw duration minus stopped time. Deactivated stops SHALL be excluded, and productive time MUST never be negative.

#### Scenario: Calculate production with two stops
- **WHEN** production runs 08:00–12:00 and active stops are 09:10–09:30 and 10:45–11:00
- **THEN** the response SHALL report 240 raw minutes, 35 stopped minutes, and 205 productive minutes

#### Scenario: Exclude a deactivated stop
- **WHEN** the second stop is deactivated
- **THEN** the response SHALL report 20 stopped minutes and 220 productive minutes

#### Scenario: Never return negative productive time
- **WHEN** valid stop data is calculated
- **THEN** productive minutes SHALL be zero or greater, and invalid overlapping/out-of-bound intervals SHALL have been rejected before calculation

### Requirement: Production period update compatibility

The system SHALL reject an update to a production start or end that would leave any existing stop outside the new production interval. An update ending exactly at a stop end SHALL be allowed. Production responses SHALL include raw, stopped, and productive minutes without requiring all stops in every paginated item.

#### Scenario: Reject production shrink around a stop
- **WHEN** production is 08:00–12:00 with a stop 11:30–11:45 and a client changes production end to 11:00
- **THEN** the system SHALL reject the update with a clear conflict message

#### Scenario: Allow production end at stop end
- **WHEN** the same production is changed to end at 11:45
- **THEN** the system SHALL allow the update and retain the contained stop
