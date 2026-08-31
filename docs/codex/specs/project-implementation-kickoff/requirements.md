# Requirements Document

## Introduction

This specification defines the requirements for implementing the P0 (Priority Zero) foundation of the AtlasOps AI platform. The goal is to make the primary end-to-end journey functional: from customer creation through document upload, AI analysis, approval workflow, real-time notifications, and audit logging.

The project currently has scaffolded module structures with hexagonal architecture but only `shared-kernel` and `ai` contain real implementations. This spec covers implementing the business logic across 13 modules (Auth, Tenants, Users, Customers, Requests, Documents, Worker, Approvals, Activities, Notifications, Search, Audit, and Frontend) to deliver a working vertical slice.

## Glossary

- **System**: The AtlasOps AI platform as a whole
- **Auth_Module**: The authentication and authorization module handling JWT tokens and role-based access
- **Tenant_Module**: The multi-tenant management module responsible for tenant lifecycle and isolation
- **User_Module**: The user management module handling profiles and role assignments per tenant
- **Customer_Module**: The customer management module for CRUD operations on business customers
- **Request_Module**: The service request module managing workflow status transitions
- **Document_Module**: The document management module handling metadata, upload, and processing orchestration
- **Worker_Process**: The asynchronous background process consuming Redis Streams for heavy operations
- **Approval_Module**: The approval workflow module for document review decisions
- **Activity_Module**: The activity feed module recording domain events as timeline entries
- **Notification_Module**: The notification delivery module supporting in-app, email, and SSE channels
- **Search_Module**: The unified search module supporting keyword queries across entities
- **Audit_Module**: The immutable append-only audit ledger for critical actions
- **Frontend_App**: The Next.js frontend application with admin and portal interfaces
- **JWT**: JSON Web Token used for stateless authentication
- **Tenant**: An isolated organizational unit within the multi-tenant platform
- **Role**: A permission level assigned to users (ADMIN, ANALYST, CLIENT)
- **Request_Status**: The lifecycle state of a service request (OPEN, IN_PROGRESS, WAITING_CUSTOMER, COMPLETED, CANCELLED)
- **Approval_Status**: The lifecycle state of an approval (PENDING, APPROVED, REJECTED, CANCELLED)
- **Redis_Streams**: The message queue backbone used for asynchronous event distribution
- **MinIO**: S3-compatible object storage used for document files
- **SSE**: Server-Sent Events used for real-time delivery of notifications and progress
- **DLQ**: Dead Letter Queue for messages that fail processing after retry attempts
- **Correlation_ID**: A UUID propagated across requests and events for distributed tracing

## Requirements

### Requirement 1: User Authentication

**User Story:** As a user, I want to authenticate with credentials and receive a JWT token, so that I can access protected resources according to my role.

#### Acceptance Criteria

1. WHEN a user submits valid credentials (email and password), THE Auth_Module SHALL return an access token (JWT) and a refresh token within 2 seconds of the request
2. WHEN a user submits invalid credentials (incorrect email or incorrect password), THE Auth_Module SHALL return a 401 Unauthorized error with a generic message that does not reveal which field is incorrect
3. THE Auth_Module SHALL include the user identifier, tenant identifier, and role in the JWT claims
4. WHEN a valid refresh token is submitted, THE Auth_Module SHALL issue a new access token, rotate the refresh token (returning a new one), and invalidate the previously issued refresh token so it cannot be reused
5. WHEN an expired or invalid refresh token is submitted, THE Auth_Module SHALL return a 401 Unauthorized error and invalidate all refresh tokens associated with that user in Redis
6. WHEN a user requests logout, THE Auth_Module SHALL invalidate the refresh token stored in Redis so it cannot be reused
7. THE Auth_Module SHALL set access token expiration to a configurable duration (default 15 minutes, minimum 5 minutes, maximum 60 minutes)
8. THE Auth_Module SHALL set refresh token expiration to a configurable duration (default 7 days, minimum 1 day, maximum 30 days)
9. WHEN a request contains an expired access token, THE Auth_Module SHALL return a 401 Unauthorized error with code TOKEN_EXPIRED
10. IF a user submits credentials with a missing or empty email or password (fewer than 1 character), THEN THE Auth_Module SHALL return a 400 Bad Request error indicating which fields failed validation without attempting authentication
11. IF a user fails authentication 5 consecutive times for the same email within a 15-minute window, THEN THE Auth_Module SHALL temporarily lock the account for 15 minutes and return a 429 Too Many Requests error on subsequent attempts
12. WHEN a user successfully authenticates, THE Auth_Module SHALL reset the failed authentication attempt counter for that email to zero

---

### Requirement 2: Role-Based Authorization

**User Story:** As an administrator, I want to restrict access to resources based on user roles, so that users only perform actions appropriate to their permissions.

