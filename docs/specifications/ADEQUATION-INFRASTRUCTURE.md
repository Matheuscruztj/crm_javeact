# Adequação — Infraestrutura

> **Referência:** Seções 13 (Docker Profiles), 14 (Health and Degradation) e 15 (Configuration and Profiles) da TECHNICAL-SPECIFICATION  
> **Estado Atual:** Definido pela spec `monorepo-sdd-harness` e código em `infra/docker-compose.yml`  
> **Estado Alvo:** Definido por PROJECT-SCOPE e TECHNICAL-SPECIFICATION

---

## Visão Geral

O sistema atual utiliza um Docker Compose simples (`infra/docker-compose.yml`) com serviços básicos (PostgreSQL, Redis, MinIO, Ollama, Prometheus, Grafana, Loki, MailHog). A visão alvo introduz Docker Compose com profiles organizados por categoria funcional, Worker Process separado da API, novos serviços especializados, comandos Make dedicados por profile e variáveis de ambiente para cada novo serviço.

---

## 1. Serviços Docker Compose — Core

| Aspecto     | Estado Atual                                                                        | Estado Alvo                                                                                                                | Ação Necessária | Prioridade |
| ----------- | ----------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| PostgreSQL  | Existe em `infra/docker-compose.yml` com pgvector (imagem `pgvector/pgvector:pg16`) | Mantido como source of truth transacional com pgvector + PostGIS; sem profile (inicia com qualquer profile)                | Manter          | P0         |
| Redis       | Existe em `infra/docker-compose.yml` (imagem `redis:7-alpine`)                      | Mantido para cache e mensageria; sem profile (inicia com qualquer profile)                                                 | Manter          | P0         |
| MinIO       | Existe em `infra/docker-compose.yml` (imagem `minio/minio`)                         | Mantido para object storage S3-compatível; sem profile (inicia com qualquer profile)                                       | Manter          | P0         |
| Backend API | Não existia como serviço Docker Compose (executado localmente)                      | Serviço `backend-api` com Dockerfile em `backend/app-boot`, depends_on PostgreSQL/Redis/MinIO healthy, porta 8080          | Criar           | P1         |
| Worker      | Não existia                                                                         | Serviço `worker` separado da API com Dockerfile em `backend/worker`, depends_on PostgreSQL/Redis/MinIO healthy, porta 8081 | Criar           | P1         |

---

## 2. Serviços Docker Compose — Advanced

| Aspecto    | Estado Atual               | Estado Alvo                                                                                                         | Ação Necessária | Prioridade |
| ---------- | -------------------------- | ------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| OpenSearch | Não existia serviço Docker | Serviço `opensearch` no profile `advanced`, imagem `opensearchproject/opensearch:2`, portas 9200/9600, health check | Criar           | P1         |
| MongoDB    | Não existia serviço Docker | Serviço `mongodb` no profile `advanced`, imagem `mongo:7`, porta 27017, health check                                | Criar           | P2         |
| Neo4j      | Não existia serviço Docker | Serviço `neo4j` no profile `advanced`, imagem `neo4j:5`, portas 7474/7687, health check                             | Criar           | P2         |

---

## 3. Serviços Docker Compose — Analytics

| Aspecto     | Estado Atual               | Estado Alvo                                                                                                               | Ação Necessária | Prioridade |
| ----------- | -------------------------- | ------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| TimescaleDB | Não existia serviço Docker | Serviço `timescaledb` no profile `analytics`, imagem `timescale/timescaledb:latest-pg16`, porta 5433, health check        | Criar           | P2         |
| ClickHouse  | Não existia serviço Docker | Serviço `clickhouse` no profile `analytics`, imagem `clickhouse/clickhouse-server:latest`, portas 8123/9009, health check | Criar           | P2         |

---

## 4. Serviços Docker Compose — Event-Sourcing

| Aspecto      | Estado Atual               | Estado Alvo                                                                                                               | Ação Necessária | Prioridade |
| ------------ | -------------------------- | ------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| EventStoreDB | Não existia serviço Docker | Serviço `eventstoredb` no profile `event-sourcing`, imagem `eventstore/eventstore:latest`, portas 2113/1113, health check | Criar           | P2         |

---

## 5. Serviços Docker Compose — Observability

