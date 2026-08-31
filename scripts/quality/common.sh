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

count_lines() {
  local value="${1:-}"

  if [[ -z "$value" ]]; then
    printf '0\n'
    return
  fi

  printf '%s\n' "$value" | sed '/^[[:space:]]*$/d' | wc -l | tr -d ' '
}

summarize_changed_areas() {
  local files="${1:-}"

  if [[ -z "$files" ]]; then
    return
  fi

  printf '%s\n' "$files" | awk -F/ '
    /^[[:space:]]*$/ { next }
    {
      if ($1 == ".github") {
        print ".github/workflows"
      } else if ($1 == "scripts" && $2 == "quality") {
        print "scripts/quality"
      } else if ($1 == "backend" || $1 == "frontend" || $1 == "docs" || $1 == "infra") {
        print $1
      } else {
        print "(root)"
      }
    }
  ' | sort -u
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

needs_frontend_unit_checks() {
  local files="$1"
  grep -Eq '^(frontend/|package\.json|pnpm-lock\.yaml|pnpm-workspace\.yaml|\.github/workflows/|scripts/quality/)' <<<"$files"
}

needs_security_checks() {
  local files="$1"
  grep -Eq '^(backend/|frontend/|infra/|docker-compose\.yml|Makefile|\.github/workflows/|scripts/quality/)' <<<"$files"
}

needs_sast_checks() {
  local files="$1"
  grep -Eq '^(backend/|frontend/|Makefile|\.github/workflows/|scripts/quality/|build\.gradle\.kts|settings\.gradle\.kts|gradle\.properties|package\.json|pnpm-lock\.yaml|pnpm-workspace\.yaml)' <<<"$files"
}

needs_dast_checks() {
  local files="$1"
  grep -Eq '^(backend/app-boot/|backend/auth/|backend/customers/|backend/documents/|backend/requests/|backend/approvals/|backend/tenants/|backend/users/|frontend/|docker-compose\.yml|Makefile|\.github/workflows/|scripts/quality/)' <<<"$files"
}

needs_resilience_checks() {
  local files="$1"
  grep -Eq '^(backend/app-boot/|backend/ai/|backend/documents/|backend/integrations/|infra/|docker-compose\.yml|Makefile|\.github/workflows/)' <<<"$files"
}
