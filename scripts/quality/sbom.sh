#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

SBOM_OUTPUT_DIR="${SBOM_OUTPUT_DIR:-build/reports/sbom}"
SYFT_IMAGE="${SYFT_IMAGE:-anchore/syft:latest}"

run_in_root mkdir -p "$SBOM_OUTPUT_DIR"

if command -v syft >/dev/null 2>&1; then
  log "Generating SBOM with Syft"
  run_quiet_or_fail "Syft SBOM generation failed" run_in_root syft . --output cyclonedx-json="$SBOM_OUTPUT_DIR/sbom.json"
  printf 'OK\n'
  exit 0
fi

if ! command -v docker >/dev/null 2>&1; then
  fail "syft is required. Install Syft or run the generation with Docker available."
fi

log "Generating SBOM with Syft via Docker"
run_quiet_or_fail "Syft SBOM generation failed" run_in_root docker run --rm \
  -v "$ROOT_DIR:/src" \
  -w /src \
  "$SYFT_IMAGE" \
  . --output cyclonedx-json="$SBOM_OUTPUT_DIR/sbom.json"

printf 'OK\n'