#### Acceptance Criteria

1. THE Auth_Module SHALL recognize exactly three roles — ADMIN, ANALYST, and CLIENT — and reject any JWT containing a role value outside this set with a 401 Unauthorized error
2. WHEN a user with role CLIENT attempts to access an ADMIN-only endpoint, THE Auth_Module SHALL return a 403 Forbidden error
3. WHEN a user with role CLIENT attempts to access an ANALYST-only endpoint, THE Auth_Module SHALL return a 403 Forbidden error
4. WHEN a user with role ANALYST attempts to access an ADMIN-only endpoint, THE Auth_Module SHALL return a 403 Forbidden error
5. THE Auth_Module SHALL validate the tenant context from the JWT matches the X-Tenant-ID header for every authenticated request
6. IF a request contains a mismatched tenant context (JWT tenant differs from X-Tenant-ID header), THEN THE Auth_Module SHALL return a 403 Forbidden error
7. IF a request does not contain a valid JWT (missing, malformed, or expired), THEN THE Auth_Module SHALL return a 401 Unauthorized error and deny access to any protected endpoint
8. IF an authenticated request is missing the X-Tenant-ID header, THEN THE Auth_Module SHALL return a 400 Bad Request error indicating the missing tenant header
9. WHILE a user with role CLIENT is authenticated, THE Auth_Module SHALL restrict data access to resources belonging exclusively to that user's own customer record within the tenant

---

### Requirement 3: Multi-Tenant Management

**User Story:** As a platform administrator, I want to create and manage tenants, so that each organization operates in complete isolation.

#### Acceptance Criteria

1. WHEN an ADMIN creates a tenant with a unique name between 3 and 100 characters containing only alphanumeric characters, hyphens, and spaces, THE Tenant_Module SHALL persist the tenant with a generated identifier, creation timestamp, and ACTIVE status
2. THE Tenant_Module SHALL enforce uniqueness of tenant name across the platform using case-insensitive comparison
3. IF a tenant creation request uses a name already taken, THEN THE Tenant_Module SHALL reject the request with an error indicating the tenant name is already in use
4. WHEN an ADMIN requests deactivation of an existing ACTIVE tenant, THE Tenant_Module SHALL set the tenant status to INACTIVE and prevent all users of that tenant from authenticating on subsequent requests
5. WHEN a tenant identifier is provided, THE Tenant_Module SHALL return the tenant representation including name, status, and creation timestamp
6. IF a retrieval request references a tenant identifier that does not exist, THEN THE Tenant_Module SHALL return an error indicating the tenant was not found

---

### Requirement 4: Cross-Tenant Data Isolation

**User Story:** As a tenant user, I want my data to be completely invisible to users from other tenants, so that business confidentiality is maintained.

#### Acceptance Criteria

1. THE System SHALL filter all data queries by the authenticated user's tenant identifier
2. WHEN a user attempts to access a resource belonging to a different tenant, THE System SHALL return a 404 Not Found (not 403) to avoid confirming resource existence
3. THE System SHALL include the tenant identifier in all database queries as a mandatory filter condition
4. FOR ALL entities that belong to a tenant, THE System SHALL store and validate the tenant identifier as a non-nullable column
5. THE System SHALL enforce tenant isolation at the repository port level by requiring tenant identifier as a mandatory parameter in all query methods
6. IF a database query returns a result whose tenant identifier does not match the authenticated tenant, THEN THE System SHALL treat the result as non-existent and not expose it

---

### Requirement 5: User Management

**User Story:** As an administrator, I want to manage users within my tenant, so that I can onboard team members and assign appropriate roles.

#### Acceptance Criteria

1. WHEN an ADMIN creates a user with email, name, password, and role, THE User_Module SHALL validate the input (email must be well-formed, name must be between 2 and 100 characters, password must be at least 8 characters, and role must be one of ADMIN, ANALYST, or CLIENT), persist the user associated with the ADMIN's tenant, and return the created user identifier
2. IF a user creation request contains invalid or missing fields, THEN THE User_Module SHALL reject the request with a validation error indicating which fields failed
3. THE User_Module SHALL enforce email uniqueness within a tenant (case-insensitive comparison)
4. IF a user creation request uses an email already registered in the same tenant, THEN THE User_Module SHALL return a 409 Conflict error
5. WHEN an ADMIN updates a user's role to a valid role (ADMIN, ANALYST, or CLIENT), THE User_Module SHALL persist the new role and the change takes effect on the user's next authentication
6. IF an ADMIN attempts to assign a role that is not one of ADMIN, ANALYST, or CLIENT, THEN THE User_Module SHALL reject the request with a validation error
7. WHEN an ADMIN deactivates a user other than themselves, THE User_Module SHALL mark the user as inactive and prevent that user from authenticating
8. IF an ADMIN attempts to deactivate their own account, THEN THE User_Module SHALL reject the request with an error indicating self-deactivation is not allowed
9. THE User_Module SHALL store user passwords using bcrypt with a minimum cost factor of 10

