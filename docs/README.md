# AtlasOps AI — Documentação

> **Versão:** 0.1.0-SNAPSHOT  
> **Última atualização:** 2026-07-19  
> **Fase:** P0 (MVP Foundation) — ~75% completo

---

## 📋 Índice Rápido

### 🎯 Documentos Principais

| Documento                        | Descrição                                       | Para quem             |
| -------------------------------- | ----------------------------------------------- | --------------------- |
| [STATUS.md](./STATUS.md)         | **Status atual do projeto, módulos e métricas** | Todos                 |
| [ROADMAP.md](./ROADMAP.md)       | **Roadmap completo P0→P1→P2→P3**                | Product/Tech Leads    |
| [QUICKSTART.md](./QUICKSTART.md) | **Começar a desenvolver em 5 minutos**          | Novos desenvolvedores |

### 🏗️ Arquitetura

| Documento                                                    | Descrição                            |
| ------------------------------------------------------------ | ------------------------------------ |
| [architecture/OVERVIEW.md](./architecture/OVERVIEW.md)       | Visão geral da arquitetura hexagonal |
| [architecture/MODULES.md](./architecture/MODULES.md)         | Módulos backend e responsabilidades  |
| [architecture/DATA-STORES.md](./architecture/DATA-STORES.md) | Stores especializados (P2)           |
| [adr/](./adr/)                                               | Architecture Decision Records        |

### 🧪 Testes e Qualidade

| Documento                                                  | Descrição                                          |
| ---------------------------------------------------------- | -------------------------------------------------- |
| [testing/STRATEGY.md](./testing/STRATEGY.md)               | Estratégia de testes (Unit, PBT, Integration, E2E) |
| [testing/COVERAGE-REPORT.md](./testing/COVERAGE-REPORT.md) | Relatório de cobertura por módulo                  |
| [BUILD-PERFORMANCE.md](./BUILD-PERFORMANCE.md)             | Otimizações de build e comandos                    |

### 🚀 Operação e Deploy

| Documento                                                        | Descrição                     |
| ---------------------------------------------------------------- | ----------------------------- |
| [runbooks/LOCAL-ENVIRONMENT.md](./runbooks/LOCAL-ENVIRONMENT.md) | Rodar ambiente local          |
| [runbooks/TROUBLESHOOTING.md](./runbooks/TROUBLESHOOTING.md)     | Resolução de problemas comuns |
| [runbooks/MIGRATIONS.md](./runbooks/MIGRATIONS.md)               | Criar e executar migrations   |

### 📐 Especificações

| Documento                                                            | Descrição                   |
| -------------------------------------------------------------------- | --------------------------- |
| [specifications/API-CONTRACTS.md](./specifications/API-CONTRACTS.md) | Contratos de API REST       |
| [specifications/DOMAIN-MODEL.md](./specifications/DOMAIN-MODEL.md)   | Modelo de domínio unificado |
| [specifications/EVENTS.md](./specifications/EVENTS.md)               | Catálogo de domain events   |

---

## 🗂️ Estrutura de Diretórios

