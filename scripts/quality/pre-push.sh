#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

changed_files="$(list_changed_files working-tree)"

run_quiet_or_fail "Backend fast verification failed" run_in_root ./gradlew verifyFast
run_quiet_or_fail "Backend static analysis failed" run_in_root ./gradlew checkstyleMain spotbugsMain
run_quiet_or_fail "Backend property tests failed" run_in_root ./gradlew testProperty
run_quiet_or_fail "Backend architecture tests failed" run_in_root ./gradlew :backend:app-boot:architectureTest
run_quiet_or_fail "Frontend verification failed" run_in_root pnpm --dir frontend verify:fast
run_quiet_or_fail "Frontend architecture checks failed" run_in_root pnpm --dir frontend architecture:check
run_quiet_or_fail "Frontend unit coverage failed" run_in_root pnpm --dir frontend test:unit:coverage

if [[ -z "$changed_files" ]] || needs_contract_checks "$changed_files"; then
  run_quiet_or_fail "Contract verification failed" run_in_root make verify-contracts
fi

if [[ -z "$changed_files" ]] || needs_resilience_checks "$changed_files"; then
  run_quiet_or_fail "Resilience checks failed" run_in_root make test-resilience-minio
fi

if [[ -z "$changed_files" ]] || needs_security_checks "$changed_files"; then
  if [[ -z "$changed_files" ]] || needs_sast_checks "$changed_files"; then
    run_quiet_or_fail "SAST scan failed" run_in_root make verify-sast
  fi

  if [[ -z "$changed_files" ]] || needs_dast_checks "$changed_files"; then
    run_quiet_or_fail "DAST scan failed" run_in_root make verify-dast
  fi

  run_quiet_or_fail "Filesystem and image security scan failed" run_in_root ./scripts/quality/trivy.sh
fi

printf 'OK\n'
