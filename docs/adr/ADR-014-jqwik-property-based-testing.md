# ADR-014: Property-Based Testing with jqwik

**Status:** Accepted  
**Date:** 2026-07-20  
**Deciders:** Engineering Team

---

## Context

Unit tests with fixed examples verify specific scenarios but cannot cover the combinatorial space of valid inputs. High-confidence coverage of domain invariants requires generating hundreds of random inputs. Two frameworks were evaluated: QuickCheck-style (`junit-quickcheck`) and jqwik.

## Decision

Use **jqwik** as the property-based testing (PBT) framework for all Java modules.

## Rationale

- jqwik integrates natively with JUnit 5 via `@Property` annotations — no separate test runner
- Supports custom `@Provide` arbitraries with fluent API
- Shrinking: on failure, automatically reduces the input to a minimal counterexample
- Supports `@Tag` annotations for CI filtering (`./gradlew testProperty`)
- 100 tries per property as the default — configurable per test

## Consequences

**Positive:**

- Domain invariants (hash chain integrity, tenant isolation, SLA calculations) are verified for arbitrary inputs
- Regressions surface as minimal counterexamples, not just failure traces

**Negative:**

- Adds `net.jqwik:jqwik` as a test dependency
- PBT tests run slower than unit tests — excluded from `verifyFast` via `@Tag("property")`

## Conventions

```java
@Property(tries = 100)
@Tag("Feature: module-name, Property N: description")
void should_invariantName_for_anyValidInput(@ForAll("customArbitrary") Type value) { ... }
```
