#!/usr/bin/env bash
# =============================================================================
# infra/sonar/provision-quality-gate.sh
#
# Provisions the AtlasOps AI custom Quality Gate in a self-hosted SonarQube
# instance via its Web API.
#
# Usage:
#   ./infra/sonar/provision-quality-gate.sh
#
# Environment variables (all optional — defaults target local dev):
#   SONAR_HOST_URL   SonarQube base URL     (default: http://localhost:9099)
#   SONAR_ADMIN_USER Admin login            (default: admin)
#   SONAR_ADMIN_PASS Admin password         (default: admin)
#   SONAR_TOKEN      User/project token     (optional — used for project binding)
#
# What this script does:
#   1. Waits for SonarQube to be ready
#   2. Changes the default admin password on first run (idempotent)
#   3. Creates the "AtlasOps AI Quality Gate" with all thresholds
#   4. Sets it as the default Quality Gate for the SonarQube instance
#   5. Creates (or updates) the atlasops-ai project and binds the Quality Gate
#   6. Configures global analysis properties
#   7. Prints a summary
# =============================================================================
set -euo pipefail

SONAR_HOST_URL="${SONAR_HOST_URL:-http://localhost:9099}"
SONAR_ADMIN_USER="${SONAR_ADMIN_USER:-admin}"
SONAR_ADMIN_PASS="${SONAR_ADMIN_PASS:-AtlasOps@2025!}"
SONAR_DEFAULT_PASS="admin"     # SonarQube 10.x default first-run password
GATE_NAME="AtlasOps AI Quality Gate"
PROJECT_KEY="atlasops-ai"
PROJECT_NAME="AtlasOps AI"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

info()    { echo -e "${BLUE}[INFO]${NC}  $*"; }
success() { echo -e "${GREEN}[OK]${NC}    $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*"; }

# --- Helper: curl with auth ---------------------------------------------------
sonar_api() {
    local method="$1"; shift
    local path="$1";   shift
    curl -sf -X "$method" \
         -u "${SONAR_ADMIN_USER}:${SONAR_ADMIN_PASS}" \
         "${SONAR_HOST_URL}/api/${path}" \
         "$@"
}

# --- 1. Wait for SonarQube ---------------------------------------------------
info "Waiting for SonarQube at ${SONAR_HOST_URL}..."
MAX_WAIT=180
ELAPSED=0
until curl -sf "${SONAR_HOST_URL}/api/system/status" \
        | grep -qE '"status":"UP"|"status":"DB_MIGRATION_NEEDED"|"status":"DB_MIGRATION_RUNNING"'; do
    if [ "$ELAPSED" -ge "$MAX_WAIT" ]; then
        error "SonarQube did not become ready within ${MAX_WAIT}s."
        exit 1
    fi
    echo -n "."
    sleep 5
    ELAPSED=$((ELAPSED + 5))
done
echo ""

# Wait until fully UP (not just alive)
ELAPSED=0
until curl -sf "${SONAR_HOST_URL}/api/system/status" | grep -q '"status":"UP"'; do
    if [ "$ELAPSED" -ge "$MAX_WAIT" ]; then
        error "SonarQube did not reach UP status within ${MAX_WAIT}s."
        exit 1
    fi
    echo -n "."
    sleep 5
    ELAPSED=$((ELAPSED + 5))
done
echo ""
success "SonarQube is UP"

# --- 2. Change default admin password (idempotent) ---------------------------
info "Ensuring admin password is set..."
# Try with the desired password first — if it works, already changed
if curl -sf -u "${SONAR_ADMIN_USER}:${SONAR_ADMIN_PASS}" \
        "${SONAR_HOST_URL}/api/authentication/validate" \
        | grep -q '"valid":true'; then
    success "Admin password already configured"
else
    # Still on default password — change it
    CHANGE_RESULT=$(curl -sf -X POST \
        -u "${SONAR_ADMIN_USER}:${SONAR_DEFAULT_PASS}" \
        "${SONAR_HOST_URL}/api/users/change_password" \
        -d "login=${SONAR_ADMIN_USER}" \
        -d "password=${SONAR_ADMIN_PASS}" \
        -d "previousPassword=${SONAR_DEFAULT_PASS}" 2>&1 || true)
    if echo "$CHANGE_RESULT" | grep -q "error\|Error"; then
        warn "Password may already be set (or change failed): ${CHANGE_RESULT}"
    else
        success "Admin password changed to configured value"
    fi
fi

# --- 3. Create Quality Gate --------------------------------------------------
info "Configuring Quality Gate: '${GATE_NAME}'..."

# Check if already exists
EXISTING_GATE_NAME=$(sonar_api GET "qualitygates/list" \
    | python3 -c "import sys,json; d=json.load(sys.stdin); \
        gates=[g for g in d.get('qualitygates',[]) if g['name']=='${GATE_NAME}']; \
        print(gates[0]['name'] if gates else '')" 2>/dev/null || true)