| Aspecto    | Estado Atual                                                           | Estado Alvo                                                                                                    | Ação Necessária | Prioridade |
| ---------- | ---------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Prometheus | Existe em `infra/docker-compose.yml` (sem profile, imagem versão fixa) | Movido para profile `observability`, imagem `prom/prometheus:latest`, porta 9090, health check                 | Alterar         | P0         |
| Grafana    | Existe em `infra/docker-compose.yml` (sem profile, imagem versão fixa) | Movido para profile `observability`, imagem `grafana/grafana:latest`, porta 3000, depends_on Prometheus e Loki | Alterar         | P0         |
| Loki       | Existe em `infra/docker-compose.yml` (sem profile, imagem versão fixa) | Movido para profile `observability`, imagem `grafana/loki:latest`, porta 3100, health check                    | Alterar         | P0         |
| Tempo      | Não existia serviço Docker                                             | Serviço `tempo` no profile `observability`, imagem `grafana/tempo:latest`, portas 3200/4317/4318, health check | Criar           | P2         |
| MailHog    | Existe em `infra/docker-compose.yml` (sem profile)                     | Movido para profile `observability`, portas 8025/1025, health check                                            | Alterar         | P0         |

---

## 6. Serviço Removido

| Aspecto | Estado Atual                                                        | Estado Alvo                                                                                         | Ação Necessária | Prioridade |
| ------- | ------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Ollama  | Existe em `infra/docker-compose.yml` (imagem `ollama/ollama:0.3.4`) | Removido do Docker Compose principal; execução gerenciada externamente ou via configuração dedicada | Remover         | P0         |

---

## 7. Docker Compose Profiles

| Aspecto                | Estado Atual                                                      | Estado Alvo                                                                                                                         | Ação Necessária | Prioridade |
| ---------------------- | ----------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Profile core           | Não existia conceito de profiles; todos os serviços subiam juntos | Profile `core`: PostgreSQL, Redis, MinIO, Backend API, Worker — serviços base sem diretiva `profiles:` (sobem com qualquer profile) | Criar           | P1         |
| Profile advanced       | Não existia                                                       | Profile `advanced`: OpenSearch, MongoDB, Neo4j; depends_on core services com condition `service_healthy`                            | Criar           | P1         |
| Profile analytics      | Não existia                                                       | Profile `analytics`: TimescaleDB, ClickHouse; depends_on core services com condition `service_healthy`                              | Criar           | P2         |
| Profile event-sourcing | Não existia                                                       | Profile `event-sourcing`: EventStoreDB; depends_on core services com condition `service_healthy`                                    | Criar           | P2         |
| Profile observability  | Existia parcialmente (serviços de monitoring sem profile)         | Profile `observability`: Prometheus, Grafana, Loki, Tempo, MailHog; depends_on core services com condition `service_healthy`        | Alterar         | P0         |

---

## 8. Worker Process

| Aspecto                   | Estado Atual | Estado Alvo                                                                                                                                   | Ação Necessária | Prioridade |
| ------------------------- | ------------ | --------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Módulo Spring Boot        | Não existia  | Módulo `backend/worker/` com plugins `org.springframework.boot` e `io.spring.dependency-management`                                           | Criar           | P1         |
| Classe Principal          | N/A          | `com.atlasops.worker.WorkerApplication` anotada com `@SpringBootApplication`                                                                  | Criar           | P1         |
| Dependências de Módulo    | N/A          | Depende de `shared-kernel`, `documents`, `ai`, `notifications`, `imports`                                                                     | Criar           | P1         |
| Configuração              | N/A          | `application.yml` com conexões para Redis (host, port, consumer group), PostgreSQL (url, user, password via env vars), MinIO (endpoint, keys) | Criar           | P1         |
| Docker Compose Service    | Não existia  | Serviço `worker` separado do `backend-api` na rede `atlasops-network`, depends_on PostgreSQL/Redis/MinIO com condition `service_healthy`      | Criar           | P1         |
| Health Check              | N/A          | Health check em porta 8081 (`/actuator/health`), interval 10s, timeout 5s, retries 12                                                         | Criar           | P2         |
| Compilação Independente   | N/A          | `./gradlew :backend:worker:build` compila independentemente do `app-boot`                                                                     | Criar           | P1         |
| Feature Flags (databases) | N/A          | Propriedades `atlasops.databases.{db}.enabled` para cada base especializada                                                                   | Criar           | P2         |

---

## 9. Comandos Make

