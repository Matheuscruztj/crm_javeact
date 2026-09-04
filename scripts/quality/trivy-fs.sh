#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

TRIVY_FS_SEVERITY="${TRIVY_FS_SEVERITY:-HIGH,CRITICAL}"
TRIVY_SKIP_DIRS="${TRIVY_SKIP_DIRS:-.git,.gradle,node_modules,frontend/node_modules,**/build,**/.next}"
TRIVY_FS_TARGETS="${TRIVY_FS_TARGETS:-.}"
TRIVY_DOCKER_IMAGE="${TRIVY_DOCKER_IMAGE:-aquasec/trivy:0.66.0}"

TRIVY_BIN_MODE=""
if command -v trivy >/dev/null 2>&1; then
  TRIVY_BIN_MODE="local"
elif command -v docker >/dev/null 2>&1; then
  TRIVY_BIN_MODE="docker"
else
  fail "trivy is required. Install Trivy or run the scan with Docker available."
fi

run_trivy() {
  if [[ "$TRIVY_BIN_MODE" == "local" ]]; then
    trivy "$@"
  else
    docker run --rm -v "$ROOT_DIR:/workspace" -w /workspace "$TRIVY_DOCKER_IMAGE" "$@"
  fi
}

log "Running Trivy filesystem scan"
run_quiet_or_fail "Trivy filesystem scan failed" run_in_root \
  run_trivy fs \
    --scanners vuln,secret,misconfig \
    --severity "$TRIVY_FS_SEVERITY" \
    --skip-dirs "$TRIVY_SKIP_DIRS" \
    --exit-code 1 \
    --no-progress \
    $TRIVY_FS_TARGETS

printf 'OK\n'
