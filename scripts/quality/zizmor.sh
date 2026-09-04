#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ZIZMOR_VERSION="${ZIZMOR_VERSION:-1.4.1}"

if command -v zizmor >/dev/null 2>&1; then
  log "Running zizmor"
  run_quiet_or_fail "zizmor failed" run_in_root zizmor --format sarif .github/workflows
  printf 'OK\n'
  exit 0
fi

if ! command -v docker >/dev/null 2>&1; then
  fail "zizmor is required. Install zizmor or run the scan with Docker available."
fi

log "Running zizmor via Docker"
run_quiet_or_fail "zizmor failed" run_in_root docker run --rm \
  -v "$ROOT_DIR:/repo" \
  -w /repo \
  "ghcr.io/trailofbits/zizmor:${ZIZMOR_VERSION}" \
  --format sarif \
  .github/workflows

printf 'OK\n'
