#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

if [[ -n "${NVD_API_KEY:-}" || -n "${OWASP_NVD_API_KEY:-}" ]]; then
  printf 'OK\n'
  exit 0
fi

if [[ "${CI:-false}" == "true" || "${GITHUB_ACTIONS:-false}" == "true" ]]; then
  fail "NVD_API_KEY is required in CI for Dependency-Check to run as a blocking gate"
fi

warn "NVD_API_KEY not set; Dependency-Check will be treated as incomplete in local execution"
printf 'OK\n'
