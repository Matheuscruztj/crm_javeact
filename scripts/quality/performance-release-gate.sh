#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

required_files=(
  "tests/load/reports/low-resource-baseline-2026-08-31.md"
  "docs/runbooks/PROFILING-AND-CAPACITY.md"
  "docs/quality/performance-policy.md"
  "docs/quality/performance-regression-review.md"
  "docs/quality/release-readiness.md"
)

for file in "${required_files[@]}"; do
  [[ -f "$file" ]] || fail "Required performance evidence not found: $file"
done

printf 'OK\n'
