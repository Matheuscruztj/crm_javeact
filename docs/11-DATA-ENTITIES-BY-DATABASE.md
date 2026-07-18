# AtlasOps AI — Data Entities by Database Type

## 1. Data classification

Every dataset is classified as:

```text
SOURCE_OF_TRUTH
EVENT_SOURCE
PROJECTION
INDEX
ARCHIVE
CACHE
TEMPORARY_PROCESSING
TELEMETRY
OBJECT_STORAGE
IMMUTABLE_RECORD
```

PostgreSQL is the default source of truth. EventStoreDB is an event source only for Approval when enabled.

---

## 2. PostgreSQL — Transactional source of truth

All business tables include tenant key, timestamps, indexes, constraints and version where concurrency applies.

### Tenant

```text
id
name
status
logo_url
primary_color
maintenance_mode
created_at
updated_at
version
```

### User

```text
id
tenant_id
customer_id nullable
email
password_hash
role
status
created_at
updated_at
version
```

Roles:

```text
ADMIN
ANALYST
CLIENT
```

Constraint:

```text
unique (tenant_id, email)
```

### Session

```text
id
tenant_id
user_id
refresh_token_hash
family_id
expires_at
rotated_at nullable
revoked_at nullable
created_at
last_used_at
ip_hash nullable
user_agent nullable
```

### Customer

```text
id
tenant_id
name
status
email nullable
phone nullable
address_line nullable
city nullable
state nullable
postal_code nullable
country_code nullable
latitude nullable
longitude nullable
location geography(Point, 4326) nullable
created_at
updated_at
version
```

Indexes:

```text
(tenant_id, status)
(tenant_id, created_at)
GIST(location)
```

### Request

```text
id
tenant_id
customer_id
title
description
priority
status
assigned_to nullable
sla_due_at nullable
created_by
created_at
updated_at
version
```

Indexes:

```text
(tenant_id, status, created_at)
(tenant_id, customer_id, created_at)
(tenant_id, assigned_to, status)
```

### RequestStatusHistory

```text
id
tenant_id
request_id
from_status nullable
to_status
changed_by
reason nullable
occurred_at
```

### RequestComment

```text
id
tenant_id
request_id
author_id
body
created_at
updated_at nullable
```

### Document

```text
id
tenant_id
customer_id
request_id nullable
original_name
storage_key nullable
preview_key nullable
mime_type
declared_size
actual_size nullable
checksum nullable
status
legal_hold
analysis_status
created_by
created_at
updated_at
version
```

### UploadSession

```text
id
tenant_id
document_id
storage_upload_id
part_size
total_size
status
expires_at
created_at
last_activity_at
completed_at nullable
version
```

### UploadPart

```text
id
tenant_id
upload_session_id
part_number
etag nullable
size nullable
status
updated_at
```

### DocumentAnalysis

```text
id
tenant_id
document_id
status
prompt_version
model_provider
model_name
model_version nullable
input_hash
result_json
confidence nullable
fallback
duration_ms
error_code nullable
created_at
```

### ApprovalReadModel

```text
id
tenant_id
resource_type
resource_id
status
requested_by
assigned_to nullable
decided_by nullable
decision_reason nullable
stream_version nullable
created_at
decided_at nullable
updated_at
version
```

### Activity

```text
id
tenant_id
actor_id nullable
type
resource_type
resource_id
summary
metadata_json
correlation_id
occurred_at
```

### Notification

```text
id
tenant_id
user_id
type
title
body
resource_type nullable
resource_id nullable
read_at nullable
created_at
```

### NotificationPreference

```text
id
tenant_id
user_id
email_enabled
updated_at
```

### Integration

```text
id
tenant_id
type
name
endpoint
enabled
secret_reference nullable
configuration_json
last_execution_at nullable
created_at
updated_at
version
```

Types:

```text
REST_API
MCP_SERVER
```

### IntegrationExecution

```text
id
tenant_id
integration_id
operation
status
duration_ms nullable
archive_reference nullable
error_code nullable
correlation_id
created_by
started_at
finished_at nullable
```

### ImportJob

```text
id
tenant_id
type
storage_key
status
total_rows nullable
valid_rows nullable
invalid_rows nullable
error_report_key nullable
idempotency_key
created_by
created_at
started_at nullable
finished_at nullable
```