| Aspecto                       | Estado Atual                                                     | Estado Alvo                                                                                                                                                            | Ação Necessária | Prioridade |
| ----------------------------- | ---------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| `make compose-up`             | Inicia `infra/docker-compose.yml` com todos os serviços          | Mantido como comando legado para `infra/docker-compose.yml`                                                                                                            | Manter          | P0         |
| `make compose-down`           | Para `infra/docker-compose.yml`                                  | Mantido como comando legado                                                                                                                                            | Manter          | P0         |
| `make compose-reset`          | Remove volumes e recria containers de `infra/docker-compose.yml` | Mantido como comando legado                                                                                                                                            | Manter          | P0         |
| `make compose-core`           | Não existia                                                      | Inicia profile `core` via `docker compose --profile core up -d`                                                                                                        | Criar           | P1         |
| `make compose-advanced`       | Não existia                                                      | Inicia profile `advanced` via `docker compose --profile advanced up -d`                                                                                                | Criar           | P1         |
| `make compose-analytics`      | Não existia                                                      | Inicia profile `analytics` via `docker compose --profile analytics up -d`                                                                                              | Criar           | P2         |
| `make compose-event-sourcing` | Não existia                                                      | Inicia profile `event-sourcing` via `docker compose --profile event-sourcing up -d`                                                                                    | Criar           | P2         |
| `make compose-observability`  | Não existia                                                      | Inicia profile `observability` via `docker compose --profile observability up -d`                                                                                      | Criar           | P2         |
| `make compose-all`            | Não existia                                                      | Inicia todos os profiles simultâneos via `docker compose --profile core --profile advanced --profile analytics --profile event-sourcing --profile observability up -d` | Criar           | P2         |

---

## 10. Variáveis de Ambiente

### 10.1 Variáveis Existentes (Mantidas)

| Aspecto             | Estado Atual               | Estado Alvo           | Ação Necessária | Prioridade |
| ------------------- | -------------------------- | --------------------- | --------------- | ---------- |
| `POSTGRES_USER`     | Definida em `.env.example` | Mantida sem alteração | Manter          | P0         |
| `POSTGRES_PASSWORD` | Definida em `.env.example` | Mantida sem alteração | Manter          | P0         |
| `POSTGRES_PORT`     | Definida em `.env.example` | Mantida sem alteração | Manter          | P0         |
| `REDIS_PORT`        | Definida em `.env.example` | Mantida sem alteração | Manter          | P0         |
| `MINIO_ACCESS_KEY`  | Definida em `.env.example` | Mantida sem alteração | Manter          | P0         |
| `MINIO_SECRET_KEY`  | Definida em `.env.example` | Mantida sem alteração | Manter          | P0         |
| `MINIO_PORT`        | Definida em `.env.example` | Mantida sem alteração | Manter          | P0         |
| `APP_PORT`          | Definida em `.env.example` | Mantida sem alteração | Manter          | P0         |
| `GRAFANA_PORT`      | Definida em `.env.example` | Mantida sem alteração | Manter          | P0         |
| `PROMETHEUS_PORT`   | Definida em `.env.example` | Mantida sem alteração | Manter          | P0         |
| `LOKI_PORT`         | Definida em `.env.example` | Mantida sem alteração | Manter          | P0         |
| `MAILHOG_PORT`      | Definida em `.env.example` | Mantida sem alteração | Manter          | P0         |

### 10.2 Variáveis Novas — Serviços Docker

