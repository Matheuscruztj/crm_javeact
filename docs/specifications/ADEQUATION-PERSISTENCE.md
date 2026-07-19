# Adequação — Persistência Especializada

> **Referência:** Seção 11 (Persistence Responsibilities) e Seção 12 (Projection Lifecycle) da TECHNICAL-SPECIFICATION  
> **Estado Atual:** Definido pela spec `monorepo-sdd-harness` e código em `backend/`  
> **Estado Alvo:** Definido por PROJECT-SCOPE e TECHNICAL-SPECIFICATION

---

## Visão Geral

O sistema atual utiliza apenas PostgreSQL (com pgvector) como banco de dados. A visão alvo introduz 7 bancos especializados, cada um com responsabilidade específica, adapter dedicado, comportamento de fallback e lifecycle de projeções definido.

Todas as bases especializadas são **opcionais em runtime**, habilitáveis via propriedade `atlasops.{database}.enabled=true|false` e associadas a Docker Compose profiles.

---

## 1. OpenSearch

| Aspecto              | Estado Atual                         | Estado Alvo                                                                                                                                | Ação Necessária | Prioridade |
| -------------------- | ------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------ | --------------- | ---------- |
| Adapter              | Não existe                           | `OpenSearchAdapter` implementando `SearchPort` no módulo `search`                                                                          | Criar           | P1         |
| Módulo Consumidor    | N/A — busca textual não implementada | Módulo `search` consome OpenSearch para full-text search e autocomplete                                                                    | Criar           | P1         |
| Fallback/Degradação  | N/A                                  | Se OpenSearch indisponível → fallback para PostgreSQL text-search via `tsvector`/`LIKE`; write principal sucede normalmente                | Criar           | P2         |
| Projection Lifecycle | N/A                                  | Projeção de índice de busca com status (DISABLED → PENDING → PROCESSING → READY → STALE → FAILED), rebuild via `make rebuild-search-index` | Criar           | P2         |
| Configuração         | N/A                                  | Propriedade `atlasops.opensearch.enabled`, associado ao profile `advanced`                                                                 | Criar           | P1         |
| Health Check         | N/A                                  | Health check com intervalo 10s, timeout 5s, 12 retries; se falhar, serviço fica `unhealthy` sem reinício automático                        | Criar           | P2         |
| Docker Compose       | Não existe serviço OpenSearch        | Serviço OpenSearch no profile `advanced` com volumes persistentes                                                                          | Criar           | P1         |

---

## 2. MongoDB

| Aspecto              | Estado Atual                                | Estado Alvo                                                                                                                     | Ação Necessária | Prioridade |
| -------------------- | ------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Adapter              | Não existe                                  | `MongoArchiveAdapter` implementando port de archive no módulo `integrations`                                                    | Criar           | P2         |
| Módulo Consumidor    | N/A — archive de execuções não implementado | Módulo `integrations` usa MongoDB para archive de execuções REST e MCP                                                          | Criar           | P2         |
| Fallback/Degradação  | N/A                                         | Se MongoDB indisponível → metadata persiste em PostgreSQL; archive enfileira retry com máximo 5 tentativas em intervalos de 30s | Criar           | P2         |
| Projection Lifecycle | N/A                                         | Archive é append-only; não requer rebuild; reconciliação verifica completude via contagem de eventos vs documentos archivados   | Criar           | P3         |
| Configuração         | N/A                                         | Propriedade `atlasops.mongodb.enabled`, associado ao profile `advanced`                                                         | Criar           | P2         |
| Health Check         | N/A                                         | Health check com intervalo 10s, timeout 5s, 12 retries                                                                          | Criar           | P2         |
| Docker Compose       | Não existe serviço MongoDB                  | Serviço MongoDB no profile `advanced` com volumes persistentes                                                                  | Criar           | P2         |

---

## 3. Neo4j

| Aspecto              | Estado Atual                                       | Estado Alvo                                                                                                                                             | Ação Necessária | Prioridade |
| -------------------- | -------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Adapter              | Não existe                                         | `Neo4jGraphAdapter` implementando port de projeção de grafo no módulo `analytics` ou `shared-kernel`                                                    | Criar           | P2         |
| Módulo Consumidor    | N/A — projeção de relacionamentos não implementada | Módulos `analytics` e `customers` consomem Neo4j para projeções de relacionamento e impacto                                                             | Criar           | P2         |
| Fallback/Degradação  | N/A                                                | Se Neo4j indisponível → write principal sucede normalmente; projeção de grafo fica stale até reconexão; health check periódico (60s) monitora reconexão | Criar           | P2         |
| Projection Lifecycle | N/A                                                | Projeção de grafo com status (DISABLED → PENDING → PROCESSING → READY → STALE → FAILED), rebuild via `make rebuild-graph`, reconciliação periódica      | Criar           | P3         |
| Configuração         | N/A                                                | Propriedade `atlasops.neo4j.enabled`, associado ao profile `advanced`                                                                                   | Criar           | P2         |
| Health Check         | N/A                                                | Health check com intervalo 10s, timeout 5s, 12 retries                                                                                                  | Criar           | P2         |
| Docker Compose       | Não existe serviço Neo4j                           | Serviço Neo4j no profile `advanced` com volumes persistentes                                                                                            | Criar           | P2         |

