# AtlasOps AI — Design Planning

## 1. Design objective

The product design must make a technically complex system understandable without exposing unnecessary infrastructure detail to normal users.

Priorities:

- clear status;
- predictable navigation;
- recovery from partial failure;
- accessibility;
- visible progress;
- safe destructive actions;
- consistent tables and forms;
- small business-domain breadth.

## 2. Design principles

Users must understand whether a resource is uploading, processing, awaiting approval, ready, failed, stale or read-only.

Technical detail such as job attempts, projection lag and raw payloads belongs in administrative or diagnostic views.

Actions are classified as:

```text
SAFE
REVERSIBLE
SENSITIVE
DESTRUCTIVE
```

Sensitive and destructive actions require explicit confirmation, affected-resource summary, server authorization, audit and approval when required.

## 3. Information architecture

Admin:

```text
Dashboard
Customers
Requests
Documents
Approvals
Search
Activities
Operations
Integrations
Imports
Audit
Settings
Developers
```

Client:

```text
Home
Requests
Documents
Notifications
```

## 4. Core journeys

### Admin creates customer

```text
Customer list
→ Create customer
→ Validate
→ Save
→ Success
→ Customer detail
```

### Client creates request

```text
Request list
→ New request
→ Title, description and priority
→ Submit
→ Request detail
```

### Client uploads large document

```text
Select file
→ Local validation
→ Create upload session
→ Upload parts
→ Pause/resume/retry
→ Complete upload
→ Show processing
```

The upload manager remains available during navigation.

### Analyst reviews document

```text
Approval list
→ Document preview
→ Review extracted text and AI result
→ Approve or reject
→ Confirm
→ Activity and notification
```

### Operator recovers job

```text
Operations
→ Failed job
→ Attempts and correlation
→ Retry or replay
→ Reason
→ Approval when destructive
→ New execution
```

## 5. Shared design system

Foundations:

- typography;
- spacing;
- breakpoints;
- focus ring;
- elevation;
- status colors;
- tenant primary color.

Components:

- button;
- input;
- textarea;
- select;
- checkbox;
- badge;
- alert;
- toast;
- dialog;
- drawer;
- tabs;
- breadcrumb;
- card;
- table;
- pagination;
- progress;
- skeleton;
- empty state;
- error state;
- command palette;
- upload item;
- notification item;
- document-viewer shell.

## 6. Table infrastructure

Used by customers, requests, documents, approvals, jobs, integrations, imports and audit.

Required:

- server pagination;
- sorting;
- filters;
- URL synchronization;
- row selection;
- limited bulk actions;
- loading, empty, error and permission states;
- keyboard support;
- responsive fallback.

Bulk actions remain limited to customer activation/deactivation and document archive/reprocess.

## 7. Upload manager

Display file name, size, uploaded bytes, progress, speed, status and controls.

Statuses:

```text
QUEUED
UPLOADING
PAUSED
COMPLETING
PROCESSING
COMPLETED
FAILED
CANCELLED
```

Requirements:

- survive route changes;
- restore recoverable state after refresh;
- explain expired sessions;
- distinguish upload and processing failure;
- accessible progress announcement.

## 8. Notifications and activities

Notification center is user-focused: unread count, mark read and deep link.

Activity feed is resource/tenant-focused: actor, action, resource, time, summary and correlation where allowed.

They may share infrastructure but are different user concepts.

## 9. Search and command palette

Search groups results by customers, requests and documents, with keyword, semantic and hybrid modes.

Command palette shortcut:

```text
Ctrl/Cmd + K
```

Commands include navigate, search, create customer, create request, start upload and open operations. Sensitive actions open the standard confirmation flow.

## 10. Optimistic UI

Allowed for mark-read, simple priority, simple status and non-sensitive tags.

Not allowed for approval, legal hold, deletion, integration execution, destructive replay or maintenance mode.

## 11. Conflict handling

On `412 Precondition Failed`:

- explain that the resource changed;
- preserve local input;
- offer reload;
- show server version;
- allow retry after review;
- avoid silent overwrite.

## 12. Document preview

Initial views:

- PDF;
- image;
- extracted text;
- metadata;
- AI result;
- approval controls.

Avoid loading the full large file unnecessarily. Version comparison, collaborative annotation and OCR editing are excluded.

## 13. Dashboard

One page with active customers, requests by status, processed documents, processing duration, failed jobs, AI fallback count, upload volume and integration latency.

Do not build a dashboard designer or complete BI product.

## 14. Required UI states

Every major page defines:

- initial loading;
- background refresh;
- empty;
- validation error;
- server error;
- permission denied;
- tenant read-only;
- stale projection;
- degraded dependency;
- success;
- partial success.

Async pages distinguish command failure, job failure, projection delay and realtime disconnection.

## 15. Accessibility

- semantic structure;
- keyboard navigation;
- visible focus;
- labels;
- heading order;
- sufficient contrast;
- accessible dialogs;
- status announcements;
- reduced motion;
- no color-only status;
- table fallback.

Automation includes axe checks, Playwright keyboard smoke, upload-progress test and dialog focus-return test.

## 16. Responsive behavior

Desktop uses persistent navigation and dense tables.

Tablet uses collapsible navigation and drawer filters.

Mobile makes the client portal fully usable and provides essential admin inspection.

No native mobile application is planned.

## 17. Design artifacts and workflow

For each P0 capability create a flow, low-fidelity wireframe, component inventory, states matrix, error copy, accessibility notes and acceptance screenshots.

Workflow:

```text
Clarify
→ map journey
→ identify states and risks
→ low-fidelity design
→ technical review
→ component reuse
→ accessibility review
→ implementation
→ visual and functional validation
→ documentation
```

## 18. Design Definition of Done

Required states are implemented, keyboard and responsive behavior work, authorization remains server-side, ETag conflicts are tested, Playwright covers the critical flow and delivered UI matches the design spec.
