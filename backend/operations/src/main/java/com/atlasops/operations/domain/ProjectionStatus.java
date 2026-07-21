package com.atlasops.operations.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity tracking the build status of a read model projection.
 * Used by the operations dashboard to show projection health and
 * trigger manual rebuilds.
 *
 * <p>Validates: P2.7 — Projection health registry
 */
public final class ProjectionStatus {

    /**
     * Lifecycle states of a projection.
     */
    public enum Status {
        /** Projection is disabled and not being updated. */
        DISABLED,
        /** Projection rebuild has been queued but not started. */
        PENDING,
        /** Projection rebuild is in progress. */
        PROCESSING,
        /** Projection is fully up-to-date. */
        READY,
        /** Projection exists but has fallen behind the event stream. */
        STALE,
        /** Projection rebuild failed. See {@code errorMessage}. */
        FAILED
    }

    private final String name;
    private Status status;
    private Instant lastBuiltAt;
    private long lag;
    private String errorMessage;

    private ProjectionStatus(
            String name, Status status, Instant lastBuiltAt, long lag, String errorMessage) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.lastBuiltAt = lastBuiltAt;
        this.lag = lag;
        this.errorMessage = errorMessage;
    }

    /** Creates a new projection status record in PENDING state. */
    public static ProjectionStatus create(String name) {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return new ProjectionStatus(name, Status.PENDING, null, 0L, null);
    }

    /** Reconstitutes from persisted state. */
    public static ProjectionStatus reconstitute(
            String name, Status status, Instant lastBuiltAt, long lag, String errorMessage) {
        return new ProjectionStatus(name, status, lastBuiltAt, lag, errorMessage);
    }

    /** Marks the projection as READY with the current timestamp. */
    public void markReady(Instant now) {
        this.status = Status.READY;
        this.lastBuiltAt = Objects.requireNonNull(now, "now must not be null");
        this.lag = 0L;
        this.errorMessage = null;
    }

    /** Marks the projection as FAILED with an error message. */
    public void markFailed(String error, Instant now) {
        this.status = Status.FAILED;
        this.errorMessage = error;
        this.lastBuiltAt = now;
    }

    /** Updates the lag counter (number of events behind). */
    public void updateLag(long lag) {
        this.lag = lag;
        if (lag > 0 && this.status == Status.READY) {
            this.status = Status.STALE;
        }
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getLastBuiltAt() {
        return lastBuiltAt;
    }

    public long getLag() {
        return lag;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
