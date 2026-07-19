# Adequação — APIs, Eventos de Domínio e Integrações

> **Referência:** Seções 3 (API conventions), 7 (Asynchronous processing), 8 (Realtime delivery), 9 (Integrations) e 10 (AI architecture) da TECHNICAL-SPECIFICATION  
> **Estado Atual:** Definido pela spec `monorepo-sdd-harness` e código em `backend/`  
> **Estado Alvo:** Definido por PROJECT-SCOPE e TECHNICAL-SPECIFICATION

---

## Visão Geral

O sistema atual expõe contratos REST para os módulos existentes (auth, tenants, users, customers, documents, requests, ai, analytics, audit). A visão alvo introduz novos contratos REST para 7 módulos adicionais, define eventos de domínio via transactional outbox + Redis Streams, adiciona SSE como protocolo de tempo real, implementa webhook outbound para integrações externas e integra MCP (Model Context Protocol) para acesso read-only a ferramentas externas.

Todas as APIs seguem o base path `/api/v1`, contrato de erro RFC 7807, paginação cursor-based, idempotência via `Idempotency-Key`, ETag/conditional requests e multi-tenancy via membership autenticada.

---

## 1. Contratos REST — Módulos Novos

### 1.1 Approvals

| Aspecto         | Estado Atual                      | Estado Alvo                                                                                                                                                                           | Ação Necessária | Prioridade |
| --------------- | --------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| CRUD Approvals  | Não existe endpoint de aprovações | `POST /api/v1/approvals` (submit), `GET /api/v1/approvals` (list), `GET /api/v1/approvals/{id}` (detail), `POST /api/v1/approvals/{id}/approve`, `POST /api/v1/approvals/{id}/reject` | Criar           | P2         |
| Idempotência    | N/A                               | Approval commands (approve, reject) aceitam `Idempotency-Key` para garantir idempotência                                                                                              | Criar           | P2         |
| ETag/Versioning | N/A                               | Approvals retornam ETag baseado em versão do aggregate; writes requerem `If-Match`                                                                                                    | Criar           | P3         |

### 1.2 Activities

| Aspecto       | Estado Atual                      | Estado Alvo                                                                                                                  | Ação Necessária | Prioridade |
| ------------- | --------------------------------- | ---------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Activity Feed | Não existe endpoint de atividades | `GET /api/v1/activities` (list paginada por tenant e entidade), `GET /api/v1/activities?entityId={id}` (filtro por entidade) | Criar           | P2         |
| Timeline Read | N/A                               | Endpoint retorna timeline de atividades com paginação cursor-based e filtro por tipo de atividade                            | Criar           | P2         |

### 1.3 Notifications

| Aspecto           | Estado Atual                        | Estado Alvo                                                                                                                                       | Ação Necessária | Prioridade |
| ----------------- | ----------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Notification CRUD | Não existe endpoint de notificações | `GET /api/v1/notifications` (list), `PATCH /api/v1/notifications/{id}/read` (mark as read), `POST /api/v1/notifications/read-all` (mark all read) | Criar           | P2         |
| Preferences       | N/A                                 | `GET /api/v1/notifications/preferences`, `PUT /api/v1/notifications/preferences` para configurar canais e tipos de notificação por usuário        | Criar           | P3         |

### 1.4 Integrations

| Aspecto          | Estado Atual                       | Estado Alvo                                                                                                                                                                                              | Ação Necessária | Prioridade |
| ---------------- | ---------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Integration CRUD | Não existe endpoint de integrações | `POST /api/v1/integrations` (create), `GET /api/v1/integrations` (list), `GET /api/v1/integrations/{id}` (detail), `PUT /api/v1/integrations/{id}` (update), `DELETE /api/v1/integrations/{id}` (remove) | Criar           | P2         |
| Execution        | N/A                                | `POST /api/v1/integrations/{id}/execute` para disparar execução manual; `GET /api/v1/integrations/{id}/executions` para histórico                                                                        | Criar           | P2         |
| Idempotência     | N/A                                | Integration execution aceita `Idempotency-Key`                                                                                                                                                           | Criar           | P3         |

### 1.5 Search

| Aspecto         | Estado Atual                           | Estado Alvo                                                                                                              | Ação Necessária | Prioridade |
| --------------- | -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ | --------------- | ---------- |
| Unified Search  | Não existe endpoint de busca unificada | `GET /api/v1/search?q={query}&type={entity-type}` — busca cross-entity (customers, requests, documents) com autocomplete | Criar           | P1         |
| Semantic Search | N/A                                    | `GET /api/v1/search/semantic?q={query}` — busca semântica via pgvector com score de relevância                           | Criar           | P2         |
| Command Palette | N/A                                    | `GET /api/v1/search/commands?q={query}` — navegação por command palette no frontend                                      | Criar           | P3         |

