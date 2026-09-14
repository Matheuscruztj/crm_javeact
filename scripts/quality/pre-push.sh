#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

changed_files="$(list_changed_files working-tree)"

PRE_PUSH_GRADLE_HEAP="${PRE_PUSH_GRADLE_HEAP:-8g}"
PRE_PUSH_GRADLE_WORKERS="${PRE_PUSH_GRADLE_WORKERS:-6}"
PRE_PUSH_NODE_OLD_SPACE_MB="${PRE_PUSH_NODE_OLD_SPACE_MB:-8192}"
PRE_PUSH_GRADLE_OPTS="${PRE_PUSH_GRADLE_OPTS:--Xmx${PRE_PUSH_GRADLE_HEAP} -XX:+UseG1GC -XX:MaxMetaspaceSize=1g}"
PRE_PUSH_NODE_OPTIONS="${PRE_PUSH_NODE_OPTIONS:---max-old-space-size=${PRE_PUSH_NODE_OLD_SPACE_MB}}"

run_pre_push_job() {
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

run_frontend_checks() {
  run_pre_push_job "Frontend verification failed" run_in_root env NODE_OPTIONS="$PRE_PUSH_NODE_OPTIONS" pnpm --dir frontend verify:fast
  run_pre_push_job "Frontend architecture checks failed" run_in_root env NODE_OPTIONS="$PRE_PUSH_NODE_OPTIONS" pnpm --dir frontend architecture:check
  run_pre_push_job "Frontend unit coverage failed" run_in_root env NODE_OPTIONS="$PRE_PUSH_NODE_OPTIONS" pnpm --dir frontend test:unit:coverage
}

run_initial_checks_parallel() {
  local backend_output frontend_output contract_output
  backend_output="$(mktemp)"
  frontend_output="$(mktemp)"
  contract_output="$(mktemp)"

  run_in_root env GRADLE_OPTS="$PRE_PUSH_GRADLE_OPTS" ./gradlew \
    --no-configuration-cache \
    --parallel \
    --max-workers="$PRE_PUSH_GRADLE_WORKERS" \
    verifyFast \
    checkstyleMain \
    spotbugsMain \
    testProperty \
    :backend:app-boot:architectureTest >"$backend_output" 2>&1 &
  local backend_pid=$!

  run_frontend_checks >"$frontend_output" 2>&1 &
  local frontend_pid=$!

  run_in_root env \
    GRADLE_OPTS="$PRE_PUSH_GRADLE_OPTS" \
    NODE_OPTIONS="$PRE_PUSH_NODE_OPTIONS" \
    PRE_PUSH_GRADLE_WORKERS="$PRE_PUSH_GRADLE_WORKERS" \
    make verify-contracts >"$contract_output" 2>&1 &
  local contract_pid=$!

  local failed=0

  if ! wait "$backend_pid"; then
    cat "$backend_output" >&2
    printf 'ERROR: Backend verification failed\n' >&2
    failed=1
  fi

  if ! wait "$frontend_pid"; then
    cat "$frontend_output" >&2
    printf 'ERROR: Frontend verification failed\n' >&2
    failed=1
  fi

  if ! wait "$contract_pid"; then
    cat "$contract_output" >&2
    printf 'ERROR: Contract verification failed\n' >&2
    failed=1
  fi

  rm -f "$backend_output" "$frontend_output" "$contract_output"

  if [[ "$failed" -ne 0 ]]; then
    exit 1
  fi
}

run_initial_checks_serial() {
  run_quiet_or_fail "Backend verification failed" run_in_root env GRADLE_OPTS="$PRE_PUSH_GRADLE_OPTS" ./gradlew \
    --no-configuration-cache \
    --max-workers="$PRE_PUSH_GRADLE_WORKERS" \
    verifyFast \
    checkstyleMain \
    spotbugsMain \
    testProperty \
    :backend:app-boot:architectureTest
  run_frontend_checks
  run_quiet_or_fail "Contract verification failed" run_in_root env \
    GRADLE_OPTS="$PRE_PUSH_GRADLE_OPTS" \
    NODE_OPTIONS="$PRE_PUSH_NODE_OPTIONS" \
    PRE_PUSH_GRADLE_WORKERS="$PRE_PUSH_GRADLE_WORKERS" \
    make verify-contracts
}

if [[ "${PRE_PUSH_SERIAL:-0}" == "1" ]]; then
  run_initial_checks_serial
else
  run_initial_checks_parallel
fi

if [[ -z "$changed_files" ]] || needs_resilience_checks "$changed_files"; then
  run_quiet_or_fail "Resilience checks failed" run_in_root make test-resilience-minio
fi

if [[ -z "$changed_files" ]] || needs_security_checks "$changed_files"; then
  if [[ -z "$changed_files" ]] || needs_sast_checks "$changed_files"; then
    run_quiet_or_fail "SAST scan failed" run_in_root make verify-sast
  fi

  if [[ -z "$changed_files" ]] || needs_dast_checks "$changed_files"; then
    run_quiet_or_fail "DAST scan failed" run_in_root make verify-dast
  fi

  run_quiet_or_fail "Filesystem security scan failed" run_in_root make verify-container-security
fi

printf 'OK\n'
