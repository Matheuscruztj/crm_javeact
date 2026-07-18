# AtlasOps AI — Architecture Diagrams: C4 and Mermaid

## 1. C4 System Context

```mermaid
C4Context
title AtlasOps AI — System Context

Person(admin, "Administrator", "Manages tenant operations, users and integrations")
Person(analyst, "Analyst", "Processes requests and reviews documents")
Person(client, "Client User", "Creates requests and uploads documents")

System(atlasops, "AtlasOps AI", "Multi-tenant operational platform")

System_Ext(rest, "External REST API", "Public data provider")
System_Ext(mcp, "External MCP Server", "Read-only tools")
System_Ext(email, "SMTP Service", "Email delivery")
System_Ext(aiProvider, "AI Provider or Local Model", "Document analysis")

Rel(admin, atlasops, "Uses", "HTTPS")
Rel(analyst, atlasops, "Uses", "HTTPS")
Rel(client, atlasops, "Uses", "HTTPS")
Rel(atlasops, rest, "Reads data", "HTTPS/JSON")
Rel(atlasops, mcp, "Invokes allowed tools", "MCP")
Rel(atlasops, email, "Sends notifications", "SMTP")
Rel(atlasops, aiProvider, "Requests analysis")
```

## 2. C4 Container Diagram

```mermaid
C4Container
title AtlasOps AI — Containers

Person(user, "User")

System_Boundary(atlas, "AtlasOps AI") {
  Container(adminWeb, "Admin Web", "React or Next.js", "Backoffice")
  Container(clientWeb, "Client Web", "React or Next.js", "Client portal")
  Container(api, "Backend API", "Node.js, Go or Java", "REST, OpenAPI and SSE")
  Container(worker, "Worker", "Node.js, Go or Java", "Async jobs and projections")
  Container(ai, "AI Adapter/Service", "Adapter or Python service", "Analysis and fallback")

  ContainerDb(pg, "PostgreSQL", "Relational", "Source of truth, outbox and ledger")
  ContainerDb(redis, "Redis/Valkey", "Key-value and streams", "Cache, locks and events")
  ContainerDb(storage, "MinIO/S3", "Object storage", "Files and previews")
  ContainerDb(search, "OpenSearch", "Search", "Text index")
  ContainerDb(mongo, "MongoDB", "Document", "Integration payload archive")
  ContainerDb(graph, "Neo4j", "Graph", "Relationship projection")
  ContainerDb(time, "TimescaleDB", "Time-series", "Metrics")
  ContainerDb(analytics, "ClickHouse", "OLAP", "Historical analytics")
  ContainerDb(eventStore, "EventStoreDB", "Event store", "Approval events")
}

Rel(user, adminWeb, "Uses")
Rel(user, clientWeb, "Uses")
Rel(adminWeb, api, "REST/SSE")
Rel(clientWeb, api, "REST/SSE")
Rel(api, pg, "Reads/Writes")
Rel(api, redis, "Cache/Locks/Publish")
Rel(api, storage, "Signed URLs")
Rel(api, worker, "Outbox/Streams")
Rel(worker, ai, "Analyze")
Rel(worker, search, "Index")
Rel(worker, mongo, "Archive")
Rel(worker, graph, "Project")
Rel(worker, time, "Measure")
Rel(worker, analytics, "Analyze")
Rel(worker, eventStore, "Approval stream")
```

## 3. Primary Journey

```mermaid
sequenceDiagram
    actor Client
    actor Analyst
    participant Web
    participant API
    participant PG as PostgreSQL
    participant S3 as MinIO/S3
    participant Stream as Redis Streams
    participant Worker
    participant AI
    participant SSE

    Client->>Web: Create request
    Web->>API: POST /requests
    API->>PG: Persist request
    API-->>Web: 201

    Client->>Web: Select large file
    Web->>API: Create upload session
    API->>PG: Persist document/session
    API-->>Web: Signed part URLs

    loop Parts
        Web->>S3: Upload part
    end

    Web->>API: Complete upload
    API->>S3: Complete multipart
    API->>PG: Confirm and write outbox
    API->>Stream: Dispatch event
    Stream->>Worker: Consume
    Worker->>S3: Read object
    Worker->>AI: Analyze
    AI-->>Worker: Result or fallback
    Worker->>PG: Persist analysis/approval
    Worker->>Stream: Publish status
    Stream->>SSE: Deliver
    SSE-->>Web: Completed

    Analyst->>API: Approve with If-Match
    API->>PG: Persist and ledger
    API->>Stream: ApprovalDecided
```

## 4. Multipart Upload State

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> UPLOADING
    UPLOADING --> PAUSED
    PAUSED --> UPLOADING
    UPLOADING --> COMPLETING
    COMPLETING --> COMPLETED
    COMPLETING --> FAILED
    FAILED --> UPLOADING
    UPLOADING --> ABORTED
    PENDING --> EXPIRED
    PAUSED --> EXPIRED