### 1.6 Imports

| Aspecto      | Estado Atual                      | Estado Alvo                                                                                                                                                  | Ação Necessária | Prioridade |
| ------------ | --------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------- | ---------- |
| Import Job   | Não existe endpoint de importação | `POST /api/v1/imports` (create job), `GET /api/v1/imports` (list), `GET /api/v1/imports/{id}` (status/progress), `POST /api/v1/imports/{id}/cancel` (cancel) | Criar           | P2         |
| Idempotência | N/A                               | Import creation aceita `Idempotency-Key` para evitar uploads duplicados                                                                                      | Criar           | P2         |
| Validation   | N/A                               | Response inclui row-level errors e contagem de linhas processadas/falhadas                                                                                   | Criar           | P3         |

### 1.7 Operations

| Aspecto            | Estado Atual                                    | Estado Alvo                                                                                                                                      | Ação Necessária | Prioridade |
| ------------------ | ----------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ | --------------- | ---------- |
| Health/Status      | Não existe endpoint administrativo de operações | `GET /api/v1/operations/health` (system health), `GET /api/v1/operations/status` (service status por componente)                                 | Criar           | P1         |
| Job Management     | N/A                                             | `GET /api/v1/operations/jobs` (list DLQ/failed), `POST /api/v1/operations/jobs/{id}/replay` (replay), `POST /api/v1/operations/jobs/{id}/cancel` | Criar           | P2         |
| Projection Rebuild | N/A                                             | `POST /api/v1/operations/projections/{name}/rebuild` para triggerar rebuild de projeções especializadas                                          | Criar           | P3         |

---

## 2. Contratos REST — Módulos Existentes (Alterações)

| Aspecto                 | Estado Atual                                      | Estado Alvo                                                                                                                                                                     | Ação Necessária | Prioridade |
| ----------------------- | ------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Documents — Upload Flow | Upload simples via backend proxy                  | Upload multipart direto ao MinIO via signed URLs; backend apenas orquestra sessão (`POST /api/v1/documents/{id}/upload-session`, `POST /api/v1/documents/{id}/complete-upload`) | Alterar         | P0         |
| Documents — Reprocess   | Não existe endpoint de reprocessamento            | `POST /api/v1/documents/{id}/reprocess` para re-executar análise de IA                                                                                                          | Criar           | P2         |
| Documents — Legal Hold  | Não existe                                        | `POST /api/v1/documents/{id}/hold`, `DELETE /api/v1/documents/{id}/hold` para controle de retenção legal                                                                        | Criar           | P3         |
| Requests — ETag         | Requests não usam ETag                            | `GET /api/v1/requests/{id}` retorna `ETag`; `PATCH` requer `If-Match` para evitar conflitos de edição concorrente                                                               | Alterar         | P0         |
| AI — Analysis Port      | Endpoint usa `AIAnalysisPort` com contrato antigo | Endpoint refatorado para usar `DocumentAnalysisPort` com request/response enriquecidos (tenant context, prompt version, output schema, confidence, fallback flag)               | Alterar         | P0         |
| Customers — Geospatial  | Customers não suportam busca geoespacial          | `GET /api/v1/customers?lat={lat}&lng={lng}&radius={km}` — radius-based queries via PostGIS                                                                                      | Criar           | P2         |

---

## 3. Eventos de Domínio

### 3.1 Transactional Outbox + Redis Streams

| Aspecto            | Estado Atual                  | Estado Alvo                                                                                                                                 | Ação Necessária | Prioridade |
| ------------------ | ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Outbox Pattern     | Não implementado              | Business state e evento commitados na mesma transação PostgreSQL; outbox poller publica em Redis Streams                                    | Criar           | P1         |
| Redis Stream       | Redis usado apenas como cache | Stream `atlasops-events` com consumer groups dedicados por responsabilidade                                                                 | Alterar         | P1         |
| Consumer Groups    | Não existem                   | `document-processing`, `notifications`, `search-index`, `vector-index`, `graph-projection`, `telemetry`, `analytics`, `integration-archive` | Criar           | P1         |
| Delivery Semantics | N/A                           | At-least-once delivery, idempotent effects, bounded retry (5x, 30s), DLQ, poison-message handling, lag metrics                              | Criar           | P2         |
| Event Versioning   | N/A                           | Todos os eventos versionados (`event.type.vN`), correlation ID propagado, tenant key obrigatório                                            | Criar           | P2         |

