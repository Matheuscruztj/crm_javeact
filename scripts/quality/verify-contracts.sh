#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

run_quiet_or_fail "OpenAPI contract export failed" run_in_root ./gradlew :backend:app-boot:openApiContractExportTest

command -v npx >/dev/null 2>&1 || fail "npx is required for contract linting"
run_quiet_or_fail "OpenAPI contract lint failed" run_in_root npx -y @stoplight/spectral-cli@6.15.0 lint backend/app-boot/build/reports/openapi/openapi.json

run_quiet_or_fail "AsyncAPI validation failed" run_in_root npx -y @asyncapi/cli@2.15.0 validate docs/asyncapi.yaml

run_quiet_or_fail "OpenAPI breaking change check failed" run_in_root ./scripts/quality/openapi-breaking-change.sh
run_quiet_or_fail "Contract artifact publication failed" run_in_root ./scripts/quality/publish-contract-artifacts.sh

printf 'OK\n'
