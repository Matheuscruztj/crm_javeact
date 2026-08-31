# AtlasOps AI - Documentação

Este diretório contém somente documentação de referência ativa. O estado de implementação é mantido em uma única fonte para evitar divergências.

## Comece aqui

| Documento | Uso |
| --- | --- |
| [00-current-status.md](./00-current-status.md) | Status consolidado: concluído, em andamento e pendente. |
| [QUICKSTART.md](./QUICKSTART.md) | Preparação do ambiente local. |
| [ROADMAP.md](./ROADMAP.md) | Prioridades de produto e entregas futuras. |
| [PENDING-ACTIVITIES.md](./PENDING-ACTIVITIES.md) | Backlog acionável por prioridade. |
| [CODING-PENDING-ACTIVITIES.md](./CODING-PENDING-ACTIVITIES.md) | Pendências de codificação, arquitetura, profiling e capacidade. |
| [CONTRIBUTING.md](./CONTRIBUTING.md) | Fluxo de contribuição. |

## Arquitetura e especificações

| Diretório ou documento | Uso |
| --- | --- |
| [architecture/](./architecture/) | Visão de arquitetura e modelo de dados. |
| [adr/](./adr/) | Decisões arquiteturais registradas. |
| [diagrams/](./diagrams/) | Diagramas C4 e de fluxo. |
| [specifications/](./specifications/) | Escopo de produto e especificação técnica. |
| [asyncapi.yaml](./asyncapi.yaml) | Contrato dos eventos assíncronos. |

## Qualidade e operação

| Diretório ou documento | Uso |
| --- | --- |
| [testing/](./testing/) | Estratégia de testes e CI/CD. |
| [ROADMAP-QUALITY-ENFORCEMENT-WAVES.md](./ROADMAP-QUALITY-ENFORCEMENT-WAVES.md) | Pendências de qualidade por wave. |
| [runbooks/](./runbooks/) | Bootstrap, operação, recuperação e resiliência. |
| [runbooks/PROFILING-AND-CAPACITY.md](./runbooks/PROFILING-AND-CAPACITY.md) | Profiling JVM, k6 em ambiente enxuto e correlação com métricas. |
| [security/](./security/) | Modelo de ameaças. |
| [BUILD-PERFORMANCE.md](./BUILD-PERFORMANCE.md) | Evidências e comandos de desempenho. |

## Convenção de manutenção

- Atualize [00-current-status.md](./00-current-status.md) quando uma capacidade mudar de estado.
- Mantenha atividades abertas em [PENDING-ACTIVITIES.md](./PENDING-ACTIVITIES.md), sem duplicar snapshots de status.
- Preserve ADRs, especificações e runbooks enquanto representarem decisões ou procedimentos em vigor.
- Remova notas de release, relatórios históricos e roadmaps encerrados quando não forem referência operacional.