### 3.2 Domain Events — Customers

| Aspecto          | Estado Atual     | Estado Alvo                                                                                                     | Ação Necessária | Prioridade |
| ---------------- | ---------------- | --------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| CustomerCreated  | Não emite evento | `customer.created.v1` — emitido na criação do customer; consumido por search-index, graph-projection, analytics | Criar           | P2         |
| CustomerUpdated  | Não emite evento | `customer.updated.v1` — emitido na atualização; consumido por search-index, graph-projection                    | Criar           | P2         |
| CustomerArchived | Não emite evento | `customer.archived.v1` — emitido no archive; consumido por search-index (remove do índice)                      | Criar           | P3         |

### 3.3 Domain Events — Documents

| Aspecto           | Estado Atual     | Estado Alvo                                                                                                            | Ação Necessária | Prioridade |
| ----------------- | ---------------- | ---------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| DocumentUploaded  | Não emite evento | `document.uploaded.v1` — emitido após confirmação do upload; consumido por document-processing (Worker)                | Criar           | P1         |
| DocumentProcessed | Não emite evento | `document.processed.v1` — emitido após processamento completo; consumido por vector-index, search-index, notifications | Criar           | P2         |
| DocumentAnalyzed  | Não emite evento | `document.analyzed.v1` — emitido após análise de IA; consumido por notifications, analytics, activities                | Criar           | P2         |
| DocumentApproved  | Não emite evento | `document.approved.v1` — emitido após aprovação; consumido por notifications, activities, analytics                    | Criar           | P2         |
| DocumentRejected  | Não emite evento | `document.rejected.v1` — emitido após rejeição; consumido por notifications, activities                                | Criar           | P2         |

### 3.4 Domain Events — Requests

| Aspecto              | Estado Atual     | Estado Alvo                                                                                                 | Ação Necessária | Prioridade |
| -------------------- | ---------------- | ----------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| RequestCreated       | Não emite evento | `request.created.v1` — emitido na criação; consumido por notifications, activities, search-index, analytics | Criar           | P2         |
| RequestStatusChanged | Não emite evento | `request.status-changed.v1` — emitido em transições de estado; consumido por notifications, activities      | Criar           | P2         |

### 3.5 Domain Events — Approvals

| Aspecto           | Estado Atual     | Estado Alvo                                                                                                       | Ação Necessária | Prioridade |
| ----------------- | ---------------- | ----------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| ApprovalSubmitted | Não emite evento | `approval.submitted.v1` — emitido ao submeter aprovação; consumido por notifications, activities                  | Criar           | P2         |
| ApprovalDecided   | Não emite evento | `approval.decided.v1` — emitido após decisão (approve/reject); consumido por notifications, activities, analytics | Criar           | P2         |

### 3.6 Domain Events — Integrations

| Aspecto             | Estado Atual     | Estado Alvo                                                                                                | Ação Necessária | Prioridade |
| ------------------- | ---------------- | ---------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| IntegrationExecuted | Não emite evento | `integration.executed.v1` — emitido após execução; consumido por integration-archive (MongoDB), activities | Criar           | P2         |
| IntegrationFailed   | Não emite evento | `integration.failed.v1` — emitido após falha; consumido por notifications, DLQ, activities                 | Criar           | P2         |

### 3.7 Domain Events — Imports

| Aspecto         | Estado Atual     | Estado Alvo                                                                                     | Ação Necessária | Prioridade |
| --------------- | ---------------- | ----------------------------------------------------------------------------------------------- | --------------- | ---------- |
| ImportStarted   | Não emite evento | `import.started.v1` — emitido ao iniciar job; consumido por activities, notifications           | Criar           | P2         |
| ImportCompleted | Não emite evento | `import.completed.v1` — emitido ao concluir; consumido por activities, notifications, analytics | Criar           | P2         |
| ImportFailed    | Não emite evento | `import.failed.v1` — emitido em falha; consumido por notifications, DLQ                         | Criar           | P3         |

---

## 4. SSE Streams (Server-Sent Events)

