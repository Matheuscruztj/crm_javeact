#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

GITLEAKS_IMAGE="${GITLEAKS_IMAGE:-zricethezav/gitleaks:latest}"
GITLEAKS_CONFIG="${GITLEAKS_CONFIG:-.gitleaks.toml}"

if command -v gitleaks >/dev/null 2>&1; then
  log "Running Gitleaks"
  run_quiet_or_fail "Gitleaks scan failed" run_in_root gitleaks detect \
    --no-banner \
    --redact \
    --config "$GITLEAKS_CONFIG" \
    --source "$ROOT_DIR"
  printf 'OK\n'
  exit 0
fi

if ! command -v docker >/dev/null 2>&1; then
  fail "gitleaks is required. Install Gitleaks or run the scan with Docker available."
fi

log "Running Gitleaks via Docker"
run_quiet_or_fail "Gitleaks scan failed" run_in_root docker run --rm \
  -v "$ROOT_DIR:/repo" \
  -w /repo \
  "$GITLEAKS_IMAGE" \
  detect \
    --no-banner \
    --redact \
    --config /repo/.gitleaks.toml \
    --source /repo

printf 'OK\n'
