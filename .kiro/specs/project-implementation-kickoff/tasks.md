# Implementation Plan: AtlasOps AI — P0 Implementation (Pending Tasks)

## Overview

Remaining pending tasks for the P0 vertical slice. Completed tasks are in `finished-tasks.md`.

## Tasks

### 12. Approvals Module (Presentation)

- [x] 12.6 Implement ApprovalController
  - REST controller: POST `/api/v1/approvals/{id}/approve`, `/reject`, `/cancel`
  - GET `/api/v1/approvals?status=PENDING` (paginated listing)
  - _Requirements: 13.2, 13.3, 13.8_

---

### 13. Activities Module (Infrastructure + Presentation)

- [x] 13.4 Implement ActivityRepository JPA adapter and ActivityController
  - JPA adapter with tenant-scoped queries, entity filtering
  - REST controller: GET `/api/v1/activities`, GET `/api/v1/activities?entityType=...&entityId=...`
  - _Requirements: 14.2, 14.3, 14.4, 14.5_

---

### 14. Notifications Module (Infrastructure + Presentation)

- [x] 14.4 Implement NotificationRepository JPA adapter, EmailSender SMTP adapter, and SSE infrastructure
  - Complete JpaNotificationRepositoryAdapter (entity/SpringData already exist, need full adapter implementing port)
  - SMTP adapter: JavaMailSender with configurable host/port (MailHog in dev)
  - SSE: `SSEController` at GET `/api/v1/events/stream?token={jwt}` with heartbeat every 30s, Last-Event-ID reconnection support
  - _Requirements: 15.8, 16.2, 17.1, 17.2, 17.5, 17.6, 17.7_
- [x] 14.5 Implement NotificationController
  - GET `/api/v1/notifications` (paginated, ordered by createdAt desc)
  - PATCH `/api/v1/notifications/mark-read` (single/bulk)
  - GET `/api/v1/notifications/unread-count`
  - _Requirements: 15.4, 15.5, 15.7, 15.8_

---

### 15. Search Module (Presentation)

- [x] 15.5 Implement SearchController
  - REST controller: GET `/api/v1/search?q=...&page=...&size=...`
  - _Requirements: 18.1, 18.2, 18.3, 18.6_

---

### 16. Audit Module (Presentation)

- [x] 16.5 Implement AuditController
  - REST controller: GET `/api/v1/audit?actorId=...&actionType=...&from=...&to=...`
  - _Requirements: 19.1, 19.4, 19.5_

---

### 17. Checkpoint — All API Modules Complete

- [x] 17. Checkpoint — All API modules complete
  - All API modules implemented and tested
  - Worker module compiles successfully
  - _Status: Verified - all module tests pass_

---

### 18. Worker Process — Event Consumers and Processing Pipeline

- [x] 18.1 Implement Worker Redis Streams consumer infrastructure
  - Configure consumer groups (XREADGROUP) per stream with configurable group names
  - Implement batch consumption (up to 10 messages per XREADGROUP, 2s block timeout)
  - Implement XACK on successful processing only
  - On startup: claim pending messages idle >60s from previous runs (XAUTOCLAIM)
  - Redis reconnection with exponential backoff (1s, 2s, 4s, max 30s)
  - _Requirements: 20.1, 20.4, 20.6, 20.7, 20.8_
- [x] 18.2 Implement Worker retry and DLQ logic
  - Retry failed tasks 3x with exponential backoff (1s, 4s, 16s)
  - After all retries: move to `{originalStream}:dlq` preserving original content, attempt count, last error, timestamp
  - Log each attempt: task ID, attempt number, duration (ms), outcome
  - _Requirements: 20.2, 20.3, 20.5_
- [x] 18.3 Write property test for worker retry and DLQ progression (Property 24)
  - **Property 24: Worker Retry and DLQ Progression**
  - **Validates: Requirements 20.2, 20.3**
  - _Status: Completed - RetryAndDlqProgressionPropertyTest with 6 properties_