---

### Requirement 6: Customer Management

**User Story:** As an administrator, I want to create and manage customers within my tenant, so that I can track business relationships.

#### Acceptance Criteria

1. WHEN an ADMIN creates a customer with name (1 to 150 characters), email (validated per shared-kernel Email value object), and optional address, THE Customer_Module SHALL persist the customer with a generated identifier, ACTIVE status, and creation timestamp
2. THE Customer_Module SHALL enforce customer email uniqueness within a tenant (case-insensitive comparison)
3. IF a customer creation request uses an email already registered in the same tenant, THEN THE Customer_Module SHALL return a 409 Conflict error
4. WHEN an ADMIN updates customer fields (name, email, address), THE Customer_Module SHALL persist all changes atomically
5. IF an update or deactivation targets a customer that does not exist in the tenant, THEN THE Customer_Module SHALL return a 404 Not Found error
6. WHEN an ADMIN deactivates a customer, THE Customer_Module SHALL set the customer status to INACTIVE and prevent new requests from being created for that customer
7. WHEN an ADMIN activates an INACTIVE customer, THE Customer_Module SHALL set the customer status to ACTIVE
8. IF an activation is attempted on an already ACTIVE customer or deactivation on an already INACTIVE customer, THEN THE Customer_Module SHALL return a 422 Unprocessable Entity error
9. WHEN a search query of at least 2 characters is submitted, THE Customer_Module SHALL return customers matching by name or email (case-insensitive partial match)
10. THE Customer_Module SHALL support paginated listing with configurable page size (default 20, maximum 100)
11. WHEN an ADMIN associates a CLIENT user to a customer, THE Customer_Module SHALL persist the association so the CLIENT can only access data related to their assigned customer

---

### Requirement 7: Customer Address with Geospatial Data

**User Story:** As an administrator, I want to store customer addresses with geographic coordinates, so that I can perform radius-based queries.

#### Acceptance Criteria

1. WHEN a customer address includes latitude and longitude, THE Customer_Module SHALL store the coordinates as a PostGIS geometry point (SRID 4326)
2. WHEN a radius query is submitted with center coordinates and distance in kilometers, THE Customer_Module SHALL return all customers within the specified radius ordered by distance ascending
3. THE Customer_Module SHALL validate latitude values between -90 and 90 and longitude values between -180 and 180
4. IF invalid coordinates are provided (outside valid ranges or non-numeric), THEN THE Customer_Module SHALL return a 400 Bad Request with a descriptive validation error indicating the violated constraint
5. THE Customer_Module SHALL support optional address fields: street, city, state, postal code, and country alongside the coordinates

---

### Requirement 8: Service Request Lifecycle

**User Story:** As a client, I want to create service requests and track their progress, so that I can manage my interaction with the organization.

#### Acceptance Criteria

1. WHEN a CLIENT creates a request with title (1 to 200 characters), description (1 to 5000 characters), and optional priority (LOW, MEDIUM, HIGH, or CRITICAL), THE Request_Module SHALL persist the request with status OPEN, the client's customer association, creation timestamp, and priority defaulting to MEDIUM if not provided
2. IF a CLIENT submits a request with a blank title, a blank description, or a title exceeding 200 characters or description exceeding 5000 characters, THEN THE Request_Module SHALL reject the request with a validation error indicating the violated constraint
3. THE Request_Module SHALL enforce the following status transitions: OPEN → IN_PROGRESS, OPEN → CANCELLED, IN_PROGRESS → WAITING_CUSTOMER, IN_PROGRESS → COMPLETED, IN_PROGRESS → CANCELLED, WAITING_CUSTOMER → IN_PROGRESS, WAITING_CUSTOMER → CANCELLED
4. IF a status transition not in the allowed set is attempted, THEN THE Request_Module SHALL return a 422 Unprocessable Entity error with the current status and the list of valid target statuses
5. WHEN an ANALYST is assigned to a request in OPEN status, THE Request_Module SHALL record the analyst's user identifier, the assignment timestamp, and transition the status to IN_PROGRESS
6. WHEN a user with CLIENT or ANALYST role adds a comment (1 to 2000 characters) to a request, THE Request_Module SHALL persist the comment with the author's user identifier, creation timestamp, and request association
7. IF a comment is submitted with blank text or text exceeding 2000 characters, THEN THE Request_Module SHALL reject the comment with a validation error indicating the violated constraint
8. THE Request_Module SHALL support associating one or more documents with a request, recording the association timestamp and the user who linked the document
9. WHEN a CLIENT queries their requests, THE Request_Module SHALL return only requests belonging to the client's associated customer, supporting pagination (default page size 20, maximum 100) and optional filtering by status and priority

