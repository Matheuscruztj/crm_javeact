#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

SEMGREP_IMAGE="${SEMGREP_IMAGE:-returntocorp/semgrep:1.84.0}"
SEMGREP_CONFIG="${SEMGREP_CONFIG:-auto}"
SEMGREP_TARGETS="${SEMGREP_TARGETS:-backend frontend}"
SEMGREP_SEVERITY="${SEMGREP_SEVERITY:-ERROR}"
SEMGREP_REPORT_DIR="${SEMGREP_REPORT_DIR:-build/reports/semgrep}"

if ! command -v docker >/dev/null 2>&1; then
  fail "docker is required to run Semgrep locally."
fi

run_in_root mkdir -p "$SEMGREP_REPORT_DIR"

log "Running Semgrep SAST scan"
run_quiet_or_fail "Semgrep scan failed" run_in_root docker run --rm \
  -v "$ROOT_DIR:/src" \
  -w /src \
  "$SEMGREP_IMAGE" \
  semgrep scan \
    --config "$SEMGREP_CONFIG" \
    --severity "$SEMGREP_SEVERITY" \
    --exclude .git \
    --exclude .gradle \
    --exclude node_modules \
    --exclude frontend/node_modules \
    --exclude '**/build' \
    --exclude '**/.next' \
    --exclude '**/target' \
    --sarif \
    --output "$SEMGREP_REPORT_DIR/semgrep.sarif" \
    --error \
    --metrics=off \
    $SEMGREP_TARGETS

printf 'OK\n'
