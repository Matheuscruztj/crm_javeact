# Security Artifact Retention

## Artifacts

- Gitleaks report;
- Semgrep SARIF;
- Dependency-Check report;
- Trivy reports;
- SBOM;
- CodeQL results.

## Retention policy

- PR artifacts: 7 days.
- Nightly artifacts: 30 days.
- SBOM: 90 days.
- Critical findings: retain until remediated and re-scanned.

## Storage rule

- artifacts must be published even when the job fails;
- artifacts should use consistent names across CI and nightly.