- [x] 18.4 Implement TextExtractionConsumer (documents.uploaded stream)
  - Consume `DocumentUploadedEvent`; skip if document status beyond UPLOADED (idempotency)
  - Extract text with Apache Tika for PDF/DOCX (up to 10MB text)
  - For images (PNG, JPEG): skip extraction, set empty text
  - On success: transition to TEXT_EXTRACTED, publish `DocumentReadyForAnalysisEvent`
  - Retry extraction 3x (1s, 5s, 30s); on exhaustion: PROCESSING_FAILED + DLQ
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.7_
- [x] 18.5 Write property test for idempotent document processing (Property 17)
  - **Property 17: Idempotent Document Processing**
  - **Validates: Requirements 11.7**
  - _Status: Completed - IdempotentDocumentProcessingPropertyTest with 4 properties_
- [x] 18.6 Implement PreviewGenerationConsumer for PDF documents
  - Generate PNG preview of first page (max 600x800 pixels) for PDF documents
  - Store at `{tenantId}/{year}/{month}/{documentId}/preview.png`
  - On failure: log and continue (non-blocking)
  - _Requirements: 11.6, 11.8_
- [x] 18.7 Implement AIAnalysisConsumer (documents.ready_for_analysis stream)
  - Consume `DocumentReadyForAnalysisEvent`; invoke `DocumentAnalysisPort`
  - On Ollama success: extract summary, category, key fields, risk indicators, confidence score
  - On Ollama unavailable/timeout (30s): execute deterministic fallback (category from content type, word count, empty risks, confidence 0.0, fallback=true)
  - Transition to ANALYZED, publish `DocumentAnalyzedEvent`
  - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_
- [x] 18.8 Write property test for AI fallback determinism (Property 18)
  - **Property 18: AI Fallback Determinism**
  - **Validates: Requirements 12.3**
  - _Status: Completed - AIFallbackDeterminismPropertyTest with 6 properties_
- [x] 18.9 Implement ApprovalCreationConsumer (documents.analyzed stream)
  - Consume `DocumentAnalyzedEvent`; invoke `CreatePendingApprovalUseCase`
  - Create PENDING approval for analyzed documents
  - _Requirements: 13.1_
- [x] 18.10 Implement NotificationEventConsumer (approvals.decided stream)
  - Consume `ApprovalDecisionEvent`; create in-app notification for CLIENT user
  - Push SSE event to connected user
  - Publish email notification to `notifications.email` stream
  - _Requirements: 15.1, 15.2, 16.1, 17.3, 17.4_
- [x] 18.11 Implement ActivityEventConsumer (activities.events stream)
  - Consume domain events (CustomerCreated, RequestStatusChanged, etc.)
  - Record activity entry with dedup via eventId
  - _Requirements: 14.1, 14.7, 14.8_
- [x] 18.12 Implement EmailConsumer (notifications.email stream)
  - Consume email notifications; send via SMTP (MailHog in dev)
  - Retry 3x (1s, 4s, 16s); DLQ on failure
  - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_

---

### 19. Checkpoint — Worker Process Complete

- [x] 19. Checkpoint — Worker process complete
  - All worker consumers implemented and tested
  - Unit tests pass for all new consumers
  - _Status: Verified - `./gradlew :backend:worker:test` passes_

---

### 20. Cross-Cutting Property Tests

- [x] 20.1 Write property test for pagination bounds enforcement (Property 28)
  - **Property 28: Pagination Bounds Enforcement**
  - **Validates: Requirements 6.10, 8.9, 14.4, 15.8, 18.6, 19.5**
  - _Status: Completed - PaginationBoundsEnforcementPropertyTest with 8 properties_

---

### 21. Frontend Authentication and Layout

