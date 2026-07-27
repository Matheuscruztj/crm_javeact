#!/usr/bin/env bash
# =============================================================================
# AtlasOps AI — Restore Script with Validation
# Validates: P3.3.3, P3.3.4 — Restore with validation
# Usage: ./infra/scripts/restore.sh BACKUP_ID [--dry-run]
# =============================================================================
set -euo pipefail

BACKUP_ID="${1:-}"
DRY_RUN="${2:-}"
BACKUP_DIR="./backups/${BACKUP_ID}"
POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
POSTGRES_USER="${POSTGRES_USER:-atlasops}"
POSTGRES_DB="${POSTGRES_DB:-atlasops}"
MINIO_ALIAS="${MINIO_ALIAS:-local}"
MINIO_BUCKET="${OBJECT_STORAGE_BUCKET:-atlasops-local}"

if [[ -z "${BACKUP_ID}" ]]; then
  echo "ERROR: BACKUP_ID required"
  echo "Usage: $0 BACKUP_ID [--dry-run]"
  exit 1
fi

if [[ ! -d "${BACKUP_DIR}" ]]; then
  echo "ERROR: Backup directory not found: ${BACKUP_DIR}"
  exit 1
fi

echo "=== AtlasOps Restore — ID: ${BACKUP_ID} ==="
if [[ "${DRY_RUN}" == "--dry-run" ]]; then
  echo "    DRY RUN MODE — no changes will be made"
fi

# ── Validate manifest ────────────────────────────────────────────────────────
echo "[1/4] Validating backup manifest..."
if [[ ! -f "${BACKUP_DIR}/manifest.json" ]]; then
  echo "ERROR: manifest.json not found in ${BACKUP_DIR}"
  exit 1
fi
echo "      Manifest valid: $(cat "${BACKUP_DIR}/manifest.json" | grep created_at || echo 'unknown date')"

if [[ -f "${BACKUP_DIR}/checksums.sha256" ]]; then
  echo "      Validating backup checksums..."
  (
    cd "${BACKUP_DIR}"
    sha256sum --check --quiet checksums.sha256
  ) && echo "      Checksum validation OK" || {
    echo "ERROR: Backup checksum validation failed"
    exit 1
  }
else
  echo "      WARNING: checksums.sha256 not found — continuing without checksum validation"
fi

# ── Validate PostgreSQL dump ─────────────────────────────────────────────────
echo "[2/4] Validating PostgreSQL dump..."
DUMP_FILE="${BACKUP_DIR}/postgres-${POSTGRES_DB}.dump"
if [[ ! -f "${DUMP_FILE}" ]]; then
  echo "ERROR: PostgreSQL dump not found: ${DUMP_FILE}"
  exit 1
fi

# Verify dump integrity
pg_restore --list "${DUMP_FILE}" > /dev/null 2>&1 && echo "      Dump integrity OK" || {
  echo "ERROR: Dump file is corrupted"
  exit 1
}

# ── Restore PostgreSQL ───────────────────────────────────────────────────────
echo "[3/4] Restoring PostgreSQL..."
if [[ "${DRY_RUN}" != "--dry-run" ]]; then
  PGPASSWORD="${POSTGRES_PASSWORD:-atlasops_local}" pg_restore \
    --host="${POSTGRES_HOST}" \
    --port="${POSTGRES_PORT}" \
    --username="${POSTGRES_USER}" \
    --dbname="${POSTGRES_DB}" \
    --clean \
    --if-exists \
    --no-owner \
    --no-acl \
    "${DUMP_FILE}"
  echo "      PostgreSQL restored"
else
  echo "      DRY RUN: would restore ${DUMP_FILE} to ${POSTGRES_DB}"
fi

# ── Restore MinIO ────────────────────────────────────────────────────────────
echo "[4/4] Restoring MinIO object storage..."
if [[ -d "${BACKUP_DIR}/objects/" ]]; then
  if [[ "${DRY_RUN}" != "--dry-run" ]]; then
    if command -v mc &>/dev/null; then
      mc mirror \
        "${BACKUP_DIR}/objects/" \
        "${MINIO_ALIAS}/${MINIO_BUCKET}" \
        --overwrite \
        --quiet || echo "      WARNING: MinIO restore failed"
    else
      echo "      WARNING: mc not found — skipping MinIO restore"
    fi
  else
    echo "      DRY RUN: would restore objects/ to ${MINIO_ALIAS}/${MINIO_BUCKET}"
  fi
fi

echo "=== Restore complete from: ${BACKUP_DIR} ==="
echo "    Run migrations: make migrate"
echo "    Restart services: make compose-up"