### Job

```text
id
tenant_id
type
status
attempts
max_attempts
available_at
idempotency_key
payload_json or payload_reference
correlation_id
error_code nullable
error_detail nullable
created_at
started_at nullable
finished_at nullable
version
```

### IdempotencyRecord

```text
id
tenant_id
actor_id nullable
operation
idempotency_key
request_hash
response_status nullable
response_body nullable
state
expires_at
created_at
updated_at
```

### OutboxEvent

```text
id
tenant_id
event_type
event_version
aggregate_type
aggregate_id
correlation_id
payload_json
occurred_at
published_at nullable
attempts
last_error nullable
```

### ProjectionStatus

```text
id
tenant_id
projection_name
status
last_event_id nullable
last_event_at nullable
lag_seconds nullable
error_code nullable
rebuild_started_at nullable
rebuild_finished_at nullable
updated_at
```

### AuditEvent

```text
id
tenant_id
actor_id nullable
action
resource_type
resource_id
ip_hash nullable
user_agent nullable
correlation_id
metadata_json
occurred_at
```

---

## 3. PostgreSQL — Immutable ledger

### LedgerEntry

```text
id
tenant_id
sequence
event_type
actor_id nullable
resource_type
resource_id
canonical_payload
payload_hash
previous_hash
current_hash
occurred_at
checkpoint_id nullable
```

Constraints:

```text
unique (tenant_id, sequence)
append-only runtime permissions
```

Hash:

```text
current_hash =
SHA-256(
  previous_hash
  + sequence
  + event_type
  + canonical_payload
  + occurred_at
)
```

### LedgerCheckpoint

```text
id
tenant_id
first_sequence
last_sequence
root_hash
signature nullable
created_at
```

---

## 4. Redis or Valkey — Key-value and coordination

Key naming:

```text
atlasops:{environment}:{tenant}:{purpose}:{identifier}
```

Examples:

```text
session:{sessionId}
idempotency:{actorId}:{operation}:{key}
lock:{resourceType}:{resourceId}
rate-limit:{subject}:{route}:{window}
upload-progress:{uploadSessionId}
sse-connection:{userId}:{connectionId}
feature:{featureName}
```

Rules:

- explicit TTL where appropriate;
- tenant in business-data namespaces;
- no essential state only in Redis;
- unique lock owner token.

---

## 5. Redis Streams — Event backbone

Main stream:

```text
atlasops-events
```

Fields:

```text
event_id
event_type
event_version
tenant_id
aggregate_type
aggregate_id
correlation_id
occurred_at
payload
```

Consumer groups:

```text
document-processing
notifications
search-index
vector-index
graph-projection
telemetry
analytics
integration-archive
```

DLQ:

```text
atlasops-events-dlq
```

or a consistently documented PostgreSQL job/DLQ strategy.

---

## 6. MinIO or S3 — Object storage

Prefixes:

```text
tenants/{tenantId}/documents/{documentId}/original/
tenants/{tenantId}/documents/{documentId}/preview/
tenants/{tenantId}/imports/{importId}/source/
tenants/{tenantId}/imports/{importId}/reports/
tenants/{tenantId}/exports/{exportId}/
tenants/{tenantId}/backups/{backupId}/
tests/{runId}/{workerId}/
```

Original filename is metadata, never the object key.

---

## 7. pgvector — Semantic index

### KnowledgeChunk

```text
id
tenant_id
document_id
page_number nullable
chunk_index
text
embedding vector(<dimension>)
embedding_provider
embedding_model
embedding_version
source_hash
metadata_json
created_at
```

Required metadata includes source document, page/location, permission category and optional customer/request reference.

---

## 8. PostGIS — Geospatial index

Stored on Customer:

```text
location geography(Point, 4326)
```

Access patterns:

- within radius;
- nearest customers;
- optional region containment.

Index:

```text
GIST(location)
```

---

## 9. MongoDB — Integration payload archive

### IntegrationExecutionPayload

```json
{
  "_id": "...",
  "executionId": "...",
  "tenantId": "...",
  "integrationId": "...",
  "integrationType": "REST_API",
  "operation": "...",
  "request": {},
  "response": {},
  "redactedHeaders": {},
  "steps": [],
  "schemaVersion": 1,
  "createdAt": "...",
  "expiresAt": "..."
}
```

