#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

BASELINE_SPEC="${OPENAPI_BASELINE_SPEC:-backend/app-boot/src/main/resources/openapi.yaml}"
CURRENT_SPEC="${OPENAPI_CURRENT_SPEC:-backend/app-boot/build/reports/openapi/openapi.json}"

if [[ ! -f "$BASELINE_SPEC" ]]; then
  fail "Baseline OpenAPI spec not found at $BASELINE_SPEC"
fi

if [[ ! -f "$CURRENT_SPEC" ]]; then
  fail "Current OpenAPI export not found at $CURRENT_SPEC"
fi

log "Checking OpenAPI breaking changes"
run_quiet_or_fail "OpenAPI breaking change check failed" run_in_root python3 - "$BASELINE_SPEC" "$CURRENT_SPEC" <<'PY'
import json
import sys
from pathlib import Path

import yaml

baseline_path = Path(sys.argv[1])
current_path = Path(sys.argv[2])

baseline = yaml.safe_load(baseline_path.read_text(encoding="utf-8"))
current = json.loads(current_path.read_text(encoding="utf-8"))

ignored_paths = {"/v3/api-docs", "/swagger-ui.html"}

baseline_paths = {path for path in (baseline or {}).get("paths", {}).keys() if path not in ignored_paths}
current_paths = {path for path in (current or {}).get("paths", {}).keys() if path not in ignored_paths}

removed_paths = sorted(baseline_paths - current_paths)
if removed_paths:
    print("Breaking change detected: removed paths:")
    for path in removed_paths:
      print(f" - {path}")
    sys.exit(1)

def operation_required_fields(spec, path, method):
    request_body = (((spec or {}).get("paths", {}).get(path, {}) or {}).get(method, {}) or {}).get("requestBody", {})
    content = (request_body or {}).get("content", {})
    app_json = content.get("application/json", {})
    schema = app_json.get("schema", {})
    return set(schema.get("required", []) or [])

for path in sorted(baseline_paths & current_paths):
    baseline_methods = baseline.get("paths", {}).get(path, {})
    current_methods = current.get("paths", {}).get(path, {})
    for method in sorted(set(baseline_methods.keys()) & set(current_methods.keys())):
        if method.startswith("x-"):
            continue
        baseline_required = operation_required_fields(baseline, path, method)
        current_required = operation_required_fields(current, path, method)
        newly_required = sorted(current_required - baseline_required)
        if newly_required:
            print(f"Breaking change detected: {method.upper()} {path} has new required request fields: {', '.join(newly_required)}")
            sys.exit(1)

print("OpenAPI breaking change check passed")
PY

printf 'OK\n'
