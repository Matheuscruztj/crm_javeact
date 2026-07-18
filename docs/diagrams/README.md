# Diagramas — AtlasOps AI

Este diretório contém diagramas de arquitetura e fluxo do projeto.

## Diagramas Disponíveis

Os diagramas estão documentados inline no design document (`.kiro/specs/monorepo-sdd-harness/design.md`) usando Mermaid:

- **Visão Geral do Monorepo** — Estrutura de diretórios e módulos
- **Arquitetura Hexagonal por Módulo** — Camadas e dependências internas
- **Fluxo de Orquestração de Agentes** — Sequência Issue → Planner → Implementer → Review → Merge
- **Stack de Observabilidade** — Backend → Loki/Prometheus → Grafana

## Convenções

- Formato preferido: **Mermaid** (renderizado diretamente em Markdown)
- Para diagramas complexos que requerem ferramentas externas: exportar PNG/SVG neste diretório
- Naming: `{tipo}-{assunto}.{ext}` (ex: `sequence-agent-orchestration.png`, `component-ai-module.svg`)
- Todo diagrama deve ter data de criação/atualização no commit message

## Ferramentas Recomendadas

- [Mermaid Live Editor](https://mermaid.live) — Para diagramas Mermaid
- [draw.io / diagrams.net](https://app.diagrams.net) — Para diagramas mais complexos
- [PlantUML](https://plantuml.com) — Alternativa text-based para UML
