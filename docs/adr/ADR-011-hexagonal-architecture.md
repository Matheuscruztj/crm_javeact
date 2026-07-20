# ADR-011 — Hexagonal Architecture (Ports and Adapters) for All Modules

**Status:** Accepted | **Date:** 2025-01-20

## Context

With 19 backend modules, we need a consistent architectural pattern that allows:

- Swapping infrastructure implementations without touching business logic
- Testing domain logic without Spring context or database
- Clear dependency rules enforced automatically

## Decision

Every module follows the **hexagonal architecture** (Ports and Adapters):

```
domain/         — Entities, Value Objects, Domain Events, Domain Services (pure Java)
domain/ports/   — Input/output port interfaces (no framework dependencies)
application/    — Use Cases, Commands, Queries (orchestrates domain)
infrastructure/ — Adapters: JPA, Redis, S3, Ollama, HTTP (implements ports)
presentation/   — REST Controllers, Event Consumers (calls use cases)
```

**Dependency rules** (enforced by ArchUnit):

- `domain` imports only shared-kernel
- `application` imports domain + shared-kernel only
- `infrastructure` implements `domain/ports/` interfaces
- `presentation` calls `application` use cases only
- No cycles between modules

## Consequences

**Positive:** 100% of domain logic testable without Spring; swap adapters (e.g., PostgreSQL → OpenSearch) without domain changes; ArchUnit fails CI if rules violated  
**Negative:** More files per feature; adapter boilerplate (mitigated by code generation patterns)