---

### Requirement 9: Document Metadata Registration

**User Story:** As a client, I want to register document metadata before uploading, so that the system can prepare for the file transfer.

#### Acceptance Criteria

1. WHEN a CLIENT registers document metadata (filename 1-255 characters, content type, size in bytes), THE Document_Module SHALL create a document record with status PENDING_UPLOAD, a generated identifier, and return the document identifier
2. THE Document_Module SHALL validate that the content type is in the allowed list (application/pdf, application/vnd.openxmlformats-officedocument.wordprocessingml.document, image/png, image/jpeg)
3. IF an unsupported content type is provided, THEN THE Document_Module SHALL return a 400 Bad Request error listing the supported content types
4. THE Document_Module SHALL associate the document with the authenticated user's tenant and optionally with a request
5. THE Document_Module SHALL validate the declared file size does not exceed 2 GB, returning a 400 Bad Request error if exceeded
6. THE Document_Module SHALL require a SHA-256 checksum to be declared at registration time for later verification

---

### Requirement 10: Document Upload to Object Storage

**User Story:** As a client, I want to upload documents via multipart upload, so that I can transfer large files reliably.

#### Acceptance Criteria

1. WHEN a client initiates an upload for a registered document with status PENDING_UPLOAD, THE Document_Module SHALL generate a presigned upload URL for MinIO with an expiration time of 60 minutes and return it to the client
2. WHEN the client confirms upload completion by calling the upload confirmation endpoint, THE Document_Module SHALL validate the file checksum (SHA-256) against the value declared during document registration
3. IF the checksum validation fails, THEN THE Document_Module SHALL mark the document as UPLOAD_FAILED, delete the uploaded object from MinIO, and return a 422 error with code CHECKSUM_MISMATCH
4. WHEN the checksum validation succeeds, THE Document_Module SHALL transition the document status to UPLOADED and publish a DocumentUploadedEvent to Redis Streams
5. THE Document_Module SHALL store documents in MinIO using the path pattern: `{tenantId}/{year}/{month}/{documentId}/{filename}` where filename is limited to 255 characters and restricted to alphanumeric characters, hyphens, underscores, and dots
6. THE Document_Module SHALL reject uploads exceeding 2 GB in file size by returning a 422 error with code FILE_SIZE_EXCEEDED
7. IF the presigned URL has expired when the client attempts to confirm the upload, THEN THE Document_Module SHALL return a 422 error with code UPLOAD_URL_EXPIRED and allow the client to request a new presigned URL

---

### Requirement 11: Asynchronous Document Processing

**User Story:** As a system operator, I want documents to be processed asynchronously after upload, so that heavy operations do not block the API.

#### Acceptance Criteria

1. WHEN a DocumentUploadedEvent is published, THE Worker_Process SHALL consume the event and begin document processing within 5 seconds of event availability in the stream
2. WHEN a DocumentUploadedEvent is consumed for a PDF or DOCX document, THE Worker_Process SHALL extract text content using Apache Tika and store the extracted text (up to 10 MB of text per document) in the document record
3. WHEN text extraction completes successfully, THE Worker_Process SHALL transition the document status to TEXT_EXTRACTED and publish a DocumentReadyForAnalysisEvent containing the document identifier and tenant identifier
4. IF text extraction fails, THEN THE Worker_Process SHALL retry the extraction up to 3 times with exponential backoff (delays of 1 second, 5 seconds, 30 seconds) and, if all retries are exhausted, transition the document status to PROCESSING_FAILED and publish the event to the DLQ
5. IF the document type does not support text extraction (image/png, image/jpeg), THEN THE Worker_Process SHALL skip the text extraction step, transition the document status to TEXT_EXTRACTED with empty extracted text, and publish the DocumentReadyForAnalysisEvent
6. WHEN a DocumentUploadedEvent is consumed for a PDF document, THE Worker_Process SHALL generate a PNG preview image of the first page with dimensions not exceeding 600x800 pixels and store it in MinIO at the path `{tenantId}/{year}/{month}/{documentId}/preview.png`
7. IF the Worker_Process receives a DocumentUploadedEvent for a document identifier that has already been processed (status beyond UPLOADED), THEN THE Worker_Process SHALL skip processing and acknowledge the event without side effects
8. IF preview generation fails, THEN THE Worker_Process SHALL log the failure and continue document processing without blocking the text extraction or AI analysis pipeline

---

### Requirement 12: AI Document Analysis with Fallback

**User Story:** As an analyst, I want documents to be automatically analyzed by AI with a reliable fallback, so that I always receive analysis results.

#### Acceptance Criteria

