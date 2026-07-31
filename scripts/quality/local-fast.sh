#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

run_or_fail "Backend fast verification failed" run_in_root ./gradlew verifyFast
run_or_fail "Frontend fast verification failed" run_in_root pnpm --dir frontend verify:fast
