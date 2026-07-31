#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

changed_files="$(list_changed_files staged)"

if [[ -z "$changed_files" ]]; then
  printf 'OK\n'
  exit 0
fi

run_quiet_or_fail "Staged diff contains whitespace errors or conflict markers" run_in_root git diff --cached --check

shell_files="$(printf '%s\n' "$changed_files" | grep -E '\.sh$' || true)"
yaml_files="$(printf '%s\n' "$changed_files" | grep -E '\.(yml|yaml)$' || true)"
json_files="$(printf '%s\n' "$changed_files" | grep -E '\.json$' || true)"

if [[ -n "$shell_files" ]]; then
  while IFS= read -r file; do
    [[ -z "$file" ]] && continue
    run_quiet_or_fail "Shell syntax check failed for $file" run_in_root bash -n "$file"
  done <<<"$shell_files"
fi

if [[ -n "$yaml_files" ]]; then
  while IFS= read -r file; do
    [[ -z "$file" ]] && continue
    run_quiet_or_fail "YAML validation failed for $file" run_in_root python3 - "$file" <<'PY'
import sys
from pathlib import Path

import yaml

path = Path(sys.argv[1])
with path.open("r", encoding="utf-8") as handle:
    yaml.safe_load(handle)
PY
  done <<<"$yaml_files"
fi

if [[ -n "$json_files" ]]; then
  while IFS= read -r file; do
    [[ -z "$file" ]] && continue
    run_quiet_or_fail "JSON validation failed for $file" run_in_root python3 - "$file" <<'PY'
import json
import sys
from pathlib import Path

path = Path(sys.argv[1])
with path.open("r", encoding="utf-8") as handle:
    json.load(handle)
PY
  done <<<"$json_files"
fi

changed_count="$(count_lines "$changed_files")"
changed_areas="$(summarize_changed_areas "$changed_files")"
areas_count="$(count_lines "$changed_areas")"

if [[ "$changed_count" -gt 12 || "$areas_count" -gt 3 ]]; then
  warn "This commit spans $changed_count files across $areas_count areas. Prefer smaller commits when practical."
  printf '%s\n' "$changed_areas"
fi

printf 'OK\n'
