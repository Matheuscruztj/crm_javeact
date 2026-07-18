# Runbook: Desenvolvimento Local

> **Versão:** 1.0  
> **Última atualização:** 2025-01-20

---

## Pré-requisitos

| Ferramenta     | Versão Mínima | Verificação              |
| -------------- | ------------- | ------------------------ |
| Java (JDK)     | 21+           | `java -version`          |
| Docker         | 24+           | `docker --version`       |
| Docker Compose | 2.20+         | `docker compose version` |
| Git            | 2.30+         | `git --version`          |
| Make           | 3.80+         | `make --version`         |

---

## Setup Rápido (Bootstrap)

```bash
# 1. Clonar o repositório
git clone <url-do-repositorio>
cd atlasops-ai

# 2. Executar bootstrap (faz tudo automaticamente)
make bootstrap
```

O `make bootstrap` executa:

1. Verifica pré-requisitos (Java 21+, Docker 24+, portas livres)
2. Copia `.env.example` para `.env` (se não existir)
3. Inicia Docker Compose (infraestrutura local)
4. Aguarda serviços ficarem healthy
5. Executa migrations de banco de dados
6. Executa `make verify` para validar o ambiente

---

## Setup Manual (Passo a Passo)

### 1. Configurar variáveis de ambiente

```bash
cp .env.example .env
# Editar .env se necessário (portas, credenciais locais)
```

### 2. Iniciar infraestrutura

```bash
make compose-up
# Aguardar ~30s para todos os serviços ficarem healthy
```

### 3. Verificar serviços

```bash
make doctor
```

### 4. Executar migrations

```bash
make migrate
```

### 5. Popular dados de demonstração (opcional)

```bash
make seed
```

### 6. Executar build e testes

```bash
make verify
```

---

## Portas dos Serviços

| Serviço       | Porta | URL                    |
| ------------- | ----- | ---------------------- |
| Backend API   | 8080  | http://localhost:8080  |
| PostgreSQL    | 5432  | localhost:5432         |
| Redis         | 6379  | localhost:6379         |
| MinIO         | 9000  | http://localhost:9000  |
| MinIO Console | 9001  | http://localhost:9001  |
| Ollama        | 11434 | http://localhost:11434 |
| Grafana       | 3000  | http://localhost:3000  |
| Prometheus    | 9090  | http://localhost:9090  |
| Loki          | 3100  | http://localhost:3100  |
| MailHog       | 8025  | http://localhost:8025  |

---

## Comandos Frequentes

| Comando                 | Descrição                                     |
| ----------------------- | --------------------------------------------- |
| `make compose-up`       | Iniciar infraestrutura local                  |
| `make compose-down`     | Parar infraestrutura                          |
| `make compose-reset`    | Limpar volumes e recriar do zero              |
| `make test-unit`        | Executar testes unitários                     |
| `make test-integration` | Executar testes de integração (requer Docker) |
| `make verify`           | Pipeline completo de quality gates            |
| `make format`           | Formatar código (Spotless)                    |
| `make lint`             | Verificar lint (Checkstyle + SpotBugs)        |
| `make seed`             | Popular dados de demonstração                 |
| `make doctor`           | Diagnóstico do ambiente                       |

---

## Resolução de Problemas

### Porta já em uso

```bash
# Identificar processo usando a porta (ex: 5432)
# Linux/Mac:
lsof -i :5432
# Windows:
netstat -ano | findstr :5432

# Encerrar o processo ou alterar a porta no .env
```

### Docker Compose não inicia

```bash
# Verificar status dos containers
docker compose -f infra/docker-compose.yml ps

# Ver logs de um serviço específico
docker compose -f infra/docker-compose.yml logs postgres

# Recriar do zero
make compose-reset
```

### Build falhando

```bash
# Limpar cache do Gradle
./gradlew clean

# Verificar versão do Java
java -version  # Deve ser 21+

# Executar diagnóstico
make doctor
```

### Testes de integração falhando

```bash
# Garantir que Docker Compose está ativo
make compose-up

# Aguardar serviços estarem healthy
docker compose -f infra/docker-compose.yml ps

# Re-executar migrations
make migrate
```

---

## Reset Completo do Ambiente

Se o ambiente estiver em estado inconsistente:

```bash
make compose-reset   # Remove volumes, recria containers, aplica migrations
make seed            # Re-popula dados de demonstração
make verify          # Valida que tudo funciona
```
