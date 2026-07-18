# Convenções Git — AtlasOps AI

> **Versão:** 1.0  
> **Última atualização:** 2025-01-20

---

## Conventional Commits

### Formato obrigatório

```
<type>(<scope>): <descrição curta>

[corpo opcional]

[footer opcional]
```

### Types permitidos

| Type       | Uso                                      | Exemplo                                                    |
| ---------- | ---------------------------------------- | ---------------------------------------------------------- |
| `feat`     | Nova funcionalidade                      | `feat(customers): add bulk import endpoint`                |
| `fix`      | Correção de bug                          | `fix(auth): resolve token expiration edge case`            |
| `refactor` | Refatoração sem mudança de comportamento | `refactor(ai): extract chunker to separate class`          |
| `test`     | Adição ou correção de testes             | `test(shared-kernel): add PBT for feature-name validation` |
| `docs`     | Documentação                             | `docs: update AGENTS.md with new procedures`               |
| `chore`    | Manutenção (deps, build, CI)             | `chore(deps): bump Spring Boot to 3.2.5`                   |
| `style`    | Formatação (sem mudança de lógica)       | `style: apply Spotless formatting`                         |
| `perf`     | Melhoria de performance                  | `perf(ai): optimize vector search query`                   |
| `ci`       | Mudanças em CI/CD                        | `ci: add integration test step to pipeline`                |
| `build`    | Mudanças no build system                 | `build(gradle): add OWASP dependency-check plugin`         |

### Scopes comuns

- Módulos: `auth`, `tenants`, `users`, `customers`, `documents`, `requests`, `pipeline`, `tasks`, `workflows`, `ai`, `analytics`, `audit`, `app-boot`, `shared-kernel`
- Infraestrutura: `docker`, `gradle`, `infra`, `monitoring`
- Documentação: `docs`, `adr`, `steering`
- Tooling: `deps`, `ci`, `makefile`

### Regras

- Descrição em inglês, lowercase, sem ponto final
- Máximo **72 caracteres** na primeira linha
- Corpo separado por linha em branco, wrap em 100 caracteres
- Breaking changes: adicionar `!` após type/scope e `BREAKING CHANGE:` no footer

### Exemplos

```
feat(customers): add pagination to list endpoint

Implements cursor-based pagination with configurable page size (max 100).
Default sort is by createdAt descending.

Spec: customer-management
Task: 3.2

BREAKING CHANGE: response format changed from array to paginated envelope
```

```
fix(ai): handle null text in document ingestion

Documents without extractable text now return FAILED status instead of
throwing NullPointerException.

Fixes: ATLAS-128
```

```
test(shared-kernel): add property test for correlation ID generation

Property 3: Correlation ID Propagation
Validates: Requirements 3.11, 11.4, 11.5
```

---

## Naming de Branches

### Formato

```
<tipo>/<issue-id>-<descricao-curta>
```

### Tipos de branch

| Prefixo     | Uso                                           |
| ----------- | --------------------------------------------- |
| `feature/`  | Nova funcionalidade                           |
| `fix/`      | Correção de bug                               |
| `refactor/` | Refatoração                                   |
| `chore/`    | Manutenção, deps, tooling                     |
| `docs/`     | Documentação                                  |
| `sandbox/`  | Branches de sandbox de agentes (auto-geradas) |
| `release/`  | Preparação de release                         |
| `hotfix/`   | Correção urgente em produção                  |

### Regras

- Descrição em kebab-case
- Máximo **60 caracteres** no total
- Sempre incluir issue ID quando existir
- Não usar caracteres especiais além de `-` e `/`

### Exemplos

```
feature/ATLAS-42-customer-bulk-import
fix/ATLAS-128-null-text-ingestion
refactor/ATLAS-200-extract-chunker
chore/ATLAS-300-bump-spring-boot
docs/ATLAS-50-update-agents-md
sandbox/ATLAS-42-agent-A2
```

### Branches protegidas

- `main` — Código em produção
- `release/*` — Preparação de release
- `hotfix/*` — Correções urgentes

**Nenhum push direto** nestas branches. Apenas merge via PR com aprovação.

---

## Tamanho de PR

### Limites recomendados

| Métrica            | Ideal | Máximo |
| ------------------ | ----- | ------ |
| Arquivos alterados | ≤ 10  | 20     |
| Linhas adicionadas | ≤ 300 | 500    |
| Linhas removidas   | ≤ 200 | 400    |
| Commits            | ≤ 5   | 10     |

### Quando quebrar em múltiplos PRs

- Mais de **500 linhas** de adição → dividir por camada ou funcionalidade
- Mais de **20 arquivos** → dividir por módulo
- Mudança de interface pública + implementação → PR separados
- Migration + código → PRs separados (migration primeiro)

### Exceções aceitas

- Adição de novo módulo completo (estrutura inicial)
- Refatoração automatizada (renaming, formatação)
- Geração de código (OpenAPI, migrations auto-geradas)

---

## Checklist de PR

Todo PR deve incluir na descrição:

```markdown
## Descrição

<!-- O que foi feito e por quê -->

## Spec/Task

- Spec: {feature-name}
- Task: {número-da-task}

## Tipo de mudança

- [ ] Nova funcionalidade (feat)
- [ ] Correção de bug (fix)
- [ ] Refatoração (refactor)
- [ ] Testes (test)
- [ ] Documentação (docs)
- [ ] Manutenção (chore)

## Checklist

- [ ] Código segue convenções do projeto (steering files)
- [ ] Testes unitários escritos e passando
- [ ] Testes de propriedade escritos (quando aplicável)
- [ ] `make verify` passa sem erros
- [ ] Sem warnings de compilação
- [ ] Documentação atualizada (se interfaces alteradas)
- [ ] Não introduz dependências com vulnerabilidades conhecidas
- [ ] Não altera arquivos protegidos sem justificativa

## Screenshots/Evidências

<!-- Se aplicável: output de testes, screenshots, logs -->

## Riscos/Observações

<!-- Algo que o reviewer deve prestar atenção especial -->
```

---

## Fluxo de Merge

### Estratégia: Squash and Merge

- Cada PR resulta em **1 commit** na branch destino
- Mensagem do squash segue formato Conventional Commits
- Histórico de commits individuais preservado no PR (não na main)

### Rebase antes do merge

- Branch deve estar atualizada com `main` antes do merge
- Conflitos resolvidos pelo autor do PR
- Nunca usar `--force` em branches compartilhadas

---

## Commits em Sandbox

Branches de sandbox (`sandbox/*`) têm regras relaxadas:

- Commits intermediários podem ter mensagens simples (WIP)
- Ao final, fazer squash antes de abrir PR
- A mensagem final do PR deve seguir Conventional Commits

---

## Tags e Releases

### Versionamento: Semantic Versioning (SemVer)

```
v{MAJOR}.{MINOR}.{PATCH}
```

- **MAJOR:** Breaking changes em APIs públicas
- **MINOR:** Nova funcionalidade backward-compatible
- **PATCH:** Correção de bug backward-compatible

### Formato de tag

```
v1.0.0
v1.1.0-rc.1    (release candidate)
v1.1.0-beta.1  (beta)
```

---

## Proibições

- ❌ `git push --force` em branches protegidas
- ❌ Commits com mensagem genérica (`fix`, `update`, `changes`, `wip`) na main
- ❌ Merge sem aprovação de pelo menos 1 reviewer
- ❌ Commits contendo segredos, senhas ou tokens
- ❌ Commits com arquivos binários grandes (>1MB) sem Git LFS
- ❌ Rewrite de história em branches públicas/compartilhadas