- [x] 21.1 Implement login page with credential form and token management
  - Create `/app/login/page.tsx` with email/password form using shadcn/ui components
  - Store access token in memory, refresh token in secure storage
  - Redirect by role: ADMIN → /admin/dashboard, CLIENT → /portal/home, ANALYST → /admin/dashboard
  - Auto-refresh expired access tokens before retrying failed requests
  - _Requirements: 21.1, 21.2, 21.3, 21.4_
  - _Status: Completed - login page with form validation, token management, role-based redirect_
- [x] 21.3 Implement API client hook with auth headers and tenant context
  - Create `lib/api-client.ts` with Authorization Bearer header on every request
  - Include X-Tenant-ID header on every request
  - Implement token refresh interceptor
  - _Requirements: 21.3, 21.4_
  - _Status: Completed - full API client with auth headers, tenant context, and token refresh_

---

### 22. Frontend Admin Pages

- [x] 22.1 Implement admin dashboard page with summary cards
  - Create `/app/admin/dashboard/page.tsx` with cards: active customers, requests by status, documents processed, pending approvals
  - Fetch summary data from API endpoints
  - _Requirements: 22.1_
  - _Status: Completed - dashboard with stat cards, requests by status distribution, loading states_
- [x] 22.2 Implement customers list page with data table, search, and create form
  - Create `/app/admin/customers/page.tsx` with paginated data table, search input, status filter
  - Create new customer dialog/form with validation (name required, email required)
  - _Requirements: 22.2, 22.3_
  - _Status: Completed - data table with search, filters, pagination, create dialog with validation_
- [x] 22.3 Implement requests list page with filters and analyst assignment
  - Create `/app/admin/requests/page.tsx` with paginated data table, status filter, priority filter
  - Analyst assignment dropdown on request rows
  - _Requirements: 22.4_
  - _Status: Completed - requests table with status/priority filters, analyst assignment dropdown_
- [x] 22.4 Implement documents list page with status badges and approval actions
  - Create `/app/admin/documents/page.tsx` with status indicators (color-coded badges)
  - Links to approval actions from document rows
  - _Requirements: 22.5_
  - _Status: Completed - documents table with status badges, approval status, file icons, review links_
- [x] 22.5 Implement approvals page with approve/reject actions and rejection reason dialog
  - Create `/app/admin/approvals/page.tsx` listing pending approvals
  - Approve button + reject button with rejection reason dialog (10-1000 chars)
  - _Requirements: 22.6_
  - _Status: Completed - approvals list with approve/reject actions, rejection dialog with validation_
- [x] 22.6 Implement audit page with filterable log table
  - Create `/app/admin/audit/page.tsx` with action type filter, date range picker, actor filter
  - Paginated data table
  - _Requirements: 22.7_
  - _Status: Completed - audit log with action type filter, date range, actor filter, pagination_
- [x] 22.7 Implement activities page with global feed and infinite scroll
  - Create `/app/admin/activities/page.tsx` showing global activity feed
  - Infinite scroll pagination
  - _Requirements: 22.8_
  - _Status: Completed - activity feed with infinite scroll, entity type filter, activity icons_

---

### 23. Frontend Portal Pages

- [x] 23.1 Implement portal home page with summary cards
  - Create `/app/portal/home/page.tsx` with: open requests count, recent documents (last 5), unread notifications count
  - _Requirements: 23.1_
  - _Status: Completed - home page with stat cards, recent documents list, quick actions_
- [x] 23.2 Implement portal requests page with create form and detail view
  - Create `/app/portal/requests/page.tsx` listing client's requests with status badge, priority, creation date
  - New request form: title (required), description (required), priority (optional dropdown)
  - Detail page `/app/portal/requests/[id]/page.tsx` with info, documents, comments thread, status timeline
  - _Requirements: 23.2, 23.3, 23.4_
  - _Status: Completed - requests list with filters, create dialog, detail page with tabs_
- [x] 23.3 Implement portal documents page with upload component
  - Create `/app/portal/documents/page.tsx` listing documents with status and upload date
  - Upload page `/app/portal/documents/upload/page.tsx` with drag-and-drop, file type validation, progress indicator, cancel/retry
  - _Requirements: 23.5, 23.6_
  - _Status: Completed - documents grid, drag-and-drop upload with validation and progress_
