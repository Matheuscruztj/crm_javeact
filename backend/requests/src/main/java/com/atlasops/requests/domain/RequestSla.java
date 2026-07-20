package com.atlasops.requests.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Value object representing the SLA (Service Level Agreement) for a request.
 * Calculates whether a request has breached its deadline.
 *
 * <p>Validates: P2.10 — Request SLA with deadline and alert
 *
 * @param deadlineDays number of business days until the SLA expires
 * @param deadline     the absolute deadline timestamp
 * @param breached     true if the SLA deadline has passed and the request is still open
 */
public record RequestSla(int deadlineDays, Instant deadline, boolean breached) {

    public RequestSla {
        Objects.requireNonNull(deadline, "deadline must not be null");
        if (deadlineDays <= 0) {
            throw new IllegalArgumentException("deadlineDays must be positive, got: " + deadlineDays);
        }
    }

    /**
     * Calculates an SLA for a request based on its creation time.
     *
     * @param createdAt    when the request was created
     * @param deadlineDays number of days until the SLA deadline
     * @param now          the current time (used to check if already breached)
     * @return a new {@link RequestSla} with deadline and breach status
     */
    public static RequestSla calculate(Instant createdAt, int deadlineDays, Instant now) {
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(now, "now must not be null");
        if (deadlineDays <= 0) {
            throw new IllegalArgumentException("deadlineDays must be positive, got: " + deadlineDays);
        }

        Instant deadline = createdAt.plus(deadlineDays, ChronoUnit.DAYS);
        boolean breached = now.isAfter(deadline);
        return new RequestSla(deadlineDays, deadline, breached);
    }

    /**
     * Returns true if the SLA deadline has passed.
     *
     * @param now current time
     * @return true if the deadline is in the past
     */
    public boolean isBreachedAt(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return now.isAfter(deadline);
    }
}
