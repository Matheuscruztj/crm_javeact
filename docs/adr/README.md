# Architecture Decision Records (ADR)

This directory contains Architecture Decision Records for the AtlasOps AI project.

## What is an ADR?

An ADR captures an architecturally significant decision along with its context and consequences. Once accepted, ADRs are immutable — they can only be superseded by new ADRs.

## Naming Convention

```
ADR-{NNN}-{short-title-kebab-case}.md
```

## Statuses

- **Proposed** — Under discussion, not yet accepted
- **Accepted** — Approved and in effect
- **Deprecated** — No longer relevant (superseded or context changed)
- **Superseded** — Replaced by a newer ADR (link to successor)

## Creating a New ADR

1. Copy `ADR-000-template.md` to the next available number
2. Fill in all sections
3. Submit as PR for team review
4. Update status to `Accepted` upon approval

## Index

| ADR                                          | Title                                  | Status   | Date       |
| -------------------------------------------- | -------------------------------------- | -------- | ---------- |
| [ADR-000](ADR-000-template.md)               | Template                               | N/A      | -          |
| [ADR-001](ADR-001-hexagonal-architecture.md) | Hexagonal Architecture per Module      | Accepted | 2025-01-15 |
| [ADR-002](ADR-002-transactional-outbox.md)   | Transactional Outbox for Domain Events | Accepted | 2025-07-19 |