1. WHEN a DocumentReadyForAnalysisEvent is consumed, THE Worker_Process SHALL invoke the AI analysis via the DocumentAnalysisPort
2. WHEN the AI provider (Ollama) is available, THE Worker_Process SHALL analyze the document extracting: summary (max 500 characters), category, key fields (list of key-value pairs), risk indicators (list of strings), and confidence score (0.0 to 1.0)
3. WHEN the AI provider is unavailable or returns an error within 30 seconds timeout, THE Worker_Process SHALL execute a deterministic fallback that provides: basic category based on content type, word count summary, empty risk indicators, and a confidence score of 0.0 with fallback flag set to true
4. WHEN analysis completes (AI or fallback), THE Worker_Process SHALL store the analysis result in the document record and transition status to ANALYZED
5. WHEN analysis completes, THE Worker_Process SHALL publish a DocumentAnalyzedEvent containing the document identifier, tenant identifier, and whether the fallback was used

---

### Requirement 13: Document Approval Workflow

**User Story:** As an analyst, I want to approve or reject analyzed documents, so that I can validate AI analysis results before they reach clients.

#### Acceptance Criteria

1. WHEN a document reaches ANALYZED status, THE Approval_Module SHALL create a pending approval record associated with the document and set its status to PENDING
2. WHEN an ANALYST approves a document that has a PENDING approval, THE Approval_Module SHALL transition the approval status to APPROVED, record the decision timestamp and the analyst identifier
3. WHEN an ANALYST rejects a document that has a PENDING approval, THE Approval_Module SHALL transition the approval status to REJECTED and store the rejection reason, which must be between 10 and 1000 characters
4. THE Approval_Module SHALL enforce that only users with ANALYST or ADMIN role can approve or reject documents
5. IF a non-authorized user attempts an approval action, THEN THE Approval_Module SHALL deny the action and return a 403 Forbidden error
6. WHEN an approval decision is made, THE Approval_Module SHALL publish an ApprovalDecisionEvent with document identifier, decision, analyst identifier, and decision timestamp
7. IF an approval or rejection is attempted on an approval record that is not in PENDING status, THEN THE Approval_Module SHALL reject the operation and return a 422 error indicating that the decision is immutable
8. WHEN an ADMIN cancels a pending approval, THE Approval_Module SHALL transition the approval status to CANCELLED, record the cancellation timestamp and the admin identifier

---

### Requirement 14: Activity Feed

**User Story:** As a user, I want to see a chronological feed of actions on entities I have access to, so that I can track what happened and when.

#### Acceptance Criteria

1. WHEN a domain event is published (customer created, request updated, document uploaded, approval decided), THE Activity_Module SHALL record an activity entry with: entity type, entity identifier, action type, actor identifier, timestamp, and a summary of at most 500 characters
2. THE Activity_Module SHALL support querying activities by entity (show all activities for a specific customer or request), ordered by timestamp descending
3. THE Activity_Module SHALL support querying a global activity feed for the tenant, ordered by timestamp descending
4. THE Activity_Module SHALL support paginated retrieval with configurable page size (minimum 1, default 20, maximum 100)
5. THE Activity_Module SHALL filter activities by the authenticated user's tenant, ensuring cross-tenant isolation
6. IF the authenticated user has the CLIENT role, THEN THE Activity_Module SHALL restrict query results to only activities whose entity belongs to that user's associated customer
7. IF a domain event is received more than once (duplicate delivery), THEN THE Activity_Module SHALL not create a duplicate activity entry
8. IF a domain event cannot be processed (malformed payload or persistence failure), THEN THE Activity_Module SHALL retain the event for retry without blocking subsequent event processing

---

### Requirement 15: In-App Notifications

**User Story:** As a client, I want to receive in-app notifications about events relevant to me, so that I can stay informed about my requests and documents.

#### Acceptance Criteria

1. WHEN an ApprovalDecisionEvent is published for a document associated with a CLIENT's request, THE Notification_Module SHALL create an in-app notification for the CLIENT user containing the approval decision (APPROVED or REJECTED) and a link to the document
2. WHEN a request status changes, THE Notification_Module SHALL create an in-app notification for the request owner (CLIENT) containing the previous status, new status, and a link to the request
3. THE Notification_Module SHALL store notifications with: recipient user identifier, tenant identifier, title (maximum 150 characters), message (maximum 500 characters), read/unread status (default unread), creation timestamp, and link to the related entity
4. WHEN a user marks a single notification as read, THE Notification_Module SHALL update the read status of that notification
5. WHEN a user marks multiple notifications as read in a single bulk request (maximum 100 notification identifiers per request), THE Notification_Module SHALL update the read status of all specified notifications atomically
6. IF a user attempts to mark as read a notification that does not exist or belongs to another user, THEN THE Notification_Module SHALL return a 404 Not Found error without revealing whether the notification exists for another user
7. THE Notification_Module SHALL support querying unread notification count for the authenticated user, scoped to the user's tenant
8. THE Notification_Module SHALL support paginated retrieval of notifications for the authenticated user with configurable page size (default 20, maximum 100), ordered by creation timestamp descending
9. THE Notification_Module SHALL filter all notification queries by the authenticated user's tenant and user identifier, ensuring cross-tenant and cross-user isolation