| Aspecto                  | Estado Atual | Estado Alvo                                              | Ação Necessária | Prioridade |
| ------------------------ | ------------ | -------------------------------------------------------- | --------------- | ---------- |
| `OPENSEARCH_PORT`        | Não existia  | Porta do OpenSearch (default: 9200)                      | Criar           | P1         |
| `OPENSEARCH_PERF_PORT`   | Não existia  | Porta de performance do OpenSearch (default: 9600)       | Criar           | P1         |
| `MONGO_USER`             | Não existia  | Usuário root do MongoDB (default: atlasops)              | Criar           | P2         |
| `MONGO_PASSWORD`         | Não existia  | Senha root do MongoDB (default: atlasops_local)          | Criar           | P2         |
| `MONGO_PORT`             | Não existia  | Porta do MongoDB (default: 27017)                        | Criar           | P2         |
| `NEO4J_USER`             | Não existia  | Usuário do Neo4j (default: neo4j)                        | Criar           | P2         |
| `NEO4J_PASSWORD`         | Não existia  | Senha do Neo4j (default: atlasops_local)                 | Criar           | P2         |
| `NEO4J_HTTP_PORT`        | Não existia  | Porta HTTP do Neo4j (default: 7474)                      | Criar           | P2         |
| `NEO4J_BOLT_PORT`        | Não existia  | Porta Bolt do Neo4j (default: 7687)                      | Criar           | P2         |
| `TIMESCALE_USER`         | Não existia  | Usuário do TimescaleDB (default: atlasops)               | Criar           | P2         |
| `TIMESCALE_PASSWORD`     | Não existia  | Senha do TimescaleDB (default: atlasops_local)           | Criar           | P2         |
| `TIMESCALE_DB`           | Não existia  | Nome do banco TimescaleDB (default: atlasops_timeseries) | Criar           | P2         |
| `TIMESCALE_PORT`         | Não existia  | Porta do TimescaleDB (default: 5433)                     | Criar           | P2         |
| `CLICKHOUSE_USER`        | Não existia  | Usuário do ClickHouse (default: atlasops)                | Criar           | P2         |
| `CLICKHOUSE_PASSWORD`    | Não existia  | Senha do ClickHouse (default: atlasops_local)            | Criar           | P2         |
| `CLICKHOUSE_DB`          | Não existia  | Nome do banco ClickHouse (default: atlasops_analytics)   | Criar           | P2         |
| `CLICKHOUSE_HTTP_PORT`   | Não existia  | Porta HTTP do ClickHouse (default: 8123)                 | Criar           | P2         |
| `CLICKHOUSE_NATIVE_PORT` | Não existia  | Porta nativa do ClickHouse (default: 9009)               | Criar           | P2         |
| `EVENTSTORE_HTTP_PORT`   | Não existia  | Porta HTTP do EventStoreDB (default: 2113)               | Criar           | P2         |
| `EVENTSTORE_TCP_PORT`    | Não existia  | Porta TCP do EventStoreDB (default: 1113)                | Criar           | P2         |
| `TEMPO_PORT`             | Não existia  | Porta HTTP do Tempo (default: 3200)                      | Criar           | P2         |
| `TEMPO_OTLP_GRPC_PORT`   | Não existia  | Porta OTLP gRPC do Tempo (default: 4317)                 | Criar           | P2         |
| `TEMPO_OTLP_HTTP_PORT`   | Não existia  | Porta OTLP HTTP do Tempo (default: 4318)                 | Criar           | P2         |
| `GRAFANA_ADMIN_USER`     | Não existia  | Usuário admin do Grafana (default: admin)                | Criar           | P2         |
| `GRAFANA_ADMIN_PASSWORD` | Não existia  | Senha admin do Grafana (default: admin)                  | Criar           | P2         |
| `MINIO_CONSOLE_PORT`     | Não existia  | Porta do console MinIO (default: 9001)                   | Criar           | P1         |

### 10.3 Variáveis Novas — Worker e Feature Flags

| Aspecto                         | Estado Atual | Estado Alvo                                                                                 | Ação Necessária | Prioridade |
| ------------------------------- | ------------ | ------------------------------------------------------------------------------------------- | --------------- | ---------- |
| `POSTGRES_URL`                  | Não existia  | URL JDBC do PostgreSQL para o Worker (default: `jdbc:postgresql://localhost:5432/atlasops`) | Criar           | P1         |
| `REDIS_HOST`                    | Não existia  | Host do Redis para o Worker (default: localhost)                                            | Criar           | P1         |
| `MINIO_ENDPOINT`                | Não existia  | Endpoint do MinIO para o Worker (default: `http://localhost:9000`)                          | Criar           | P1         |
| `ATLASOPS_OPENSEARCH_ENABLED`   | Não existia  | Feature flag para habilitar OpenSearch (default: false)                                     | Criar           | P2         |
| `ATLASOPS_MONGODB_ENABLED`      | Não existia  | Feature flag para habilitar MongoDB (default: false)                                        | Criar           | P2         |
| `ATLASOPS_NEO4J_ENABLED`        | Não existia  | Feature flag para habilitar Neo4j (default: false)                                          | Criar           | P2         |
| `ATLASOPS_TIMESCALEDB_ENABLED`  | Não existia  | Feature flag para habilitar TimescaleDB (default: false)                                    | Criar           | P2         |
| `ATLASOPS_CLICKHOUSE_ENABLED`   | Não existia  | Feature flag para habilitar ClickHouse (default: false)                                     | Criar           | P2         |
| `ATLASOPS_EVENTSTOREDB_ENABLED` | Não existia  | Feature flag para habilitar EventStoreDB (default: false)                                   | Criar           | P2         |

---

## 11. Rede e Volumes

