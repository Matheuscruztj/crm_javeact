#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ACTIONLINT_IMAGE="${ACTIONLINT_IMAGE:-rhysd/actionlint:latest}"

if command -v actionlint >/dev/null 2>&1; then
  log "Running actionlint"
  run_quiet_or_fail "actionlint failed" run_in_root actionlint
  printf 'OK\n'
  exit 0
fi

if ! command -v docker >/dev/null 2>&1; then
  fail "actionlint is required. Install actionlint or run the scan with Docker available."
fi

log "Running actionlint via Docker"
run_quiet_or_fail "actionlint failed" run_in_root docker run --rm \
  -v "$ROOT_DIR:/repo" \
  -w /repo \
  "$ACTIONLINT_IMAGE" \
  -color

printf 'OK\n'
