# Spec Delta

## Purpose

Define a safe, repeatable load-validation contract using synthetic data and mocked external AI, measuring technical behavior under representative backend traffic without claiming business availability or capacity guarantees.

## ADDED Requirements

### Requirement: Load validation SHALL use bounded synthetic data

Load tests SHALL generate or provision documented synthetic data covering companies, sectors, people, parts, machines, production, stops, goals, standard times, values, and audit records without committing giant fixture files or real secrets.

#### Scenario: Load dataset is prepared
- **WHEN** the load profile initializes its dataset
- **THEN** the dataset volume, generator version, database, environment, and cleanup behavior are recorded and no production data is used

### Requirement: The load profile SHALL cover representative protected flows

The selected load tool SHALL exercise login, main Dashboard, sector and employee drill-downs, and production queries with valid authorization contexts; external AI SHALL be mocked in the standard profile.

#### Scenario: Protected traffic is generated
- **WHEN** the load profile runs with multiple authorized contexts
- **THEN** requests preserve company/sector isolation and report latency, throughput, status distribution, and authorization failures by scenario

### Requirement: Load results SHALL observe system bottlenecks without inventing an SLA

Load validation SHALL observe CPU, memory, GC, Hikari pool, query behavior, response size, latency percentiles, and 5xx rate, and SHALL compare results with the baseline instead of asserting an unsupported business SLA.

#### Scenario: Load exposes degradation
- **WHEN** latency, pool saturation, errors, or resource use exceeds the recorded technical baseline
- **THEN** the report identifies the affected scenario and bottleneck evidence, and does not mask failures by relaxing security or returning fabricated data