| Aspecto              | Estado Atual            | Estado Alvo                                                                                                           | Ação Necessária | Prioridade |
| -------------------- | ----------------------- | --------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| SSE Endpoint         | Não existe endpoint SSE | `GET /api/v1/events/stream` — stream unificado de eventos em tempo real filtrado por tenant e user                    | Criar           | P1         |
| Authentication       | N/A                     | Conexão SSE autenticada via JWT (token no query param ou header); validação de tenant membership                      | Criar           | P1         |
| Notifications Stream | N/A                     | Notificações para o usuário atual (new request, document approved/rejected, import complete) entregues via SSE        | Criar           | P2         |
| Activities Timeline  | N/A                     | Atividades relevantes para o tenant/user entregues em tempo real (new customer, status changes)                       | Criar           | P2         |
| Import Progress      | N/A                     | Progresso de importação em lote (rows processed, percentage, errors) entregue via SSE ao usuário que iniciou o import | Criar           | P2         |
| Heartbeat            | N/A                     | Heartbeat periódico para manter conexão ativa e detectar clientes desconectados                                       | Criar           | P2         |
| Last-Event-ID        | N/A                     | Suporte a `Last-Event-ID` header para reconexão com bounded replay de eventos perdidos                                | Criar           | P2         |
| Backpressure         | N/A                     | Política de backpressure: buffer limitado por conexão; eventos acima do buffer são descartados com indicador de gap   | Criar           | P3         |
| Connection Metrics   | N/A                     | Métrica de conexões SSE ativas exposta em Prometheus (`atlasops_sse_active_connections`)                              | Criar           | P3         |
| Disconnect Cleanup   | N/A                     | Cleanup automático de conexões inativas (timeout de heartbeat > 60s) com liberação de recursos                        | Criar           | P3         |

---

## 5. Webhook Outbound

| Aspecto           | Estado Atual                             | Estado Alvo                                                                                                                               | Ação Necessária | Prioridade |
| ----------------- | ---------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Webhook Dispatch  | Não existe mecanismo de webhook outbound | Módulo `integrations` despacha webhooks para URLs configuradas por tenant; payload inclui event type, timestamp, tenant ID, resource data | Criar           | P2         |
| HMAC Signature    | N/A                                      | Cada webhook inclui `X-Webhook-Signature` com HMAC-SHA256 do body usando secret configurado por integração                                | Criar           | P2         |
| Delivery ID       | N/A                                      | Cada delivery possui ID único para idempotência e rastreabilidade do receptor                                                             | Criar           | P2         |
| Timestamp         | N/A                                      | Header `X-Webhook-Timestamp` em ISO 8601 UTC para proteção contra replay attacks                                                          | Criar           | P2         |
| Bounded Retry     | N/A                                      | Retry com backoff exponencial: até 5 tentativas (1s, 5s, 30s, 120s, 600s); após esgotar → DLQ                                             | Criar           | P2         |
| DLQ               | N/A                                      | Dead Letter Queue para deliveries falhadas; operadores podem inspecionar e replay via `POST /api/v1/operations/webhooks/{id}/replay`      | Criar           | P3         |
| Delivery History  | N/A                                      | `GET /api/v1/integrations/{id}/webhooks` — histórico de deliveries com status (DELIVERED, FAILED, PENDING, DEAD_LETTER)                   | Criar           | P3         |
| Idempotent Replay | N/A                                      | Replay de webhook produz mesmo delivery ID; receptor pode usar delivery ID para de-duplicação                                             | Criar           | P3         |
| Security Controls | N/A                                      | HTTPS obrigatório; blocking de loopback/private networks; timeout de 10s por delivery; tamanho máximo de payload 1MB                      | Criar           | P2         |

---

## 6. MCP Integration (Model Context Protocol)

| Aspecto                | Estado Atual              | Estado Alvo                                                                                                                            | Ação Necessária | Prioridade |
| ---------------------- | ------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| MCP Client             | Não existe integração MCP | Módulo `integrations` implementa client MCP para acesso read-only a ferramentas externas                                               | Criar           | P2         |
| Server Allowlist       | N/A                       | Apenas servidores MCP allowlisted podem ser conectados; configuração por tenant via `integrations`                                     | Criar           | P2         |
| Tool Schema Import     | N/A                       | Import automático do schema de tools disponíveis no servidor MCP conectado                                                             | Criar           | P2         |
| Per-Tool Policy        | N/A                       | Cada tool pode ser habilitada/desabilitada individualmente por tenant; timeout e step limit configuráveis                              | Criar           | P2         |
| Tenant-Aware Execution | N/A                       | Toda execução de tool MCP é scoped ao tenant; contexto de tenant é propagado para auditoria                                            | Criar           | P2         |
| Execution Limits       | N/A                       | Timeout por tool call (30s default); step limit (10 calls máximo por execution chain); result-size limit (1MB)                         | Criar           | P2         |
| Security Controls      | N/A                       | Prompt-injection review; sensitive-result redaction; no direct database credentials expostas; logs redacted                            | Criar           | P2         |
| Execution Archive      | N/A                       | Todas as execuções MCP são archivadas em MongoDB (via `MongoArchiveAdapter`); full tool-call trace preservado                          | Criar           | P2         |
| MCP API Endpoint       | N/A                       | `POST /api/v1/integrations/mcp/execute` — dispara execução de tool MCP; `GET /api/v1/integrations/mcp/tools` — lista tools disponíveis | Criar           | P2         |
| Fallback               | N/A                       | Se MCP server indisponível → retorna `INTEGRATION_UNAVAILABLE` (HTTP 503); não há fallback automático                                  | Criar           | P3         |

