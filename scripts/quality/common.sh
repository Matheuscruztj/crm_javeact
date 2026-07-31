#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

log() {
  printf '\n==> %s\n' "$1"
}

warn() {
  printf 'WARNING: %s\n' "$1"
}

fail() {
  printf 'ERROR: %s\n' "$1" >&2
  exit 1
}

run_or_fail() {
  local description="$1"
  shift

  if ! "$@"; then
    fail "$description"
  fi
}

run_quiet_or_fail() {
  local description="$1"
  shift

  local output_file
  output_file="$(mktemp)"

  if ! "$@" >"$output_file" 2>&1; then
    cat "$output_file" >&2
    rm -f "$output_file"
    fail "$description"
  fi

  rm -f "$output_file"
}

repo_root() {
  printf '%s\n' "$ROOT_DIR"
}

run_in_root() {
  (cd "$ROOT_DIR" && "$@")
}

has_staged_changes() {
  git -C "$ROOT_DIR" diff --cached --name-only --diff-filter=ACMR | grep -q .
}

list_changed_files() {
  local scope="${1:-staged}"

  if [[ "$scope" == "staged" ]] && has_staged_changes; then
    git -C "$ROOT_DIR" diff --cached --name-only --diff-filter=ACMR
    return
  fi

  git -C "$ROOT_DIR" diff --name-only --diff-filter=ACMR
}

needs_backend_checks() {
  local files="$1"
  grep -Eq '^(backend/|build\.gradle\.kts|settings\.gradle\.kts|gradle\.properties|Makefile|\.github/workflows/|scripts/quality/)' <<<"$files"
}

needs_frontend_checks() {
  local files="$1"
  grep -Eq '^(frontend/|package\.json|pnpm-lock\.yaml|pnpm-workspace\.yaml|\.github/workflows/|scripts/quality/)' <<<"$files"
}

needs_contract_checks() {
  local files="$1"
  grep -Eq '^(backend/app-boot/|docs/asyncapi\.yaml|Makefile|\.github/workflows/|scripts/quality/)' <<<"$files"
}
