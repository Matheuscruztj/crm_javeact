# AtlasOps AI — Quickstart Guide

> **Tempo estimado:** 10-15 minutos  
> **Pré-requisitos:** Java 21+, Docker 24+, Git

---

## 🚀 Setup em 3 Passos

### 1️⃣ Clone e Configure

```bash
# Clonar repositório
git clone <url-do-repositorio>
cd atlasops-ai

# Copiar variáveis de ambiente
cp .env.example .env

# (Opcional) Editar .env se necessário
# Valores padrão já funcionam para desenvolvimento local
```

### 2️⃣ Bootstrap Automático

```bash
# Este comando faz tudo automaticamente:
# - Verifica Java 21+ e Docker 24+
# - Valida portas disponíveis (5432, 6379, 9000, etc.)
# - Instala dependências
# - Inicia Docker Compose (PostgreSQL, Redis, MinIO, Ollama, etc.)
# - Executa migrations
# - Popula dados demo

make bootstrap
```

**Aguarde ~2-3 minutos** enquanto os containers sobem e as migrations rodam.

### 3️⃣ Verificar Instalação

```bash
# Executa quality gates básicos (compile + fast tests)
make verify-fast
```

Se tudo passar ✅, você está pronto!

---

## 🎯 Próximos Passos

### Rodar o Backend

```bash
# Opção 1: Via Gradle (recomendado para desenvolvimento)
./gradlew :backend:app-boot:bootRun

# Opção 2: Via Docker Compose (simula produção)
make compose-core
```

Backend estará disponível em: `http://localhost:8080`

**Endpoints úteis:**

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator Health: `http://localhost:8080/actuator/health`
- API Base: `http://localhost:8080/api/v1/`

### Rodar o Worker (Processamento Assíncrono)

```bash
./gradlew :backend:worker:bootRun
```

### Rodar o Frontend (quando disponível)

```bash
cd frontend
pnpm install
pnpm dev
```

Frontend estará em: `http://localhost:3000`

---

## 🧪 Testes

### Testes Rápidos (durante desenvolvimento)

```bash
# Apenas testes unitários rápidos (~30s)
make test-fast

# Antes de commit
make verify-fast
```

### Testes Completos

```bash
# Todos os unit tests (~1-2min)
make test-unit

# Property-based tests (~3-5min)
make test-property

# Integration tests (~5-10min, requer Docker)
make test-integration

# Tudo (~15min)
make verify-full
```

---

## 🐳 Infraestrutura Local

### Serviços Disponíveis

| Serviço    | URL                      | Credenciais             |
| ---------- | ------------------------ | ----------------------- |
| PostgreSQL | `localhost:5432`         | `atlasops/atlasops`     |
| Redis      | `localhost:6379`         | (sem senha)             |
| MinIO      | `http://localhost:9001`  | `minioadmin/minioadmin` |
| Ollama     | `http://localhost:11434` | (sem auth)              |
| Prometheus | `http://localhost:9090`  | (sem auth)              |
| Grafana    | `http://localhost:3000`  | `admin/admin`           |
| MailHog    | `http://localhost:8025`  | (mock SMTP)             |

### Comandos de Infraestrutura

```bash
# Iniciar todos os serviços
make compose-up

# Parar (mantém dados)
make compose-down

# Reconstruir do zero (remove volumes)
make compose-reset

# Verificar status
docker compose ps
```

---

## 📂 Estrutura do Projeto

```
atlasops-ai/
├── backend/              # 19 módulos Java (Gradle multi-project)
│   ├── shared-kernel/    # Tipos base, ports compartilhados
│   ├── auth/             # Autenticação JWT
│   ├── tenants/          # Multi-tenancy
│   ├── users/            # Gestão de usuários
│   ├── customers/        # CRUD clientes
│   ├── documents/        # Upload e storage (MinIO)
│   ├── requests/         # Solicitações de serviço
│   ├── approvals/        # Fluxo de aprovação
│   ├── ai/               # RAG pipeline (Ollama + pgvector)
│   ├── app-boot/         # Ponto de entrada Spring Boot
│   └── worker/           # Processo assíncrono
│
├── frontend/             # React 19 + Next.js 15
│   └── (em desenvolvimento)
│
├── infra/                # Docker Compose, scripts, monitoring
├── docs/                 # Documentação técnica
├── .kiro/                # Specs SDD, steering, skills
├── Makefile              # Comandos padronizados
└── AGENTS.md             # Governança e convenções
```