---

### Requirement 16: Email Notifications

**User Story:** As a client, I want to receive email notifications for important events, so that I am informed even when not using the application.

#### Acceptance Criteria

1. WHEN an approval decision is made on a document, THE Notification_Module SHALL send an email notification to the document owner (CLIENT user associated with the request)
2. THE Notification_Module SHALL deliver emails via SMTP to MailHog in development environment (host and port configurable via environment variables)
3. THE Notification_Module SHALL include in the email body: the event description, a link to the relevant entity, and the tenant name
4. IF email delivery fails, THEN THE Notification_Module SHALL retry delivery up to 3 times with exponential backoff (1s, 4s, 16s) before publishing the failed delivery to the DLQ
5. THE Notification_Module SHALL not block the notification creation flow while sending email (asynchronous delivery)

---

### Requirement 17: Real-Time Delivery via SSE

**User Story:** As a user, I want to receive real-time updates while using the application, so that I see changes without refreshing the page.

#### Acceptance Criteria

1. WHEN a client establishes an SSE connection with a valid JWT token (passed as query parameter), THE Notification_Module SHALL authenticate the connection and begin streaming events
2. IF the SSE connection attempt includes an invalid or expired JWT, THEN THE Notification_Module SHALL reject the connection with a 401 Unauthorized error
3. WHILE an SSE connection is active, THE Notification_Module SHALL push new notifications to the connected user within 5 seconds of creation
4. WHILE an SSE connection is active, THE Notification_Module SHALL push document processing progress events to the document owner within 3 seconds of status change
5. IF the SSE connection drops, THEN THE Notification_Module SHALL allow the client to reconnect and receive missed events using a Last-Event-ID header, with missed events retained for up to 60 minutes
6. THE Notification_Module SHALL scope SSE events to the authenticated user's tenant and user identifier
7. THE Notification_Module SHALL send a heartbeat comment (`:keepalive\n\n`) every 30 seconds to keep the connection alive through proxies and load balancers

---

### Requirement 18: Unified Search

**User Story:** As a user, I want to search across customers, requests, and documents from a single search bar, so that I can quickly find information.

#### Acceptance Criteria

1. WHEN a keyword search query of at least 2 characters and at most 200 characters is submitted, THE Search_Module SHALL return matching results across customers (by name, email), requests (by title, description), and documents (by filename, extracted text) within 2 seconds
2. THE Search_Module SHALL implement keyword search using PostgreSQL full-text search (tsvector/tsquery) as the P0 implementation
3. THE Search_Module SHALL return results grouped by entity type, where each result includes entity type, entity ID, title or name, a text snippet of up to 200 characters, and a relevance rank as a normalized score between 0.0 and 1.0
4. THE Search_Module SHALL filter all search results by the authenticated user's tenant
5. WHEN a CLIENT user performs a search, THE Search_Module SHALL further filter results to only entities associated with the client's customer
6. THE Search_Module SHALL support paginated results with configurable page size (default 20, maximum 50)
7. IF the search query is empty or contains fewer than 2 characters, THEN THE Search_Module SHALL reject the request with a validation error indicating the minimum query length requirement
8. IF no results match the search query, THEN THE Search_Module SHALL return an empty result set with zero total count and the standard pagination metadata

---

### Requirement 19: Immutable Audit Ledger

**User Story:** As a compliance officer, I want all critical actions recorded in an immutable ledger, so that I can audit system behavior and detect unauthorized changes.

#### Acceptance Criteria

1. WHEN a critical action occurs (user login, customer creation, document upload, approval decision, role change, tenant deactivation), THE Audit_Module SHALL write an append-only entry with: action type, actor identifier, tenant identifier, entity type, entity identifier, timestamp, correlation identifier, and action details as JSON (maximum 10 KB)
2. THE Audit_Module SHALL implement the ledger as a PostgreSQL append-only table with no UPDATE or DELETE operations permitted
3. THE Audit_Module SHALL enforce immutability through a database trigger that raises an exception on any UPDATE or DELETE attempt
4. THE Audit_Module SHALL support querying audit entries by time range, actor, entity, and action type, filtered by the authenticated user's tenant
5. THE Audit_Module SHALL support paginated retrieval (default page size 50, maximum 200) ordered by timestamp descending
6. THE Audit_Module SHALL include the correlation identifier from the request context in every audit entry for distributed tracing
7. IF the audit write fails (database unavailable or constraint violation), THEN THE Audit_Module SHALL log the failure at ERROR level with full context and retry once after 1 second before allowing the original operation to proceed without blocking
8. IF a request does not carry a correlation identifier in the MDC context, THEN THE Audit_Module SHALL generate a new UUID v4 as the correlation identifier for that audit entry

