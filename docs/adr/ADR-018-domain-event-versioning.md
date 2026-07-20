# ADR-018: Domain Event Versioning

**Status:** Accepted  
**Date:** 2026-07-20  
**Deciders:** Engineering Team

---

## Context

Domain events published to Redis Streams must carry a stable, versioned type identifier. Initially events used Java class simple names (`CustomerCreatedEvent`), which is brittle — renaming a class breaks consumers.

## Decision

All `DomainEvent` subclasses produce a **versioned kebab-case type string** via `getEventType()`.

## Format

```
{entity}.{action}.v{N}
```

Examples:

- `customer.created.v1`
- `document.analyzed.v1`
- `request.status-changed.v1`
- `approval.decided.v1`

## Implementation

`DomainEvent.getEventType()` default implementation converts the class name via CamelCase → kebab-case + `.v1` suffix. Subclasses can override to provide an explicit stable name.

## Versioning Rules

1. `v1` = initial version
2. Breaking schema changes → new version (`customer.created.v2`)
3. Old and new version consumers coexist during migration window
4. Event `v1` consumers ignore unknown fields (schema evolution friendly)

## Consequences

**Positive:**

- Class renaming no longer breaks consumers
- Consumers can version-gate their logic (`if eventType.endsWith(".v2")`)

**Negative:**

- Requires discipline: class renames must preserve `getEventType()` return value
- Default implementation relies on naming convention — teams must document overrides
