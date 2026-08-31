# Template de Tarefas — AtlasOps AI

Este diretório guarda contratos versionados de tarefa e grafos de execução para features futuras.

## Arquivos

- `feature-task-template.md`: contrato-base para uma feature.
- `feature-graph-template.md`: formato legível para o grafo de tarefas.

## Regras

- cada tarefa deve registrar objetivo, contexto, fora de escopo, critérios de aceite, módulos afetados, riscos, testes, comandos de validação, evidências e limite máximo de mudança;
- o grafo deve explicitar nós, dependências, paralelismo, dono, estado, entradas, saídas e gates humanos;
- o fluxo reduzido para bugs pequenos deve ser preferido quando a tarefa não justificar orquestração multiagente;
- os artefatos devem permanecer reproduzíveis a partir do commit e do identificador da execução.
