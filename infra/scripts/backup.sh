#!/usr/bin/env bash
# =============================================================================
# AtlasOps AI — PostgreSQL + MinIO Backup Script
# Validates: P3.3.1, P3.3.2 — Backup scripts
# Usage: ./infra/scripts/backup.sh [BACKUP_ID]
# =============================================================================
set -euo pipefail

BACKUP_ID="${1:-$(date +%Y%m%d%H%M%S)}"
BACKUP_DIR="./backups/${BACKUP_ID}"
POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
POSTGRES_USER="${POSTGRES_USER:-atlasops}"
POSTGRES_DB="${POSTGRES_DB:-atlasops}"
MINIO_ALIAS="${MINIO_ALIAS:-local}"
MINIO_BUCKET="${OBJECT_STORAGE_BUCKET:-atlasops-local}"

echo "=== AtlasOps Backup — ID: ${BACKUP_ID} ==="
mkdir -p "${BACKUP_DIR}"

# ── PostgreSQL Dump ──────────────────────────────────────────────────────────
echo "[1/3] Backing up PostgreSQL database..."
PGPASSWORD="${POSTGRES_PASSWORD:-atlasops_local}" pg_dump \
  --host="${POSTGRES_HOST}" \
  --port="${POSTGRES_PORT}" \
  --username="${POSTGRES_USER}" \
  --dbname="${POSTGRES_DB}" \
  --format=custom \
  --no-owner \
  --no-acl \
  --file="${BACKUP_DIR}/postgres-${POSTGRES_DB}.dump"

echo "      PostgreSQL backup: ${BACKUP_DIR}/postgres-${POSTGRES_DB}.dump"

# ── MinIO Object Storage Sync ────────────────────────────────────────────────
echo "[2/3] Backing up MinIO object storage..."
if command -v mc &>/dev/null; then
  mc mirror \
    "${MINIO_ALIAS}/${MINIO_BUCKET}" \
    "${BACKUP_DIR}/objects/" \
    --overwrite \
    --quiet || echo "      WARNING: MinIO backup failed (mc not configured?)"
else
  echo "      WARNING: mc (MinIO client) not found — skipping object storage backup"
fi

# ── Manifest ─────────────────────────────────────────────────────────────────
echo "[3/3] Writing backup manifest..."
cat > "${BACKUP_DIR}/manifest.json" << EOF
{
  "backup_id": "${BACKUP_ID}",
  "created_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "postgres_db": "${POSTGRES_DB}",
  "postgres_host": "${POSTGRES_HOST}",
  "minio_bucket": "${MINIO_BUCKET}",
  "files": [
    "postgres-${POSTGRES_DB}.dump",
    "objects/"
  ]
}
EOF

echo "=== Backup complete: ${BACKUP_DIR} ==="
echo "    Restore with: ./infra/scripts/restore.sh ${BACKUP_ID}"