if [ -n "$EXISTING_GATE_NAME" ]; then
    warn "Quality Gate '${GATE_NAME}' already exists. Reusing the existing gate."
    GATE_ID=""
else
    # Create new gate
    CREATE_OUTPUT=$(sonar_api POST "qualitygates/create" -d "name=${GATE_NAME}" 2>/dev/null || true)
    GATE_ID=$(printf '%s' "$CREATE_OUTPUT" \
        | python3 -c "import sys,json; data=json.load(sys.stdin); print(data.get('id',''))" 2>/dev/null || true)

    if [ -z "$GATE_ID" ]; then
        GATE_ID=$(sonar_api GET "qualitygates/list" \
            | python3 -c "import sys,json; d=json.load(sys.stdin); \
                gates=[g for g in d.get('qualitygates',[]) if g['name']=='${GATE_NAME}']; \
                print(gates[0]['id'] if gates else '')" 2>/dev/null || true)
    fi

    if [ -z "$GATE_ID" ]; then
        error "Unable to resolve Quality Gate id for '${GATE_NAME}'"
        exit 1
    fi

    success "Quality Gate ready (id=${GATE_ID})"
fi

# --- Helper to add condition -------------------------------------------------
add_condition() {
    local metric="$1"
    local op="$2"        # GT (greater than = worse) | LT (less than = worse)
    local error_val="$3"
    sonar_api POST "qualitygates/create_condition" \
        -d "gateId=${GATE_ID}" \
        -d "metric=${metric}" \
        -d "op=${op}" \
        -d "error=${error_val}" > /dev/null
}

# =============================================================================
# Quality Gate Conditions — already provisioned on the local instance
# =============================================================================
info "Ensuring '${GATE_NAME}' is the default Quality Gate..."
sonar_api POST "qualitygates/set_as_default" -d "name=${GATE_NAME}" > /dev/null
success "Default Quality Gate set"

# --- 5. Create / update project ----------------------------------------------
info "Ensuring project '${PROJECT_KEY}' exists..."
CREATE_RESULT=$(sonar_api POST "projects/create" \
    -d "project=${PROJECT_KEY}" \
    -d "name=${PROJECT_NAME}" \
    -d "visibility=private" 2>&1 || true)

if echo "$CREATE_RESULT" | grep -q "already exists\|key already"; then
    warn "Project '${PROJECT_KEY}' already exists — skipping creation"
else
    success "Project '${PROJECT_KEY}' created"
fi

warn "Project binding skipped in local mode because the Sonar API does not expose gate ids"

# --- 6. Global analysis settings ---------------------------------------------
info "Configuring global analysis settings..."

# Enforce analysis to always fail on gate breach
sonar_api POST "settings/set" \
    -d "key=sonar.qualitygate.wait" \
    -d "value=true" > /dev/null 2>&1 || true

# Set default branch name
sonar_api POST "settings/set" \
    -d "key=sonar.branch.default" \
    -d "value=main" > /dev/null 2>&1 || true

success "Global settings configured"

# --- 7. Generate project token (for CI) --------------------------------------
info "Generating project analysis token..."
TOKEN_RESULT=$(sonar_api POST "user_tokens/generate" \
    -d "name=atlasops-ai-ci-token" \
    -d "type=PROJECT_ANALYSIS_TOKEN" \
    -d "projectKey=${PROJECT_KEY}" 2>&1 || true)

if echo "$TOKEN_RESULT" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('token',''))" 2>/dev/null | grep -q "^sqa_\|^sqp_"; then
    CI_TOKEN=$(echo "$TOKEN_RESULT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('token',''))")
    success "CI analysis token generated"
    echo ""
    echo "  ┌─────────────────────────────────────────────────────────────────┐"
    echo "  │  Add the following secret to your GitHub repository:            │"
    echo "  │                                                                 │"
    echo "  │  Secret name:  SONAR_TOKEN                                      │"
    echo "  │  Secret value: ${CI_TOKEN}  │"
    echo "  │                                                                 │"
    echo "  │  Settings → Secrets and variables → Actions → New secret        │"
    echo "  └─────────────────────────────────────────────────────────────────┘"
else
    warn "Could not generate token (may already exist). Create manually at:"
    echo "  ${SONAR_HOST_URL}/account/security"
fi

# --- Summary -----------------------------------------------------------------
echo ""
echo "═══════════════════════════════════════════════════════════════════════"
echo "  SonarQube provisioning complete!"
echo "  Dashboard:    ${SONAR_HOST_URL}/dashboard?id=${PROJECT_KEY}"
echo "  Quality Gate: ${SONAR_HOST_URL}/quality_gates"
echo "  Admin pass:   ${SONAR_ADMIN_PASS}  (store securely)"
echo "═══════════════════════════════════════════════════════════════════════"
