#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

OPENAPI_EXPORT="${OPENAPI_EXPORT:-backend/app-boot/build/reports/openapi/openapi.json}"
OPENAPI_SNAPSHOT="${OPENAPI_SNAPSHOT:-backend/app-boot/src/main/resources/openapi.yaml}"
ASYNCAPI_SPEC="${ASYNCAPI_SPEC:-docs/asyncapi.yaml}"
CONTRACT_ARTIFACT_DIR="${CONTRACT_ARTIFACT_DIR:-build/reports/contracts}"

run_in_root mkdir -p "$CONTRACT_ARTIFACT_DIR"

for file in "$OPENAPI_EXPORT" "$OPENAPI_SNAPSHOT" "$ASYNCAPI_SPEC"; do
  [[ -f "$file" ]] || fail "Required contract artifact not found: $file"
done

run_in_root cp "$OPENAPI_EXPORT" "$CONTRACT_ARTIFACT_DIR/openapi-export.json"
run_in_root cp "$OPENAPI_SNAPSHOT" "$CONTRACT_ARTIFACT_DIR/openapi-snapshot.yaml"
run_in_root cp "$ASYNCAPI_SPEC" "$CONTRACT_ARTIFACT_DIR/asyncapi.yaml"

printf 'OK\n'
