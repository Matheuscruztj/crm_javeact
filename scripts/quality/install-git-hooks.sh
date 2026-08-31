#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "$0")" && pwd)/common.sh"

HOOKS_PATH="$(git -C "$(repo_root)" rev-parse --git-path hooks)"
HOOKS_DIR="$(dirname "$HOOKS_PATH")"

mkdir -p "$HOOKS_DIR"

if [[ "$(basename "$HOOKS_PATH")" == "_" ]]; then
  # Husky manages the dispatcher files in .husky/_ and calls the source hooks
  # from .husky/pre-push.
  chmod +x "$(repo_root)/.husky/pre-push"
else
  cat >"${HOOKS_PATH}/pre-push" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(git rev-parse --show-toplevel)"
"${ROOT_DIR}/scripts/quality/pre-push.sh"
EOF

  chmod +x "${HOOKS_PATH}/pre-push"
fi