---

## 7. Contratos REST — Developer Documentation

| Aspecto               | Estado Atual                         | Estado Alvo                                                                                                                   | Ação Necessária | Prioridade |
| --------------------- | ------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| OpenAPI/Swagger       | SpringDoc configurado mas incompleto | OpenAPI 3.1 completo com exemplos em endpoints críticos; acessível em `/swagger-ui.html` (profile local); contract lint em CI | Alterar         | P1         |
| AsyncAPI              | Não existe                           | AsyncAPI spec documentando todos os domain events, consumer groups e schemas — acessível na área de developers                | Criar           | P2         |
| SSE Documentation     | N/A                                  | Exemplos de conexão SSE, event types, reconnection e Last-Event-ID na área de developers                                      | Criar           | P3         |
| Webhook Documentation | N/A                                  | Documentação de webhook payload format, signature verification, retry policy e endpoint configuration                         | Criar           | P3         |

---

## Resumo de Prioridades

| Prioridade | Descrição                  | Itens                                                                                                                                                                                |
| ---------- | -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **P0**     | Remover/alterar primeiro   | Upload flow (signed URLs), ETag em Requests, refactoring AIAnalysisPort → DocumentAnalysisPort                                                                                       |
| **P1**     | Criar scaffolding          | Transactional outbox, Redis Streams + consumer groups, SSE endpoint, Search unificada, Operations health, OpenAPI completo                                                           |
| **P2**     | Implementar funcionalidade | Todos os novos REST contracts (approvals, activities, notifications, integrations, imports), domain events, webhook outbound, MCP integration, SSE notifications/activities/progress |
| **P3**     | Hardening e polish         | ETag em Approvals, notification preferences, command palette, backpressure SSE, webhook DLQ/history/replay, MCP fallback, AsyncAPI, documentação SSE/webhook                         |

---

## Padrão de Eventos de Domínio (Aplicável a Todos os Eventos)

Todos os eventos de domínio seguem a mesma estrutura base:

```json
{
  "id": "evt_...",
  "type": "resource.action.v1",
  "tenantId": "tenant-...",
  "actorId": "user-...",
  "correlationId": "corr-...",
  "occurredAt": "2025-01-15T10:30:00Z",
  "data": {}
}
```

**Regras:**

1. **Versionamento:** Todos os eventos são versionados (`v1`, `v2`, etc.); breaking changes requerem nova versão
2. **Tenant Key:** Obrigatório em todos os eventos para garantir isolamento multi-tenant
3. **Correlation ID:** Propagado end-to-end para rastreabilidade
4. **Idempotência:** Consumers implementam efeitos idempotentes (upsert baseado em event ID)
5. **Retry:** Bounded retry (5 tentativas, 30s intervalo); após esgotar → DLQ
6. **Poison Message:** Mensagens que falham consistentemente são movidas para DLQ com metadata de diagnóstico

---

## Referências

- [TECHNICAL-SPECIFICATION — Seção 3: API conventions](TECHNICAL-SPECIFICATION.md)
- [TECHNICAL-SPECIFICATION — Seção 7: Asynchronous processing](TECHNICAL-SPECIFICATION.md)
- [TECHNICAL-SPECIFICATION — Seção 8: Realtime delivery](TECHNICAL-SPECIFICATION.md)
- [TECHNICAL-SPECIFICATION — Seção 9: Integrations](TECHNICAL-SPECIFICATION.md)
- [TECHNICAL-SPECIFICATION — Seção 10: AI architecture](TECHNICAL-SPECIFICATION.md)
- [PROJECT-SCOPE — Seção 4: Essential functional scope](PROJECT-SCOPE.md)
- [PROJECT-SCOPE — Seção 7: Communication protocols](PROJECT-SCOPE.md)
