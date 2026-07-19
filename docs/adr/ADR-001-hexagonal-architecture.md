# ADR-001: Hexagonal Architecture per Module

> **Status:** Accepted  
> **Date:** 2025-01-15  
> **Authors:** AtlasOps Team

---

## Context

The AtlasOps AI platform needs a clear separation between business logic and infrastructure concerns. The system integrates with multiple external services (PostgreSQL, Redis, MinIO, Ollama) and must allow these to be replaced or evolved independently of domain rules.

The monorepo contains 18+ modules, each representing a bounded context. Without a consistent architecture pattern, coupling between layers would grow uncontrollably.

## Decision

Each backend module follows **Hexagonal Architecture** (Ports and Adapters) with four standard packages:

```
domain/         → Entities, Value Objects, Domain Events, Domain Services
domain/ports/   → Interfaces (ports) defining contracts
application/    → Use Cases, Commands, Queries
infrastructure/ → Adapters implementing ports (JPA, Redis, S3, AI)
presentation/   → REST Controllers, Event Consumers
```

Dependency rules (enforced by ArchUnit):

- `domain` depends only on `shared-kernel`
- `application` depends on `domain` + `shared-kernel`
- `infrastructure` implements `domain/ports`
- `presentation` depends on `application`

## Consequences

### Positive

- Domain logic is completely isolated from frameworks
- Infrastructure adapters can be swapped without touching business rules
- Each layer is independently testable (domain = pure unit tests, application = mocked ports)
- ArchUnit provides automated enforcement of architectural rules

### Negative

- More boilerplate (port interfaces, adapter classes, package-info files)
- Learning curve for developers unfamiliar with hexagonal patterns
- Some simple CRUD operations feel over-engineered

### Neutral

- All modules follow the same structure, making navigation predictable
- `shared-kernel` serves as the foundation for cross-cutting types

## Alternatives Considered

### Alternative 1: Layered Architecture (traditional MVC)

Simpler but allows domain to accidentally depend on frameworks. Rejected due to long-term maintainability concerns.

### Alternative 2: Clean Architecture (Uncle Bob)

Very similar to hexagonal but with more prescribed layers. Rejected as overly prescriptive for our module sizes.

## References

- [Alistair Cockburn - Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Module Structure Steering File](.kiro/steering/module-structure.md)
