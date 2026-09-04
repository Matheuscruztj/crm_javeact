# Contract Rollout Policy

## Goal

Prevent unapproved breaking changes from reaching consumers without an explicit migration window.

## Rules

- Breaking changes require a contract diff.
- Breaking changes require owner approval.
- Frontend consumers must be reviewed before release.
- Contract artifacts must be published with the build.
- Backward-incompatible changes need a rollout note and compatibility window.

## Minimum evidence

- OpenAPI export.
- Breaking-change diff result.
- Contract artifact bundle.
- Consumer impact review.