---

### Requirement 20: Worker Process Reliability

**User Story:** As a system operator, I want the worker process to handle failures gracefully with retries and dead letter queues, so that no work is permanently lost.

#### Acceptance Criteria

1. THE Worker_Process SHALL consume events from Redis Streams using consumer groups (XREADGROUP) for at-least-once delivery with a consumer group name configurable via application properties
2. WHEN a processing task fails, THE Worker_Process SHALL retry the task up to 3 times with exponential backoff (1s, 4s, 16s)
3. IF a task fails after all retry attempts, THEN THE Worker_Process SHALL move the message to a Dead Letter Queue stream (named `{originalStream}:dlq`) preserving the original message content, attempt count, last error message, and timestamp
4. THE Worker_Process SHALL acknowledge messages (XACK) only after successful processing
5. THE Worker_Process SHALL log each processing attempt with: task identifier, attempt number, duration in milliseconds, and outcome (success/failure/retry)
6. WHEN the Worker_Process starts, THE Worker_Process SHALL claim and resume processing any pending (unacknowledged) messages that have been idle for more than 60 seconds from previous runs
7. THE Worker_Process SHALL process messages in batches of up to 10 messages per XREADGROUP call with a block timeout of 2 seconds
8. IF the Redis connection is lost, THEN THE Worker_Process SHALL retry connection with exponential backoff (1s, 2s, 4s, max 30s) and resume processing from the last acknowledged position once reconnected

---

### Requirement 21: Frontend Authentication and Layout

**User Story:** As a user, I want to log in through the web application and see an interface appropriate to my role, so that I can perform my tasks efficiently.

#### Acceptance Criteria

1. WHEN a user accesses a protected page without authentication, THE Frontend_App SHALL redirect to the login page
2. WHEN a user submits valid credentials on the login page, THE Frontend_App SHALL store the tokens securely (access token in memory, refresh token in httpOnly cookie or secure storage) and redirect to the appropriate home page based on role (ADMIN → /admin/dashboard, CLIENT → /portal/home, ANALYST → /admin/dashboard)
3. THE Frontend_App SHALL include the access token in the Authorization header of every API request
4. WHEN the access token expires, THE Frontend_App SHALL automatically attempt a refresh using the refresh token before retrying the failed request
5. THE Frontend_App SHALL display a sidebar navigation appropriate to the user's role (full admin menu for ADMIN/ANALYST, limited portal menu for CLIENT)
6. THE Frontend_App SHALL implement responsive layout: persistent sidebar on desktop (≥1024px), collapsible overlay on mobile (<1024px)

---

### Requirement 22: Frontend Admin Pages

**User Story:** As an administrator, I want functional admin pages for customers, requests, documents, and approvals, so that I can manage the platform.

#### Acceptance Criteria

1. THE Frontend_App SHALL render the admin dashboard page (/admin/dashboard) with summary cards showing: active customers count, requests by status, documents processed count, and pending approvals count
2. THE Frontend_App SHALL render the customers list page (/admin/customers) with a paginated data table supporting search and status filter
3. WHEN an ADMIN clicks "New Customer", THE Frontend_App SHALL display a form for creating a customer with validation (required fields: name, email) using shadcn/ui form components
4. THE Frontend_App SHALL render the requests list page (/admin/requests) with paginated data table, status filter, priority filter, and analyst assignment
5. THE Frontend_App SHALL render the documents list page (/admin/documents) with status indicators (color-coded badges) and links to approval actions
6. THE Frontend_App SHALL render the approvals page (/admin/approvals) listing pending approvals with approve/reject action buttons and rejection reason dialog
7. THE Frontend_App SHALL render the audit page (/admin/audit) with a filterable log table (by action type, date range, actor)
8. THE Frontend_App SHALL render the activities page (/admin/activities) showing the global activity feed with infinite scroll pagination

---

### Requirement 23: Frontend Portal Pages

**User Story:** As a client, I want portal pages to manage my requests and documents, so that I can interact with the organization digitally.

#### Acceptance Criteria

1. THE Frontend_App SHALL render the portal home page (/portal/home) with a summary of open requests count, recent documents (last 5), and unread notifications count
2. THE Frontend_App SHALL render the requests page (/portal/requests) listing only the client's requests with status badge, priority, and creation date
3. WHEN a CLIENT clicks "New Request", THE Frontend_App SHALL display a form for creating a request with title (required), description (required), and priority (optional dropdown)
4. THE Frontend_App SHALL render the request detail page (/portal/requests/:id) showing request information, associated documents, comments thread, and status timeline
5. THE Frontend_App SHALL render the documents page (/portal/documents) listing documents associated with the client's requests with status and upload date
6. THE Frontend_App SHALL render the upload page (/portal/documents/upload) with a file upload component supporting drag-and-drop, file type validation, progress indication, and cancel/retry
7. THE Frontend_App SHALL render the notifications page (/portal/notifications) listing all notifications with read/unread indicators and mark-as-read functionality

