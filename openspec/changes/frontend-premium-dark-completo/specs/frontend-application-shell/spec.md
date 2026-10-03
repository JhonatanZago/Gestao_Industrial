# Spec Delta

## Purpose

Define the secure and reusable application shell that authenticates users and routes them through the industrial ERP according to backend contracts and permissions.

## ADDED Requirements

### Requirement: Secure session and navigation
The frontend SHALL authenticate through the real login contract, keep the access token only in the approved client-side session strategy, attach it to protected requests, and provide logout by discarding the client session.

#### Scenario: Successful login
- **WHEN** a user submits valid credentials
- **THEN** the frontend stores the returned session data without logging secrets and navigates to an authorized landing page

#### Scenario: Unauthorized session
- **WHEN** a protected request returns 401 or 403
- **THEN** the frontend clears or preserves the session according to the status and shows a safe access message without exposing stack traces

### Requirement: Permission-aware application shell
The frontend SHALL expose routes and menu entries only when supported by the authenticated user's real permissions, while still handling direct unauthorized navigation with a forbidden state.

#### Scenario: Restricted menu
- **WHEN** the authenticated user lacks a permission for a module
- **THEN** that module is absent from the menu and its route rejects direct access

### Requirement: Consistent operational states
The shell SHALL present loading, empty, retryable error, backend unavailable, rate-limit, and correlation-ID states consistently across features.

#### Scenario: Correlated backend error
- **WHEN** an API error includes a correlation ID
- **THEN** the frontend shows a safe support code that the user can copy without revealing internal details
