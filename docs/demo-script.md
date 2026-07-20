# AtlasOps AI — Demo Script (End-to-End Walkthrough)

> **Validates:** P3.8.3 — Demo script documentado  
> **Duration:** ~15 minutes  
> **Prerequisites:** `make bootstrap` completed

---

## Setup

```bash
make compose-up     # Start infrastructure
make seed-demo      # Load demo data (Tenant Alpha + Beta, users, customers)
# Frontend: cd frontend && pnpm dev → http://localhost:3000
# API docs: http://localhost:8080/swagger-ui.html
```

**Demo credentials:**
| Role | Email | Password |
|------|-------|----------|
| ADMIN | admin@atlasops.test | admin-demo-2025 |
| ANALYST | analyst@atlasops.test | analyst-demo-2025 |
| CLIENT | client@atlasops.test | client-demo-2025 |

---

## Scene 1 — Admin Login and Customer Management (3 min)

1. Open `http://localhost:3000`, land on login page
2. Login as **ADMIN** → redirected to `/admin/dashboard`
3. Navigate to **Customers** → see paginated list from seed data
4. Create a new customer: "Demo Corp", `demo@corp.com`
   - Show: form validation (email format, name length)
   - Show: 201 Created with Location header in Network tab
5. Click into customer detail → show activate/deactivate toggle
6. Show audit trail: `/admin/audit` → filter by actor "admin@atlasops.test"

**Key points to highlight:**

- Tenant isolation: X-Tenant-ID header in every request
- Optimistic locking: try to edit same customer in two tabs → 412 Precondition Failed
- No data from other tenants visible

---

## Scene 2 — Client Portal and Document Upload (4 min)

1. Open incognito window, login as **CLIENT**
2. Land on `/portal/home` → show summary cards (open requests, recent docs)
3. Create a service request: "Annual Report Analysis"
4. Navigate to `/portal/documents/upload`
5. Upload a PDF document (use `infra/seed/demo-document.pdf` if available)
   - Show: upload progress bar
   - Show: status changes UPLOADED → TEXT_EXTRACTED → ANALYZED
6. Watch the notification bell → new notification appears via SSE
7. Open notification → links to document detail

**Key points:**

- Presigned URL upload (no bytes through API)
- SSE real-time updates (heartbeat visible in Network tab → EventStream)
- Redis stream → Worker → AI analysis pipeline

---

## Scene 3 — Document Analysis and Approval (4 min)

1. Login as **ANALYST**
2. Navigate to `/admin/documents` → find the uploaded document
3. Open document detail → show AI analysis result:
   - Summary, category, confidence score, fallback=false (Ollama available)
   - Or fallback=true (Ollama unavailable) — explain deterministic fallback
4. Navigate to `/admin/approvals` → see pending approval for the document
5. Click Approve → ledger entry created
6. Show ledger verification: `GET /api/v1/approvals/{id}/verify` in Swagger
   - `integrityValid: true`, `tamperingDetected: false`
7. Navigate to `/admin/activities` → see the approval activity in real-time feed

**Key points:**

- Approval state machine: PENDING → APPROVED (immutable)
- Hash chain ledger: every approval decision is tamper-evident
- Domain events published: `ApprovalDecisionEvent` → notification to CLIENT

---

## Scene 4 — Operations and Observability (2 min)

1. Navigate to `/admin/operations` → show job list
   - If worker jobs exist: show progress bars, retry/cancel buttons
2. Open Grafana: `http://localhost:3001`
   - Show AI metrics dashboard: analysis duration, fallback rate
   - Show alert rules in Alertmanager (if configured)
3. Run k6 smoke test in terminal:
   ```bash
   make test-load-smoke
   ```
4. Show `make verify-ledger` output

---

## Scene 5 — Cross-Tenant Isolation (2 min)

1. In API tab, try accessing Tenant Beta data with Tenant Alpha token:
   ```bash
   curl -H "Authorization: Bearer $ALPHA_TOKEN" \
        -H "X-Tenant-ID: tenant-beta" \
        http://localhost:8080/api/v1/customers
   # Expected: 403 Forbidden — tenant mismatch
   ```
2. Show ResourceAuthorizationFilter blocking the request
3. Show that CLIENT user cannot access other customer's data

---

## Teardown

```bash
make compose-down
```
