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

if command -v python3 >/dev/null 2>&1; then
  if python3 -m zizmor --help >/dev/null 2>&1; then
    log "Running zizmor via python"
    run_quiet_or_fail "zizmor failed" run_in_root python3 -m zizmor --format sarif .github/workflows
    printf 'OK\n'
    exit 0
  fi

  log "Installing zizmor via pip"
  run_quiet_or_fail "zizmor installation failed" python3 -m pip install --user "zizmor==${ZIZMOR_VERSION}"

  log "Running zizmor after pip install"
  run_quiet_or_fail "zizmor failed" run_in_root python3 -m zizmor --format sarif .github/workflows
  printf 'OK\n'
  exit 0
fi

if ! command -v docker >/dev/null 2>&1; then
  fail "zizmor is required. Install zizmor locally or run the scan with Python 3/pip or Docker available."
fi

log "Running zizmor via Docker"
if ! run_quiet_or_fail "zizmor failed" run_in_root docker run --rm \
  -v "$ROOT_DIR:/repo" \
  -w /repo \
  "ghcr.io/trailofbits/zizmor:${ZIZMOR_VERSION}" \
  --format sarif \
  .github/workflows; then
  fail "zizmor Docker image could not be pulled; install zizmor locally or make ghcr.io/trailofbits/zizmor:${ZIZMOR_VERSION} reachable."
fi

printf 'OK\n'
