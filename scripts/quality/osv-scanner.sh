#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

OSV_SCANNER_IMAGE="${OSV_SCANNER_IMAGE:-ghcr.io/google/osv-scanner:latest}"

supported_manifests=()
for candidate in \
  "$ROOT_DIR/frontend/pnpm-lock.yaml" \
  "$ROOT_DIR/frontend/package-lock.json" \
  "$ROOT_DIR/frontend/yarn.lock" \
  "$ROOT_DIR/backend/gradle.lockfile" \
  "$ROOT_DIR/gradle.lockfile"; do
  if [[ -f "$candidate" ]]; then
    supported_manifests+=("$candidate")
  fi
done

relative_manifests=()
for manifest in "${supported_manifests[@]}"; do
  relative_manifests+=("${manifest#"$ROOT_DIR/"}")
done

if [[ "${#supported_manifests[@]}" -eq 0 ]]; then
  warn "No OSV-supported lockfiles found; skipping OSV-Scanner"
  printf 'OK\n'
  exit 0
fi

if command -v osv-scanner >/dev/null 2>&1; then
  log "Running OSV-Scanner"
  run_quiet_or_fail "OSV-Scanner failed" run_in_root osv-scanner scan "${relative_manifests[@]}"
  printf 'OK\n'
  exit 0
fi

if ! command -v docker >/dev/null 2>&1; then
  fail "osv-scanner is required. Install OSV-Scanner or run the scan with Docker available."
fi

log "Running OSV-Scanner via Docker"
run_quiet_or_fail "OSV-Scanner failed" run_in_root docker run --rm \
  -v "$ROOT_DIR:/src" \
  -w /src \
  "$OSV_SCANNER_IMAGE" \
  scan "${relative_manifests[@]}"

printf 'OK\n'
