# ADR-001: Adoção de Monorepo com Gradle Multi-Project e Arquitetura Hexagonal

**Status:** Accepted  
**Data:** 2025-01-15  
**Autores:** Equipe AtlasOps AI

---

## Contexto

O projeto AtlasOps AI é um CRM inteligente multi-tenant com capacidades de análise de documentos via IA local. O sistema possui múltiplos domínios de negócio (autenticação, tenants, clientes, documentos, pipeline de IA, workflows, auditoria) que precisam ser organizados de forma modular, com isolamento claro de responsabilidades e facilidade de evolução independente.

As opções consideradas foram:

1. **Multi-repo** — um repositório por módulo/serviço
2. **Monorepo com Maven** — repositório único com build Maven
3. **Monorepo com Gradle multi-project** — repositório único com Gradle Kotlin DSL
4. **Monolito sem modularização** — código-fonte em projeto único sem separação de módulos

Além disso, precisava-se decidir o padrão arquitetural interno de cada módulo para garantir isolamento de domínio, testabilidade e substituibilidade de infraestrutura.

---

## Decisão

Adotamos **monorepo com Gradle multi-project (Kotlin DSL)** como estrutura de repositório, e **arquitetura hexagonal (ports and adapters)** como padrão interno de cada módulo backend.

### Monorepo Gradle

- Um único repositório contém backend, frontend, infraestrutura e documentação
- Cada módulo de negócio é um subprojeto Gradle independente
- `settings.gradle.kts` declara todos os subprojetos via `include()`
- `gradle.properties` centraliza versões de dependências compartilhadas
- Módulo `app-boot` agrega todos os módulos e é o ponto de entrada Spring Boot

### Arquitetura Hexagonal

Cada módulo segue a estrutura:

```
{modulo}/
├── domain/        → Entidades, Value Objects, Domain Events, Ports
├── application/   → Use Cases, Commands, Queries, DTOs
├── infrastructure/→ Adapters (JPA, Redis, S3, AI)
├── presentation/  → Controllers REST, Event Consumers
```

Regras de dependência:

- `domain/` não importa `infrastructure/` nem `presentation/`
- `application/` não importa `infrastructure/` nem `presentation/`
- `infrastructure/` implementa ports definidos em `domain/`
- `presentation/` depende apenas de `application/`

---

## Consequências

### Positivas

- **Refatoração de fronteiras facilitada**: mover código entre módulos sem friction de múltiplos repos
- **Build incremental**: Gradle reconstrói apenas módulos afetados por mudanças
- **Consistência**: versões de dependências centralizadas, formatação e lint aplicados globalmente
- **Isolamento de domínio**: ArchUnit valida automaticamente as regras de dependência entre camadas
- **Testabilidade**: domínio testável sem frameworks, adapters substituíveis em testes
- **Evolução independente**: módulos podem ser extraídos para microsserviços no futuro se necessário
- **Visibilidade**: todo o código em um lugar, facilitando code review e entendimento do sistema

### Negativas

- **Tempo de clone inicial**: repositório maior com o tempo
- **Complexidade de build**: configuração Gradle mais elaborada que um projeto simples
- **Risco de acoplamento**: sem disciplina, módulos podem acumular dependências desnecessárias (mitigado por ArchUnit)
- **CI mais complexa**: pipelines precisam detectar quais módulos foram afetados (mitigado por Gradle build avoidance)

---

## Alternativas Consideradas

### Multi-repo

- **Rejeitado porque:** alto friction para refatoração de fronteiras entre módulos, dificuldade de manter versões consistentes, complexidade de CI/CD distribuído, overhead de manutenção de múltiplos pipelines

### Maven

- **Rejeitado porque:** builds mais lentos que Gradle, menos flexibilidade no Kotlin DSL, build avoidance inferior, ecossistema de plugins mais limitado para necessidades do projeto

### Monolito sem modularização

- **Rejeitado porque:** dificulta isolamento de domínio, torna testes de integração mais lentos, não permite evolução independente de módulos, aumenta risco de acoplamento acidental

---

## Referências

- [Gradle Multi-Project Builds](https://docs.gradle.org/current/userguide/multi_project_builds.html)
- [Hexagonal Architecture — Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [ArchUnit — Unit test your Java architecture](https://www.archunit.org/)
- Design Document: `.kiro/specs/monorepo-sdd-harness/design.md`