- [x] 23.4 Implement portal notifications page
  - Create `/app/portal/notifications/page.tsx` with read/unread indicators
  - Mark-as-read functionality (single and bulk)
  - _Requirements: 23.7_
  - _Status: Completed - notifications list with read/unread indicators, bulk mark-as-read_

---

### 24. Frontend Search and Command Palette

- [x] 24.1 Implement global search with command palette (Ctrl+K / Cmd+K)
  - Create command palette component using shadcn/ui Dialog/Command
  - Display results grouped by entity type (customers, requests, documents)
  - Keyboard navigation (arrow keys, Enter to select, Escape to close)
  - Debounce input by 300ms
  - Loading skeleton while fetching, "No results found" for empty results
  - Navigate to entity detail page on selection
  - _Requirements: 24.1, 24.2, 24.3, 24.4, 24.5, 24.6, 24.7_
  - _Status: Completed - CommandPalette component with keyboard navigation, grouping, debounce_

---

### 25. Frontend Real-Time Updates via SSE

- [x] 25.1 Implement SSE client hook with reconnection and notification integration
  - Create `hooks/use-sse.ts` establishing SSE connection after auth
  - Display toast notification on new events, increment unread badge count
  - Update document progress indicator on processing events
  - Reconnect with exponential backoff (1s, 2s, 4s, max 30s) + Last-Event-ID
  - Close connection on logout
  - _Requirements: 25.1, 25.2, 25.3, 25.4, 25.5_
  - _Status: Completed - useSSE, useSSENotifications, useSSEDocumentProgress hooks_

---

### 26. Final Checkpoint — Full System Integration

- [x] 26. Final checkpoint — Full system integration
  - Ensure all tests pass, ask the user if questions arise.
  - _Status: Completed - All tests pass including architecture validation. Architecture violations in presentation layer accessing domain ports directly were fixed by creating application layer use cases (ValidateTokenUseCase, GetUserByIdUseCase, ServiceRequestPageResult)._

---

## Notes

- Tasks marked with `[ ]` are pending
- Tasks marked with `*` are optional property-based test tasks that can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at logical boundaries
- Completed tasks are in `finished-tasks.md`

## Task Dependency Graph

```json
{
  "waves": [
    {
      "id": 0,
      "tasks": ["12.6", "13.4", "14.4", "15.5", "16.5"],
      "description": "Controllers + remaining infra for API modules"
    },
    {
      "id": 1,
      "tasks": ["14.5"],
      "description": "NotificationController (depends on 14.4 SSE infra)"
    },
    {
      "id": 2,
      "tasks": ["18.1"],
      "description": "Worker Redis Streams consumer infrastructure"
    },
    {
      "id": 3,
      "tasks": ["18.2"],
      "description": "Worker retry and DLQ logic"
    },
    {
      "id": 4,
      "tasks": [
        "18.3",
        "18.4",
        "18.6",
        "18.7",
        "18.9",
        "18.10",
        "18.11",
        "18.12"
      ],
      "description": "Worker event consumers and PBT for retry/DLQ"
    },
    {
      "id": 5,
      "tasks": ["18.5", "18.8", "20.1"],
      "description": "Worker property tests + pagination bounds PBT"
    },
    {
      "id": 6,
      "tasks": ["21.1", "21.3"],
      "description": "Frontend auth + API client"
    },
    {
      "id": 7,
      "tasks": [
        "22.1",
        "22.2",
        "22.3",
        "22.4",
        "22.5",
        "22.6",
        "22.7",
        "23.1",
        "23.2",
        "23.3",
        "23.4"
      ],
      "description": "Frontend admin + portal pages"
    },
    {
      "id": 8,
      "tasks": ["24.1", "25.1"],
      "description": "Frontend search + SSE"
    }
  ]
}
```
