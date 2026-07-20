# ADR-017: Redis-Based Distributed Locking

**Status:** Accepted  
**Date:** 2026-07-20  
**Deciders:** Engineering Team

---

## Context

Multiple concurrent workers may process the same document upload completion or trigger projection rebuilds simultaneously. Without coordination, this causes duplicate processing and data corruption.

## Decision

Use **Redis SET NX PX** for distributed locking via `RedisDistributedLockAdapter`.

## Implementation

```
Lock:    SET lock:{key} {ownerToken} NX PX {ttlMs}
Fence:   INCR fence:{key}  → monotonically increasing fencing token
Release: Lua script — checks ownerToken before DEL (safe release)
```

## Key Properties

- **NX (Not eXists):** Atomic acquisition — only one caller succeeds
- **PX (expiry in ms):** Auto-release if owner crashes (prevents deadlock)
- **Fencing token:** Prevents stale writers from overwriting newer data
- **Safe release:** Lua script ensures only the lock owner can release it

## Alternatives Considered

- Zookeeper/etcd: operational overhead exceeds value for this scale
- PostgreSQL advisory locks: couples distributed lock to database availability
- Redlock (multi-node): overkill for a single Redis instance dev environment

## Consequences

**Positive:**

- Zero additional infrastructure (Redis already required for Streams + cache)
- Lock TTL prevents deadlocks from crashed processes

**Negative:**

- Clock skew between Redis and application can affect TTL precision
- Redis restart clears all locks — in-flight operations may proceed without lock (mitigated by TTL and fencing tokens)