| Aspecto             | Estado Atual                                              | Estado Alvo                                                                | Ação Necessária | Prioridade |
| ------------------- | --------------------------------------------------------- | -------------------------------------------------------------------------- | --------------- | ---------- |
| Rede Docker         | `atlasops-network` (bridge) em `infra/docker-compose.yml` | Mantida como `atlasops-network` (bridge) no novo `docker-compose.yml` raiz | Manter          | P0         |
| Volume PostgreSQL   | `atlasops-pgdata`                                         | Mantido                                                                    | Manter          | P0         |
| Volume Redis        | `atlasops-redisdata`                                      | Mantido                                                                    | Manter          | P0         |
| Volume MinIO        | `atlasops-miniodata`                                      | Mantido                                                                    | Manter          | P0         |
| Volume Grafana      | `atlasops-grafanadata`                                    | Mantido                                                                    | Manter          | P0         |
| Volume Ollama       | `atlasops-ollamadata`                                     | Removido (Ollama removido do Docker Compose)                               | Remover         | P0         |
| Volume OpenSearch   | Não existia                                               | `atlasops-opensearchdata` para persistência de índices                     | Criar           | P1         |
| Volume MongoDB      | Não existia                                               | `atlasops-mongodata` para persistência de documentos                       | Criar           | P2         |
| Volume Neo4j        | Não existia                                               | `atlasops-neo4jdata` para persistência do grafo                            | Criar           | P2         |
| Volume TimescaleDB  | Não existia                                               | `atlasops-timescaledata` para persistência de séries temporais             | Criar           | P2         |
| Volume ClickHouse   | Não existia                                               | `atlasops-clickhousedata` para persistência de dados analíticos            | Criar           | P2         |
| Volume EventStoreDB | Não existia                                               | `atlasops-eventstoredata` para persistência de event streams               | Criar           | P2         |
| Volume Tempo        | Não existia                                               | `atlasops-tempodata` para persistência de traces                           | Criar           | P2         |

---

## 12. Health Checks

| Aspecto                  | Estado Atual                                                    | Estado Alvo                                                                                                              | Ação Necessária | Prioridade |
| ------------------------ | --------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ | --------------- | ---------- |
| Padrão de Health Check   | Interval 10s, timeout 5s, retries 12 (para serviços existentes) | Mantido como padrão para todos os serviços (interval: 10s, timeout: 5s, retries: 12)                                     | Manter          | P0         |
| Backend API health       | Não existia no compose                                          | `curl -f http://localhost:8080/actuator/health`                                                                          | Criar           | P1         |
| Worker health            | Não existia                                                     | `curl -f http://localhost:8081/actuator/health`                                                                          | Criar           | P1         |
| OpenSearch health        | Não existia                                                     | `curl -f http://localhost:9200/_cluster/health`                                                                          | Criar           | P1         |
| MongoDB health           | Não existia                                                     | `mongosh --eval "db.adminCommand('ping')"`                                                                               | Criar           | P2         |
| Neo4j health             | Não existia                                                     | `curl -f http://localhost:7474`                                                                                          | Criar           | P2         |
| TimescaleDB health       | Não existia                                                     | `pg_isready -U atlasops -d atlasops_timeseries`                                                                          | Criar           | P2         |
| ClickHouse health        | Não existia                                                     | `curl -f http://localhost:8123/ping`                                                                                     | Criar           | P2         |
| EventStoreDB health      | Não existia                                                     | `curl -f http://localhost:2113/health/live`                                                                              | Criar           | P2         |
| Tempo health             | Não existia                                                     | `wget --spider -q http://localhost:3200/ready`                                                                           | Criar           | P2         |
| Comportamento após falha | Não definido formalmente                                        | Serviço permanece em estado `unhealthy` sem reinício automático após esgotar 12 retries, permitindo diagnóstico via logs | Criar           | P2         |

---

## Resumo de Prioridades

| Prioridade | Descrição                  | Itens                                                                                                                                                                    |
| ---------- | -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **P0**     | Remover/alterar primeiro   | Remover Ollama; alterar Prometheus/Grafana/Loki/MailHog para profile `observability`; manter serviços core (PostgreSQL, Redis, MinIO); manter variáveis existentes       |
| **P1**     | Criar scaffolding          | Worker Process (módulo, Docker service, config); Backend API como serviço Docker; profiles `core` e `advanced`; Make targets; OpenSearch; variáveis de conexão do Worker |
| **P2**     | Implementar funcionalidade | Profiles `analytics`, `event-sourcing`; serviços especializados (MongoDB, Neo4j, TimescaleDB, ClickHouse, EventStoreDB, Tempo); feature flags; health checks; volumes    |
| **P3**     | Hardening e polish         | Otimização de imagens Docker; tuning de health check intervals; documentação de troubleshooting; monitoramento avançado de containers                                    |

---

## Referências

- [TECHNICAL-SPECIFICATION](TECHNICAL-SPECIFICATION.md)
- [PROJECT-SCOPE](PROJECT-SCOPE.md)
- [ADEQUATION-PERSISTENCE](ADEQUATION-PERSISTENCE.md) — Detalhes de cada banco especializado
- [Docker Compose com Profiles](../../docker-compose.yml) — Implementação atual
- [Makefile](../../Makefile) — Comandos Make implementados
