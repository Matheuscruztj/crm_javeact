# ADR-016: Flyway for Database Migrations

**Status:** Accepted  
**Date:** 2026-07-20  
**Deciders:** Engineering Team

---

## Context

The project needs a deterministic, version-controlled database migration strategy that:

- Works with Spring Boot auto-configuration
- Supports rollback detection (Flyway does not rollback by default — this is intentional)
- Is compatible with PostgreSQL-specific DDL (pgvector, PostGIS)

## Decision

Use **Flyway** with sequential versioned migrations in `backend/app-boot/src/main/resources/db/migration/`.

## Naming Convention

```
V{YYYYMMDD}_{NNNN}__{description}.sql
```

Example: `V20250120_0004__create_requests_comments_documents_tables.sql`

## Rules

1. Migrations are **immutable once applied** to production — never modify an existing migration
2. DDL corrections go in a **new migration** (e.g., ADR-016 came from a table rename migration)
3. Migrations run automatically on application startup via Spring Boot integration
4. `flywayMigrate` Gradle task available for CI and manual runs

## Consequences

**Positive:**

- Schema evolution is auditable and reproducible
- `flyway_schema_history` table prevents re-applying migrations

**Negative:**

- Rollback requires a separate migration — Flyway Community does not support `undo`
- Table naming mismatches (JPA vs SQL) surface only at startup unless caught by integration tests

## Lessons Learned

V20250720_0019 was created to fix a mismatch between JPA `@Table(name=...)` annotations and migration DDL. Future migrations should be verified against JPA entities before commit.