---

### Requirement 24: Frontend Search and Command Palette

**User Story:** As a user, I want a global search and command palette accessible via keyboard shortcut, so that I can navigate and find information quickly.

#### Acceptance Criteria

1. WHEN a user presses Ctrl+K (or Cmd+K on macOS), THE Frontend_App SHALL open a command palette overlay
2. THE Frontend_App SHALL display search results grouped by entity type (customers, requests, documents) as the user types
3. WHEN a user selects a search result, THE Frontend_App SHALL navigate to the corresponding entity detail page
4. THE Frontend_App SHALL debounce search input by 300ms to avoid excessive API calls
5. THE Frontend_App SHALL display the command palette with keyboard navigation support (arrow keys to navigate, Enter to select, Escape to close)
6. THE Frontend_App SHALL display a loading skeleton while search results are being fetched
7. THE Frontend_App SHALL display "No results found" when the search returns empty

---

### Requirement 25: Frontend Real-Time Updates

**User Story:** As a user, I want to see real-time updates on the interface without refreshing, so that I have current information.

#### Acceptance Criteria

1. WHEN the Frontend_App loads after authentication, THE Frontend_App SHALL establish an SSE connection to the notification endpoint
2. WHILE an SSE connection is active and a new notification arrives, THE Frontend_App SHALL display a toast notification and increment the unread count in the navigation badge
3. WHILE an SSE connection is active and a document processing progress event arrives, THE Frontend_App SHALL update the progress indicator on the document detail page
4. IF the SSE connection is lost, THEN THE Frontend_App SHALL attempt reconnection with exponential backoff (1s, 2s, 4s, max 30s) and include the Last-Event-ID for resumption
5. WHEN the user logs out, THE Frontend_App SHALL close the SSE connection

---

### Requirement 26: Database Schema and Migrations

**User Story:** As a developer, I want a well-structured database schema managed by Flyway migrations, so that the data layer is consistent and evolvable.

#### Acceptance Criteria

1. THE System SHALL create Flyway migration scripts for the following tables: tenants, users, customers, requests, documents, approvals, activities, notifications, audit_entries, and search_index, with each table using a UUID primary key column of PostgreSQL type `uuid` and including `created_at TIMESTAMPTZ NOT NULL DEFAULT now()` and `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()` audit columns
2. THE System SHALL include a `tenant_id UUID NOT NULL` column with a foreign key reference to `tenants(id)` on every table except the `tenants` table itself, and SHALL create a B-tree index on the `tenant_id` column of each tenant-scoped table
3. THE System SHALL include indexes for: a B-tree index on each `status` column where one exists, a GIN index on `tsvector` columns in the `search_index` table, and a GIST index on `geometry` columns where geospatial data is stored
4. THE System SHALL enforce referential integrity via foreign keys for: `users.tenant_id → tenants.id`, `customers.tenant_id → tenants.id`, `requests.customer_id → customers.id`, `documents.request_id → requests.id`, `approvals.document_id → documents.id`, `activities.tenant_id → tenants.id`, `notifications.tenant_id → tenants.id`, and `audit_entries.tenant_id → tenants.id`
5. THE System SHALL include a database trigger on the `audit_entries` table that raises an exception and prevents any UPDATE or DELETE operation on existing rows
6. WHEN `make migrate` is executed consecutively at least 2 times against an already-migrated database, THE System SHALL complete without errors and without applying duplicate changes
7. THE System SHALL enable the `pgvector`, `postgis`, and `uuid-ossp` PostgreSQL extensions in the first migration script before any table creation statements execute

---

### Requirement 27: API Correlation and Observability

**User Story:** As a developer, I want every request to carry a correlation identifier through all layers, so that I can trace operations across services.

#### Acceptance Criteria

1. WHEN a request includes an X-Correlation-ID header with a non-blank value, THE System SHALL trim the value and propagate it as the correlation identifier in the response header, all published domain events, and all log entries for that request
2. WHEN a request does not include an X-Correlation-ID header or the header value is blank, THE System SHALL generate a UUID v4 and use it as the correlation identifier
3. THE System SHALL include the X-Correlation-ID in all response headers
4. THE System SHALL include the correlation identifier in all published domain events
5. THE System SHALL include the correlation identifier in all log entries via the MDC key "correlationId"
6. WHEN a request completes or fails with an exception, THE System SHALL remove the correlation identifier from the MDC context to prevent leaking between subsequent requests on the same thread