```
docs/
├── README.md                    # Este arquivo (índice principal)
├── STATUS.md                    # Status atual consolidado
├── ROADMAP.md                   # Roadmap completo (ex-PENDING-TASKS)
├── QUICKSTART.md                # Guia rápido para desenvolvedores
├── BUILD-PERFORMANCE.md         # Otimizações de build
│
├── architecture/                # Documentos de arquitetura
│   ├── OVERVIEW.md              # Visão geral hexagonal
│   ├── MODULES.md               # Descrição dos 19 módulos
│   ├── DATA-STORES.md           # Stores especializados (P2)
│   └── DEPENDENCY-RULES.md      # Regras de dependência ArchUnit
│
├── adr/                         # Architecture Decision Records
│   ├── ADR-001-template.md
│   ├── ADR-002-hexagonal-arch.md
│   ├── ADR-003-multi-tenant.md
│   └── ...
│
├── testing/                     # Estratégia e relatórios de teste
│   ├── STRATEGY.md              # Estratégia de testes
│   ├── COVERAGE-REPORT.md       # Cobertura atual
│   └── E2E-SCENARIOS.md         # Cenários Playwright
│
├── runbooks/                    # Guias operacionais
│   ├── LOCAL-ENVIRONMENT.md     # Setup local
│   ├── TROUBLESHOOTING.md       # Problemas comuns
│   ├── MIGRATIONS.md            # Migrations
│   └── OBSERVABILITY.md         # Métricas e logs
│
├── specifications/              # Especificações técnicas
│   ├── API-CONTRACTS.md         # Contratos REST
│   ├── DOMAIN-MODEL.md          # Modelo de domínio
│   ├── EVENTS.md                # Domain events
│   └── SECURITY.md              # Requisitos de segurança
│
├── task-plans/                  # Planos detalhados por fase
│   ├── P0-FOUNDATION.md         # MVP foundation
│   ├── P1-EXPERIENCE.md         # Search e integrações
│   ├── P2-SPECIALIZED-DATA.md   # Stores especializados
│   └── P3-HARDENING.md          # Release candidate
│
└── diagrams/                    # Diagramas C4, sequência, etc.
    ├── context.puml
    ├── containers.puml
    └── components/
```

---

## 🎯 Navegação por Objetivo

### Eu quero... entender o projeto

1. Leia [STATUS.md](./STATUS.md) para visão geral
2. Revise [architecture/OVERVIEW.md](./architecture/OVERVIEW.md)
3. Explore [ROADMAP.md](./ROADMAP.md) para próximos passos

### Eu quero... começar a desenvolver

1. Siga [QUICKSTART.md](./QUICKSTART.md)
2. Consulte [runbooks/LOCAL-ENVIRONMENT.md](./runbooks/LOCAL-ENVIRONMENT.md)
3. Leia [BUILD-PERFORMANCE.md](./BUILD-PERFORMANCE.md) para comandos

### Eu quero... contribuir com código

1. Revise [../AGENTS.md](../AGENTS.md) para convenções
2. Leia [testing/STRATEGY.md](./testing/STRATEGY.md)
3. Consulte [specifications/API-CONTRACTS.md](./specifications/API-CONTRACTS.md)

### Eu quero... resolver um problema

1. Consulte [runbooks/TROUBLESHOOTING.md](./runbooks/TROUBLESHOOTING.md)
2. Execute `make doctor` para diagnóstico
3. Revise logs em `infra/logs/`

### Eu quero... entender uma decisão de arquitetura

1. Navegue [adr/](./adr/) para ADRs
2. Consulte [architecture/DEPENDENCY-RULES.md](./architecture/DEPENDENCY-RULES.md)

---

## 📊 Status Geral (Resumo)

| Aspecto                 | Status                                   |
| ----------------------- | ---------------------------------------- |
| **Fase Atual**          | P0 (MVP Foundation) — 75%                |
| **Módulos Backend**     | 19 módulos — 14 completos, 5 parciais    |
| **Cobertura de Testes** | ~60% (meta: ≥75%)                        |
| **Build Performance**   | ~2-3min (70% melhoria)                   |
| **Infraestrutura**      | Docker Compose — 8 serviços ativos       |
| **CI/CD**               | GitHub Actions — testes unitários ✅     |
| **Frontend**            | React 19 + Next.js 15 — estrutura pronta |

---

## 🔗 Links Externos

- [AGENTS.md](../AGENTS.md) — Governança e papéis de agentes
- [.kiro/steering/](../.kiro/steering/) — Convenções técnicas
- [.kiro/specs/](../.kiro/specs/) — Specs SDD
- [Makefile](../Makefile) — Comandos disponíveis

---

## 📝 Notas de Atualização

- **2026-07-19:** Reorganização completa da estrutura de docs
- **2026-07-19:** Criação de STATUS.md e ROADMAP.md consolidados
- **2026-07-19:** Adição de QUICKSTART.md para onboarding