---

## 4. TimescaleDB

| Aspecto              | Estado Atual                                               | Estado Alvo                                                                                                                                                                                        | Ação Necessária | Prioridade |
| -------------------- | ---------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Adapter              | Não existe                                                 | `TimescaleMetricsAdapter` implementando port de métricas time-series no módulo `analytics`                                                                                                         | Criar           | P2         |
| Módulo Consumidor    | N/A — métricas operacionais selecionadas não implementadas | Módulo `analytics` consome TimescaleDB para séries temporais selecionadas (métricas operacionais)                                                                                                  | Criar           | P2         |
| Fallback/Degradação  | N/A                                                        | Se TimescaleDB indisponível → endpoints de charts retornam indicação de dados indisponíveis (HTTP 503 com body explicativo); ingestão enfileira retry com máximo 5 tentativas em intervalos de 30s | Criar           | P2         |
| Projection Lifecycle | N/A                                                        | Projeção analítica com status lifecycle, rebuild via `make rebuild-analytics`, lag metric exposta em Prometheus                                                                                    | Criar           | P3         |
| Configuração         | N/A                                                        | Propriedade `atlasops.timescaledb.enabled`, associado ao profile `analytics`                                                                                                                       | Criar           | P2         |
| Health Check         | N/A                                                        | Health check com intervalo 10s, timeout 5s, 12 retries                                                                                                                                             | Criar           | P2         |
| Docker Compose       | Não existe serviço TimescaleDB                             | Serviço TimescaleDB no profile `analytics` com volumes persistentes                                                                                                                                | Criar           | P2         |

---

## 5. ClickHouse

| Aspecto              | Estado Atual                                        | Estado Alvo                                                                                                                                                                                       | Ação Necessária | Prioridade |
| -------------------- | --------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Adapter              | Não existe                                          | `ClickHouseAnalyticsAdapter` implementando port de projeção analítica histórica no módulo `analytics`                                                                                             | Criar           | P2         |
| Módulo Consumidor    | N/A — projeção analítica histórica não implementada | Módulo `analytics` consome ClickHouse para projeções analíticas históricas de alto volume                                                                                                         | Criar           | P2         |
| Fallback/Degradação  | N/A                                                 | Se ClickHouse indisponível → endpoints de charts retornam indicação de dados indisponíveis (HTTP 503 com body explicativo); ingestão enfileira retry com máximo 5 tentativas em intervalos de 30s | Criar           | P2         |
| Projection Lifecycle | N/A                                                 | Projeção analítica com status lifecycle, rebuild via `make rebuild-analytics`, lag metric, reconciliação periódica com PostgreSQL                                                                 | Criar           | P3         |
| Configuração         | N/A                                                 | Propriedade `atlasops.clickhouse.enabled`, associado ao profile `analytics`                                                                                                                       | Criar           | P2         |
| Health Check         | N/A                                                 | Health check com intervalo 10s, timeout 5s, 12 retries                                                                                                                                            | Criar           | P2         |
| Docker Compose       | Não existe serviço ClickHouse                       | Serviço ClickHouse no profile `analytics` com volumes persistentes                                                                                                                                | Criar           | P2         |

---

## 6. DuckDB

