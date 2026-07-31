#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

if [[ $# -lt 1 ]]; then
  fail "Usage: scripts/quality/backend-module.sh <module-name|:backend:module|backend/module-path>"
fi

module_input="$1"

case "$module_input" in
  :backend:*)
    gradle_module="$module_input"
    ;;
  backend/*)
    module_name="${module_input#backend/}"
    module_name="${module_name%%/*}"
    gradle_module=":backend:${module_name}"
    ;;
  *)
    gradle_module=":backend:${module_input}"
    ;;
esac

run_or_fail "Compilation failed for ${gradle_module}" run_in_root ./gradlew "${gradle_module}:compileJava"
run_or_fail "Tests failed for ${gradle_module}" run_in_root ./gradlew "${gradle_module}:test"
