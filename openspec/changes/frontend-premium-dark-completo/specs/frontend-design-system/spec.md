# Spec Delta

## Purpose

Define a coherent Premium Dark visual language and accessible reusable UI primitives for all frontend screens.

## ADDED Requirements

### Requirement: Centralized Premium Dark language
The frontend SHALL use centralized visual tokens for color, typography, spacing, radius, borders, shadows, status colors, and responsive breakpoints.

#### Scenario: Consistent screen rendering
- **WHEN** a user navigates between modules
- **THEN** shared surfaces, controls, statuses, focus states, and spacing remain visually consistent without per-screen token duplication

### Requirement: Reusable accessible components
The frontend SHALL provide reusable controls for forms, cards, tables, filters, pagination, dialogs, drawers, toasts, charts, loading skeletons, empty states, error states, and forbidden states with keyboard and accessible-name support.

#### Scenario: Keyboard operation
- **WHEN** a user navigates a form or dialog with a keyboard
- **THEN** focus order, labels, expanded state, and confirmation actions remain operable and visible

### Requirement: Responsive industrial layout
The frontend SHALL adapt the shell, grids, charts, tables, and dialogs for desktop, notebook, tablet, and mobile widths without losing essential actions or data context.

#### Scenario: Mobile navigation
- **WHEN** the viewport is narrow
- **THEN** the sidebar becomes a usable drawer and content remains readable without uncontrolled horizontal overflow
