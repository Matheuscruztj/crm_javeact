#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

ZAP_IMAGE="${ZAP_IMAGE:-ghcr.io/zaproxy/zaproxy:stable}"
ZAP_BASE_URL="${ZAP_BASE_URL:-http://localhost:8080}"
ZAP_REPORT_DIR="${ZAP_REPORT_DIR:-build/reports/zap}"
ZAP_TARGET_URL="${ZAP_TARGET_URL:-${ZAP_BASE_URL}/v3/api-docs}"
ZAP_WAIT_SECONDS="${ZAP_WAIT_SECONDS:-15}"
ZAP_MAX_TIME="${ZAP_MAX_TIME:-120}"
ZAP_AUTH_PROFILES="${ZAP_AUTH_PROFILES:-admin@atlasops.test|admin-demo-2025|a0000000-0000-0000-0000-000000000001;admin@atlasops.test|Admin123!|a0000000-0000-0000-0000-000000000001;admin@test-alpha.local|Test1234!|t1000000-0000-0000-0000-000000000001}"

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
  warn "application is not ready at ${ZAP_BASE_URL}; skipping authenticated DAST scan"
  printf 'OK\n'
  exit 0
fi

backend_health="$(docker inspect -f '{{.State.Health.Status}}' atlasops-backend-api 2>/dev/null || true)"
if [[ "$backend_health" != "healthy" ]]; then
  warn "backend-api container is ${backend_health:-unavailable}; skipping authenticated DAST scan"
  printf 'OK\n'
  exit 0
fi

log "Logging in for authenticated ZAP scan"
access_token=""
tenant_id=""
authenticated_email=""

IFS=';' read -r -a auth_profiles <<<"$ZAP_AUTH_PROFILES"
for profile in "${auth_profiles[@]}"; do
  IFS='|' read -r auth_email auth_password auth_tenant <<<"$profile"
  if [[ -z "${auth_email:-}" || -z "${auth_password:-}" || -z "${auth_tenant:-}" ]]; then
    continue
  fi

  if auth_response="$(curl -fsS \
    -X POST \
    -H "Content-Type: application/json" \
    -H "X-Tenant-ID: ${auth_tenant}" \
    -d "{\"email\":\"${auth_email}\",\"password\":\"${auth_password}\"}" \
    "${ZAP_BASE_URL}/api/v1/auth/login" 2>/dev/null)"; then
    access_token="$(python3 -c 'import json, sys; print(json.load(sys.stdin)["accessToken"])' <<<"$auth_response")"
    tenant_id="$auth_tenant"
    authenticated_email="$auth_email"
    if [[ -n "$access_token" ]]; then
      break
    fi
  fi
done

zap_extra_args=()
if [[ -n "$access_token" && -n "$tenant_id" ]]; then
  log "Authenticated as ${authenticated_email} for tenant ${tenant_id}"
  zap_extra_args=(
    -z "-config replacer.full_list(0).description=auth-header -config replacer.full_list(0).enabled=true -config replacer.full_list(0).matchtype=REQ_HEADER -config replacer.full_list(0).matchstr=Authorization -config replacer.full_list(0).replacement=Bearer%20${access_token} -config replacer.full_list(0).initiators=1,2,3,4 -config replacer.full_list(1).description=tenant-header -config replacer.full_list(1).enabled=true -config replacer.full_list(1).matchtype=REQ_HEADER -config replacer.full_list(1).matchstr=X-Tenant-ID -config replacer.full_list(1).replacement=${tenant_id} -config replacer.full_list(1).initiators=1,2,3,4"
  )
else
  warn "Authenticated login not available; falling back to unauthenticated API scan"
fi

log "Running OWASP ZAP API scan against ${ZAP_TARGET_URL}"
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
    "${zap_extra_args[@]}"

run_in_root mv -f zap-api.html "$ZAP_REPORT_DIR/zap-api.html"
run_in_root mv -f zap-api.json "$ZAP_REPORT_DIR/zap-api.json"
run_in_root mv -f zap-api.xml "$ZAP_REPORT_DIR/zap-api.xml"

printf 'OK\n'
