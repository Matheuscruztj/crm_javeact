#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

changed_files="$(list_changed_files staged)"

if [[ -z "$changed_files" ]]; then
  changed_files="$(list_changed_files working-tree)"
fi

if [[ -z "$changed_files" ]]; then
  printf 'OK\n'
  exit 0
fi

if needs_backend_checks "$changed_files"; then
  run_quiet_or_fail "Backend pre-commit checks failed" run_in_root ./gradlew verifyFast
fi

if needs_frontend_checks "$changed_files"; then
  run_quiet_or_fail "Frontend pre-commit checks failed" run_in_root pnpm --dir frontend verify:fast
fi

printf 'OK\n'
