#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

HOOKS_DIR="$(git -C "$(repo_root)" rev-parse --git-path hooks)"

mkdir -p "$HOOKS_DIR"

cat >"${HOOKS_DIR}/pre-push" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(git rev-parse --show-toplevel)"
"${ROOT_DIR}/scripts/quality/pre-push.sh"
EOF

chmod +x "${HOOKS_DIR}/pre-push"