```

## 5. Request State

```mermaid
stateDiagram-v2
    [*] --> OPEN
    OPEN --> IN_PROGRESS
    OPEN --> CANCELLED
    IN_PROGRESS --> WAITING_CUSTOMER
    IN_PROGRESS --> COMPLETED
    IN_PROGRESS --> CANCELLED
    WAITING_CUSTOMER --> IN_PROGRESS
    WAITING_CUSTOMER --> CANCELLED
```

## 6. Document State

```mermaid
stateDiagram-v2
    [*] --> PENDING_UPLOAD
    PENDING_UPLOAD --> UPLOADING
    UPLOADING --> UPLOADED
    UPLOADING --> FAILED
    UPLOADED --> PROCESSING
    PROCESSING --> READY
    PROCESSING --> FAILED
    READY --> APPROVED
    READY --> REJECTED
    READY --> ARCHIVED
    REJECTED --> PROCESSING
    FAILED --> PROCESSING
    APPROVED --> ARCHIVED
```

## 7. Outbox and Projections

```mermaid
flowchart LR
    CMD[Business Command] --> TX[PostgreSQL Transaction]
    TX --> STATE[Business State]
    TX --> OUTBOX[Outbox]
    OUTBOX --> STREAM[(Redis Streams)]

    STREAM --> DOC[Document Consumer]
    STREAM --> NOTIF[Notification]
    STREAM --> SEARCH[Search]
    STREAM --> GRAPH[Graph]
    STREAM --> METRIC[Telemetry]
    STREAM --> ANALYTICS[Analytics]
    STREAM --> ARCHIVE[Archive]

    SEARCH --> OS[(OpenSearch)]
    SEARCH --> VECTOR[(pgvector)]
    GRAPH --> NEO[(Neo4j)]
    METRIC --> TS[(TimescaleDB)]
    ANALYTICS --> CH[(ClickHouse)]
    ARCHIVE --> MONGO[(MongoDB)]
```

## 8. Hybrid Search

```mermaid
flowchart TD
    Q[Query] --> AUTH[Tenant and Permission Context]
    AUTH --> TEXT[OpenSearch]
    AUTH --> SEM[pgvector]
    TEXT --> RANK[Hybrid Ranker]
    SEM --> RANK
    RANK --> HYDRATE[PostgreSQL Hydration]
    HYDRATE --> FILTER[Final Authorization]
    FILTER --> RESULTS[Results]
```

## 9. Approval Event Sourcing

```mermaid
sequenceDiagram
    participant API
    participant ES as EventStoreDB
    participant Projector
    participant PG as PostgreSQL Read Model

    API->>ES: ApprovalRequested expectedVersion=-1
    ES->>Projector: Event
    Projector->>PG: PENDING
    API->>ES: ApprovalApproved expectedVersion=0
    ES->>Projector: Event
    Projector->>PG: APPROVED
```

## 10. Verification Pipeline

```mermaid
flowchart LR
    FORMAT[Format] --> LINT[Lint]
    LINT --> TYPE[Typecheck]
    TYPE --> UNIT[Unit]
    UNIT --> BUILD[Build]
    BUILD --> MIG[Migration]
    MIG --> CONTRACT[Contract]
    CONTRACT --> INT[Integration]
    INT --> E2E[Playwright]
    E2E --> LOAD[k6]
    LOAD --> SEC[Security]
    SEC --> RELEASE[Release]
```

## 11. Agent Loop

```mermaid
flowchart TD
    ISSUE[Task Contract] --> PLAN[Planner]
    PLAN --> HUMAN[Human Review]
    HUMAN --> IMPLEMENT[Implementer]
    IMPLEMENT --> TEST[Test Engineer]
    TEST --> REVIEW[Reviewer]
    REVIEW --> SEC[Security/Architecture]
    SEC --> CI[CI]
    CI --> DOC[Documentation Agent]
    DOC --> MERGE[Human Merge]
    CI -->|Failure| IMPLEMENT
```

## 12. Compose Profiles

```mermaid
flowchart TB
    CORE[core] --> PG[PostgreSQL]
    CORE --> REDIS[Redis]
    CORE --> MINIO[MinIO]
    CORE --> MAIL[MailHog]

    ADV[advanced] --> MONGO[MongoDB]
    ADV --> NEO[Neo4j]
    ADV --> TS[TimescaleDB]
    ADV --> OS[OpenSearch]

    ANA[analytics] --> CH[ClickHouse]
    EVT[event-sourcing] --> ES[EventStoreDB]
    OBS[observability] --> GRAFANA[Metrics, Logs and Traces]
```