Indexes:

- execution ID;
- tenant and created time;
- TTL on expiry.

Secrets are redacted before persistence. PostgreSQL stores status and archive reference.

---

## 10. Neo4j — Relationship projection

Nodes:

```text
Customer
Request
Document
Integration
```

Properties:

```text
id
tenantId
status nullable
updatedAt
```

Relationships:

```text
(Customer)-[:OPENED]->(Request)
(Customer)-[:OWNS]->(Document)
(Request)-[:ATTACHED]->(Document)
(Document)-[:PROCESSED_BY]->(Integration)
```

Every query constrains tenant before traversal. The graph is rebuildable and never authorizes access.

---

## 11. TimescaleDB — Time-series

### OperationalMetric

```text
time
tenant_id
metric_name
resource_type nullable
resource_id nullable
value
unit
dimensions_json
event_id nullable
```

Initial metrics:

```text
document_processing_duration
request_sla_consumption
integration_latency
ai_analysis_duration
```

Policies include retention, optional compression, aggregate buckets and duplicate-event handling.

---

## 12. OpenSearch — Text index

Aliases:

```text
customers-current
requests-current
documents-current
```

Physical indexes:

```text
customers-v1
requests-v1
documents-v1
```

Fields:

```text
id
tenantId
resourceType
title
content
status
customerId nullable
requestId nullable
createdAt
updatedAt
permissions
```

Results are hydrated and finally authorized from PostgreSQL.

---

## 13. ClickHouse — Historical analytics

### ProductEvent

```text
event_id
event_type
tenant_id
resource_id
occurred_at
duration_ms nullable
status nullable
fallback nullable
dimensions_json
```

Initial events:

```text
customer.created
request.created
document.processed
ai.analysis.completed
```

Queries:

- counts by tenant and period;
- average document duration;
- AI fallback ratio.

---

## 14. DuckDB — Import staging

Temporary tables:

```text
raw_customer_import
normalized_customer_import
validated_customer_import
customer_import_errors
```

Requirements:

- memory limit;
- isolated temporary directory;
- cleanup;
- malformed-file handling;
- no long-lived source of truth.

---

## 15. EventStoreDB — Approval event source

Stream:

```text
approval-{approvalId}
```

Metadata:

```text
tenantId
resourceType
resourceId
correlationId
actorId
```

Events:

```text
ApprovalRequested
ApprovalAssigned
ApprovalApproved
ApprovalRejected
ApprovalCancelled
```

PostgreSQL contains the Approval read model.

---

## 16. Ownership matrix

| Data | Owner | Store |
|---|---|---|
| Tenant, user, session | Auth/Tenants | PostgreSQL |
| Customer | Customers | PostgreSQL and PostGIS |
| Request | Requests | PostgreSQL |
| Document metadata | Documents | PostgreSQL |
| File bytes | Documents | MinIO/S3 |
| Analysis | Documents/AI | PostgreSQL |
| Approval events | Approvals | EventStoreDB when enabled |
| Approval read state | Approvals | PostgreSQL |
| Activities/notifications | Activities | PostgreSQL |
| Integration metadata | Integrations | PostgreSQL |
| Raw integration payload | Integrations | MongoDB |
| Semantic chunks | Search | pgvector |
| Text search | Search | OpenSearch |
| Relationships | Projection | Neo4j |
| Time-series | Analytics | TimescaleDB |
| Historical analytics | Analytics | ClickHouse |
| CSV processing | Imports | DuckDB |
| Critical evidence | Audit | PostgreSQL ledger |
| Event distribution | Async | Redis Streams |

---

## 17. Cross-store deletion and retention

```text
check legal hold
→ update transactional state
→ delete or archive object
→ remove OpenSearch document
→ remove pgvector chunks
→ remove Neo4j relationships
→ apply MongoDB retention
→ emit analytical correction
→ append ledger evidence
```

Projection outage creates a retry, not silent inconsistency.

---

## 18. Rebuild and reconciliation

Commands:

```bash
make rebuild-search-index
make rebuild-vector-index
make rebuild-graph
make rebuild-analytics
make replay-projections
make verify-ledger
```

Each rebuild supports all tenants, one tenant and resource scope where practical, with progress, safe cancellation, validation and final status.
