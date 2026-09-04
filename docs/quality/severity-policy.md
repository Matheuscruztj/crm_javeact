# Policy de Bloqueio por Severidade

## Segurança

- `CRITICAL`: bloqueio imediato.
- `HIGH`: bloqueio em PR e nightly, exceto exceções aprovadas.
- `MEDIUM`: advisory por padrão, com revisão se houver exploração plausível.
- `LOW`: advisory.

## Exceções

- Toda exceção deve ter justificativa, owner e data de expiração.
- Exceção sem prazo expira automaticamente no próximo ciclo de revisão.
- Exceção não pode cobrir falha nova introduzida pela própria PR.

## Aplicação

- `Gitleaks`, `Semgrep`, `Trivy` e `Dependency-Check` seguem o critério acima.
- `actionlint` e `zizmor` são blocking quando o workflow muda.
- `OSV-Scanner` é blocking para vulnerabilidade nova introduzida na branch.
