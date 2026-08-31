#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ZAP_IMAGE="${ZAP_IMAGE:-ghcr.io/zaproxy/zaproxy:stable}"
ZAP_BASE_URL="${ZAP_BASE_URL:-http://localhost:8080}"
ZAP_REPORT_DIR="${ZAP_REPORT_DIR:-build/reports/zap}"
ZAP_TARGET_URL="${ZAP_TARGET_URL:-${ZAP_BASE_URL}/v3/api-docs}"
ZAP_WAIT_SECONDS="${ZAP_WAIT_SECONDS:-15}"
ZAP_MAX_TIME="${ZAP_MAX_TIME:-120}"
ZAP_AUTH_EMAIL="${ZAP_AUTH_EMAIL:-admin@atlasops.test}"
ZAP_AUTH_PASSWORD="${ZAP_AUTH_PASSWORD:-admin-demo-2025}"
ZAP_TENANT_ID="${ZAP_TENANT_ID:-t1000000-0000-0000-0000-000000000001}"

if ! command -v docker >/dev/null 2>&1; then
  fail "docker is required to run ZAP baseline locally."
fi

run_in_root mkdir -p "$ZAP_REPORT_DIR"

log "Waiting for application readiness at ${ZAP_BASE_URL}"
for _ in $(seq 1 "$ZAP_WAIT_SECONDS"); do
  if curl -fsS "${ZAP_BASE_URL}/actuator/health/readiness" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

if ! curl -fsS "${ZAP_BASE_URL}/actuator/health/readiness" >/dev/null 2>&1; then
  fail "application is not ready at ${ZAP_BASE_URL}"
fi

log "Logging in for authenticated ZAP scan"
auth_response="$(curl -fsS \
  -X POST \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: ${ZAP_TENANT_ID}" \
  -d "{\"email\":\"${ZAP_AUTH_EMAIL}\",\"password\":\"${ZAP_AUTH_PASSWORD}\"}" \
  "${ZAP_BASE_URL}/api/v1/auth/login")"

access_token="$(python3 -c 'import json, sys; print(json.load(sys.stdin)["accessToken"])' <<<"$auth_response")"

if [[ -z "$access_token" ]]; then
  fail "could not extract access token from login response"
fi

log "Running authenticated OWASP ZAP API scan against ${ZAP_TARGET_URL}"
run_quiet_or_fail "ZAP authenticated scan failed" run_in_root docker run --rm \
  --network host \
  -v "$ROOT_DIR:/zap/wrk" \
  "$ZAP_IMAGE" \
  zap-api-scan.py \
    -t "$ZAP_TARGET_URL" \
    -f openapi \
    -r zap-api.html \
    -J zap-api.json \
    -x zap-api.xml \
    -m "$ZAP_MAX_TIME" \
    -z "-config replacer.full_list(0).description=auth-header -config replacer.full_list(0).enabled=true -config replacer.full_list(0).matchtype=REQ_HEADER -config replacer.full_list(0).matchstr=Authorization -config replacer.full_list(0).replacement=Bearer%20${access_token} -config replacer.full_list(0).initiators=1,2,3,4 -config replacer.full_list(1).description=tenant-header -config replacer.full_list(1).enabled=true -config replacer.full_list(1).matchtype=REQ_HEADER -config replacer.full_list(1).matchstr=X-Tenant-ID -config replacer.full_list(1).replacement=${ZAP_TENANT_ID} -config replacer.full_list(1).initiators=1,2,3,4"

run_in_root mv -f zap-api.html "$ZAP_REPORT_DIR/zap-api.html"
run_in_root mv -f zap-api.json "$ZAP_REPORT_DIR/zap-api.json"
run_in_root mv -f zap-api.xml "$ZAP_REPORT_DIR/zap-api.xml"

printf 'OK\n'
