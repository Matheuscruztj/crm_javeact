# ADR-006 — Document Upload: Presigned S3/MinIO URLs (No Backend Proxy)

**Status:** Accepted  
**Date:** 2025-01-20

---

## Context

Documents can be large (>500MB per PROJECT-SCOPE §4). Routing file bytes through the backend API would consume thread pool, memory, and network bandwidth proportional to file size, degrading API performance for all users.

## Decision

We use **presigned S3 URLs** (MinIO-compatible). The backend never proxies file bytes:

1. Client registers metadata: `POST /api/v1/documents` → gets `documentId`
2. Client requests upload URL: `POST /api/v1/documents/{id}/upload-url` → gets presigned PUT URL (15min TTL)
3. Client uploads directly to MinIO via signed URL (bypasses API entirely)
4. Client confirms: `POST /api/v1/documents/{id}/confirm-upload` → triggers processing

## Consequences

**Positive:**

- API thread pool not blocked by file I/O
- Supports pause/resume (upload can be split into parts)
- Works for files >500MB without timeout risk
- MinIO handles bandwidth directly

**Negative:**

- Two-step flow adds client complexity (mitigated by UploadManager component)
- Presigned URL expires (15min); client must handle expiry and re-request
- MinIO must be accessible from client's network

**Mitigations:**

- UploadManager frontend component handles the full flow transparently
- Idempotency-Key on confirm-upload prevents duplicate confirmations