---

## 🎓 Aprender Mais

### Documentação Essencial

1. [STATUS.md](./STATUS.md) — Status atual do projeto
2. [ROADMAP.md](./ROADMAP.md) — Roadmap e tarefas pendentes
3. [AGENTS.md](../AGENTS.md) — Governança e convenções técnicas
4. [BUILD-PERFORMANCE.md](./BUILD-PERFORMANCE.md) — Otimizações de build

### Convenções (Steering Files)

- [.kiro/steering/java-conventions.md](../.kiro/steering/java-conventions.md)
- [.kiro/steering/testing-patterns.md](../.kiro/steering/testing-patterns.md)
- [.kiro/steering/api-conventions.md](../.kiro/steering/api-conventions.md)
- [.kiro/steering/git-conventions.md](../.kiro/steering/git-conventions.md)

---

## 🆘 Problemas Comuns

### Port already in use

```bash
# Verificar quem está usando a porta
netstat -ano | findstr :5432

# Parar serviços conflitantes ou alterar porta no .env
```

### Docker não inicia

```bash
# Verificar se Docker está rodando
docker ps

# Se não estiver, iniciar Docker Desktop manualmente
```

### Build falha com "OutOfMemoryError"

```bash
# Aumentar memória temporariamente
GRADLE_OPTS="-Xmx6g" make build
```

### Testes falhando

```bash
# Limpar caches
make clean-cache
make clean

# Reconstruir
make build
```

### Dependências desatualizadas

```bash
# Atualizar dependências do Gradle
./gradlew --refresh-dependencies

# Recompilar
make build
```

---

## 🛠️ Ferramentas Recomendadas

### IDEs

- **IntelliJ IDEA Ultimate** (recomendado) — Melhor suporte Spring Boot
- **VS Code** — Instalar extensões: Java Extension Pack, Spring Boot Extension Pack

### Plugins IntelliJ

- Lombok
- JPA Buddy
- Docker
- Database Tools

### Ferramentas CLI

```bash
# HTTPie (testar APIs)
http POST localhost:8080/api/v1/auth/login email=admin@atlasops.test password=Admin123!

# jq (parse JSON)
curl localhost:8080/actuator/health | jq

# Docker Desktop (interface gráfica)
# Facilita gerenciamento de containers
```

---

## ✅ Checklist de Primeiro Commit

Antes de fazer seu primeiro commit:

- [ ] `make verify-fast` passa sem erros
- [ ] Seguiu convenções de código ([java-conventions.md](../.kiro/steering/java-conventions.md))
- [ ] Testes unitários escritos (se aplicável)
- [ ] Commit message segue Conventional Commits (`feat:`, `fix:`, etc.)
- [ ] Branch segue padrão (`feature/ATLAS-XX-descricao`)

```bash
# Criar branch
git checkout -b feature/ATLAS-42-minha-feature

# Commit seguindo Conventional Commits
git commit -m "feat(customers): add bulk import validation"

# Push
git push -u origin feature/ATLAS-42-minha-feature

# Abrir PR no GitHub
```

---

## 🎉 Pronto!

Você está pronto para contribuir com o AtlasOps AI!

**Próximas leituras recomendadas:**

1. [ROADMAP.md](./ROADMAP.md) — Ver tarefas disponíveis
2. [architecture/MODULES.md](./architecture/MODULES.md) — Entender módulos
3. [testing/STRATEGY.md](./testing/STRATEGY.md) — Estratégia de testes

**Dúvidas?**

- Consulte [runbooks/TROUBLESHOOTING.md](./runbooks/TROUBLESHOOTING.md)
- Execute `make doctor` para diagnóstico
- Revise issues no GitHub
