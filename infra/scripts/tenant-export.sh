#!/usr/bin/env bash
# =============================================================================
# AtlasOps AI — Tenant Data Export (Portability)
# Validates: P3.3.5 — Tenant export/import (portability)
# Usage: ./infra/scripts/tenant-export.sh TENANT_ID [OUTPUT_DIR]
# =============================================================================
set -euo pipefail

TENANT_ID="${1:-}"
OUTPUT_DIR="${2:-./exports/${TENANT_ID}}"
POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
POSTGRES_USER="${POSTGRES_USER:-atlasops}"
POSTGRES_DB="${POSTGRES_DB:-atlasops}"
MINIO_ALIAS="${MINIO_ALIAS:-local}"
MINIO_BUCKET="${OBJECT_STORAGE_BUCKET:-atlasops-local}"

if [[ -z "${TENANT_ID}" ]]; then
  echo "ERROR: TENANT_ID required"
  echo "Usage: $0 TENANT_ID [OUTPUT_DIR]"
  exit 1
fi

echo "=== AtlasOps Tenant Export — Tenant: ${TENANT_ID} ==="
mkdir -p "${OUTPUT_DIR}"

# ── Export tenant data from PostgreSQL ───────────────────────────────────────
echo "[1/4] Exporting tenant data from PostgreSQL..."

TABLES=(customers requests comments documents approvals activities notifications users)

for table in "${TABLES[@]}"; do
  echo "  → $table"
  PGPASSWORD="${POSTGRES_PASSWORD:-atlasops_local}" psql \
    --host="${POSTGRES_HOST}" \
    --port="${POSTGRES_PORT}" \
    --username="${POSTGRES_USER}" \
    --dbname="${POSTGRES_DB}" \
    --no-password \
    --command="\COPY (SELECT * FROM ${table} WHERE tenant_id = '${TENANT_ID}') TO STDOUT CSV HEADER" \
    > "${OUTPUT_DIR}/${table}.csv" 2>/dev/null || echo "  WARNING: ${table} export failed (table may not have tenant_id)"
done

# ── Export tenant config ──────────────────────────────────────────────────────
echo "[2/4] Exporting tenant configuration..."
PGPASSWORD="${POSTGRES_PASSWORD:-atlasops_local}" psql \
  --host="${POSTGRES_HOST}" \
  --port="${POSTGRES_PORT}" \
  --username="${POSTGRES_USER}" \
  --dbname="${POSTGRES_DB}" \
  --no-password \
  --command="\COPY (SELECT * FROM tenants WHERE id = '${TENANT_ID}') TO STDOUT CSV HEADER" \
  > "${OUTPUT_DIR}/tenant.csv" 2>/dev/null || echo "  WARNING: tenant config export failed"

# ── Export object storage files ───────────────────────────────────────────────
echo "[3/4] Exporting object storage files..."
if command -v mc &>/dev/null; then
  mc mirror \
    "${MINIO_ALIAS}/${MINIO_BUCKET}/${TENANT_ID}/" \
    "${OUTPUT_DIR}/objects/" \
    --overwrite \
    --quiet 2>/dev/null || echo "  WARNING: Object storage export failed"
else
  echo "  WARNING: mc not found — skipping object storage export"
fi

# ── Write manifest ────────────────────────────────────────────────────────────
echo "[4/4] Writing export manifest..."
cat > "${OUTPUT_DIR}/manifest.json" << EOF
{
  "tenant_id": "${TENANT_ID}",
  "exported_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "tables": $(printf '%s\n' "${TABLES[@]}" | python3 -c "import sys,json; print(json.dumps(sys.stdin.read().splitlines()))"),
  "postgres_host": "${POSTGRES_HOST}",
  "minio_bucket": "${MINIO_BUCKET}"
}
EOF

echo "=== Export complete: ${OUTPUT_DIR} ==="
echo "    Files:"
ls -lh "${OUTPUT_DIR}"
