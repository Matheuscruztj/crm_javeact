package com.atlasops.shared.harness;

import java.util.Objects;

/**
 * Enforces cross-sandbox isolation by validating that operations use exclusively the namespace of
 * the requesting sandbox's run_id.
 *
 * <p>Sandbox namespaces are derived from the run_id following the convention:
 *
 * <ul>
 *   <li>Branch: sandbox/{runId}
 *   <li>Database: atlasops_{issue} (extracted from runId)
 *   <li>Compose project: atlasops_{issue}
 *   <li>Bucket prefix: {issue}/
 * </ul>
 *
 * <p>Any operation attempting to access a namespace that does not belong to the requesting run_id
 * is rejected with a {@link SecurityException}.
 */
public class CrossSandboxIsolation {

  /**
   * Validates that the requesting sandbox (identified by runId) is allowed to access the specified
   * target namespace.
   *
   * <p>The target namespace must belong to the sandbox identified by runId. A namespace "belongs"
   * to a sandbox if it contains the issue identifier extracted from the runId.
   *
   * @param runId the run_id of the requesting sandbox (e.g., "ATLAS-42-agent-A2")
   * @param targetNamespace the namespace being accessed (e.g., database name, bucket path, branch)
   * @throws SecurityException if the target namespace does not belong to the requesting sandbox
   * @throws NullPointerException if either argument is null
   */
  public void validateAccess(String runId, String targetNamespace) {
    Objects.requireNonNull(runId, "runId must not be null");
    Objects.requireNonNull(targetNamespace, "targetNamespace must not be null");

    if (runId.isBlank()) {
      throw new SecurityException("Cross-sandbox violation: runId is blank");
    }

    if (targetNamespace.isBlank()) {
      throw new SecurityException("Cross-sandbox violation: targetNamespace is blank");
    }

    String issueId = extractIssueId(runId);

    if (!namespaceMatchesIssue(targetNamespace, issueId)) {
      throw new SecurityException(
          "Cross-sandbox violation: runId '"
              + runId
              + "' cannot access namespace '"
              + targetNamespace
              + "' (expected namespace containing issue '"
              + issueId
              + "')");
    }
  }

  /**
   * Extracts the issue identifier from a run_id.
   *
   * <p>Convention: run_id = {issue}-agent-{role} Example: "ATLAS-42-agent-A2" → "ATLAS-42"
   *
   * @param runId the full run identifier
   * @return the issue portion of the run_id
   */
  String extractIssueId(String runId) {
    int agentIdx = runId.lastIndexOf("-agent-");
    if (agentIdx > 0) {
      return runId.substring(0, agentIdx);
    }
    // If the pattern is not found, use the full runId as the issue identifier
    return runId;
  }

  /**
   * Checks whether a target namespace belongs to the given issue.
   *
   * <p>A namespace belongs to an issue if it contains the issue identifier (case-insensitive) or a
   * normalized form of it (underscores instead of hyphens), matched as a complete token. This
   * prevents false positives where one issue ID is a prefix of another (e.g., "ABC-500" vs
   * "ABC-5001").
   */
  private boolean namespaceMatchesIssue(String targetNamespace, String issueId) {
    String normalizedNamespace = targetNamespace.toLowerCase();
    String normalizedIssue = issueId.toLowerCase();
    String underscoreIssue = normalizedIssue.replace("-", "_");

    return containsAsWholeToken(normalizedNamespace, normalizedIssue)
        || containsAsWholeToken(normalizedNamespace, underscoreIssue);
  }

  /**
   * Checks if the namespace contains the token as a complete unit (not as a prefix of a longer
   * token). A token boundary is defined as start/end-of-string or a separator character (/, -, _).
   */
  private boolean containsAsWholeToken(String namespace, String token) {
    int idx = namespace.indexOf(token);
    while (idx >= 0) {
      int endIdx = idx + token.length();
      boolean startBoundary = idx == 0 || isSeparator(namespace.charAt(idx - 1));
      boolean endBoundary = endIdx >= namespace.length() || isSeparator(namespace.charAt(endIdx));
      if (startBoundary && endBoundary) {
        return true;
      }
      idx = namespace.indexOf(token, idx + 1);
    }
    return false;
  }

  private boolean isSeparator(char c) {
    return c == '/' || c == '-' || c == '_' || c == '\\';
  }
}