| Aspecto              | Estado Atual                                        | Estado Alvo                                                                                                                                                                      | Ação Necessária | Prioridade |
| -------------------- | --------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Adapter              | Não existe                                          | `DuckDBProcessingAdapter` implementando port de processamento temporário no módulo `imports`                                                                                     | Criar           | P2         |
| Módulo Consumidor    | N/A — processamento de CSV/Parquet não implementado | Módulo `imports` consome DuckDB para processamento temporário de arquivos CSV e Parquet durante importações em lote                                                              | Criar           | P2         |
| Fallback/Degradação  | N/A                                                 | Se DuckDB indisponível → processamento de CSV/Parquet retorna erro 503 indicando serviço temporariamente indisponível; não há retry automático (processamento é síncrono ao job) | Criar           | P3         |
| Projection Lifecycle | N/A                                                 | Não se aplica — DuckDB é usado para processamento efêmero, não para projeções persistentes; dados temporários são descartados após conclusão do job de import                    | Manter          | P3         |
| Configuração         | N/A                                                 | Não requer propriedade de habilitação — DuckDB é embarcado (in-process) e ativado quando o módulo `imports` processa arquivos; sem serviço Docker dedicado                       | Criar           | P3         |
| Health Check         | N/A                                                 | Não se aplica — DuckDB é biblioteca embarcada, não serviço de rede; disponibilidade é determinada pela presença do JAR no classpath                                              | Manter          | P3         |
| Docker Compose       | N/A                                                 | Não requer serviço Docker — DuckDB é executado in-process como dependência Maven/Gradle do módulo `imports`                                                                      | Manter          | P3         |

---

## 7. EventStoreDB

| Aspecto              | Estado Atual                                        | Estado Alvo                                                                                                                                                                                                      | Ação Necessária | Prioridade |
| -------------------- | --------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Adapter              | Não existe                                          | `EventStoreApprovalAdapter` implementando port de event sourcing no módulo `approvals`                                                                                                                           | Criar           | P2         |
| Módulo Consumidor    | N/A — event sourcing de aprovações não implementado | Módulo `approvals` usa EventStoreDB como source of truth para fluxos de aprovação via event sourcing                                                                                                             | Criar           | P2         |
| Fallback/Degradação  | N/A                                                 | Se EventStoreDB indisponível → operações de aprovação ficam bloqueadas (não há fallback seguro para event sourcing — consistência é crítica); sistema retorna HTTP 503 para operações de aprovação até reconexão | Criar           | P2         |
| Projection Lifecycle | N/A                                                 | Event streams por tenant e por approval; projeções de read model em PostgreSQL; rebuild via replay de eventos; status lifecycle completo (DISABLED → READY)                                                      | Criar           | P3         |
| Configuração         | N/A                                                 | Propriedade `atlasops.eventstoredb.enabled`, associado ao profile `event-sourcing`                                                                                                                               | Criar           | P2         |
| Health Check         | N/A                                                 | Health check com intervalo 10s, timeout 5s, 12 retries                                                                                                                                                           | Criar           | P2         |
| Docker Compose       | Não existe serviço EventStoreDB                     | Serviço EventStoreDB no profile `event-sourcing` com volumes persistentes                                                                                                                                        | Criar           | P2         |

---

## Resumo de Prioridades

| Prioridade | Descrição                  | Itens                                                                                          |
| ---------- | -------------------------- | ---------------------------------------------------------------------------------------------- |
| **P0**     | Remover/alterar primeiro   | Nenhum item de persistência requer remoção — PostgreSQL é mantido como está                    |
| **P1**     | Criar scaffolding          | OpenSearch (adapter, config, Docker), pois busca textual é funcionalidade core do produto      |
| **P2**     | Implementar funcionalidade | MongoDB, Neo4j, TimescaleDB, ClickHouse, EventStoreDB (adapters, config, Docker, fallback)     |
| **P3**     | Hardening e polish         | DuckDB (processamento efêmero), projection lifecycle avançado, reconciliação, rebuild commands |

---

## Padrão de Circuit Breaker (Aplicável a Todas as Bases Especializadas)

Todas as bases de dados especializadas (exceto DuckDB que é in-process) seguem o mesmo padrão de degradação:

1. **Detecção:** Timeout de conexão superior a 5 segundos indica indisponibilidade
2. **Retry:** Até 5 tentativas com intervalo de 30 segundos entre cada
3. **Após esgotar retries:** Log ERROR com nome do adapter + timestamp; cessar novas tentativas
4. **Health check periódico:** Intervalo de 60 segundos monitora reconexão
5. **Reconexão:** Quando serviço responde ao health check, retomar fluxo normal em até 60 segundos
6. **Configuração:** Cada base habilitável via `atlasops.{database}.enabled=true|false`

---

## Referências

- [TECHNICAL-SPECIFICATION — Seção 11: Persistence Responsibilities](TECHNICAL-SPECIFICATION.md)
- [TECHNICAL-SPECIFICATION — Seção 12: Projection Lifecycle](TECHNICAL-SPECIFICATION.md)
- [TECHNICAL-SPECIFICATION — Seção 14: Health and Degradation](TECHNICAL-SPECIFICATION.md)
- [TECHNICAL-SPECIFICATION — Seção 15: Configuration and Profiles](TECHNICAL-SPECIFICATION.md)
