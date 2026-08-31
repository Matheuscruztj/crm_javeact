#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

TRIVY_FS_SEVERITY="${TRIVY_FS_SEVERITY:-HIGH,CRITICAL}"
TRIVY_IMAGE_SEVERITY="${TRIVY_IMAGE_SEVERITY:-HIGH,CRITICAL}"
TRIVY_SKIP_DIRS="${TRIVY_SKIP_DIRS:-.git,.gradle,node_modules,frontend/node_modules,**/build,**/.next}"
TRIVY_FS_TARGETS="${TRIVY_FS_TARGETS:-.}"
TRIVY_IMAGE_REFS="${TRIVY_IMAGE_REFS:-atlasops/backend-api:latest atlasops/worker:latest}"
TRIVY_DOCKER_IMAGE="${TRIVY_DOCKER_IMAGE:-aquasec/trivy:0.66.0}"

TRIVY_BIN_MODE=""
if command -v trivy >/dev/null 2>&1; then
  TRIVY_BIN_MODE="local"
elif command -v docker >/dev/null 2>&1; then
  TRIVY_BIN_MODE="docker"
else
  fail "trivy is required. Install Trivy or run the security scan with Docker available."
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

if [[ "$TRIVY_BIN_MODE" == "local" ]] && command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
  for image_ref in $TRIVY_IMAGE_REFS; do
    if docker image inspect "$image_ref" >/dev/null 2>&1; then
      log "Running Trivy image scan for $image_ref"
      run_quiet_or_fail "Trivy image scan failed for $image_ref" run_in_root \
        run_trivy image \
          --scanners vuln,secret \
          --severity "$TRIVY_IMAGE_SEVERITY" \
          --exit-code 1 \
          --no-progress \
          "$image_ref"
    else
      warn "Skipping image scan for $image_ref because the image is not present locally"
    fi
  done
else
  warn "Docker image scanning was skipped"
fi

printf 'OK\n'
