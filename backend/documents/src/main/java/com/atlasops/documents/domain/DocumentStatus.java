package com.atlasops.documents.domain;

import java.util.Set;

/**
 * Enum representing the lifecycle status of a document.
 *
 * <p>Valid transitions:
 *
 * <ul>
 *   <li>PENDING_UPLOAD → UPLOADED
 *   <li>PENDING_UPLOAD → UPLOAD_FAILED
 *   <li>UPLOADED → TEXT_EXTRACTED
 *   <li>UPLOADED → PROCESSING_FAILED
 *   <li>TEXT_EXTRACTED → ANALYZED
 *   <li>TEXT_EXTRACTED → PROCESSING_FAILED
 * </ul>
 */
public enum DocumentStatus {
  PENDING_UPLOAD(Set.of()),
  UPLOADED(Set.of()),
  TEXT_EXTRACTED(Set.of()),
  ANALYZED(Set.of()),
  UPLOAD_FAILED(Set.of()),
  PROCESSING_FAILED(Set.of());

  private final Set<DocumentStatus> allowedTransitions;

  DocumentStatus(Set<DocumentStatus> allowedTransitions) {
    this.allowedTransitions = allowedTransitions;
  }

  /** Returns the set of statuses this status can transition to. */
  public Set<DocumentStatus> getAllowedTransitions() {
    return allowedTransitions;
  }

  /**
   * Checks if a transition from this status to the target status is valid.
   *
   * @param target the target status
   * @return true if the transition is allowed
   */
  public boolean canTransitionTo(DocumentStatus target) {
    return TRANSITIONS.get(this).contains(target);
  }

  // Static initialization to define allowed transitions (avoids forward reference issue)
  private static final java.util.Map<DocumentStatus, Set<DocumentStatus>> TRANSITIONS =
      java.util.Map.of(
          PENDING_UPLOAD, Set.of(UPLOADED, UPLOAD_FAILED),
          UPLOADED, Set.of(TEXT_EXTRACTED, PROCESSING_FAILED),
          TEXT_EXTRACTED, Set.of(ANALYZED, PROCESSING_FAILED),
          ANALYZED, Set.of(),
          UPLOAD_FAILED, Set.of(),
          PROCESSING_FAILED, Set.of());
}
