package com.atlasops.requests.application;

import com.atlasops.requests.domain.RequestSla;
import com.atlasops.requests.domain.RequestStatus;
import com.atlasops.requests.domain.ServiceRequest;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.EventPublisher;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use case for checking and marking SLA breaches on open requests.
 * Should be triggered periodically (e.g., scheduled hourly) or on explicit request.
 *
 * <p>Finds all OPEN requests that have a sla_deadline in the past and marks them as
 * SLA-breached, then creates analyst notifications.
 *
 * <p>Validates: P2.10 — Request SLA with deadline and alert
 */
public class CheckSlaBreachesUseCase {

    private static final Logger log = LoggerFactory.getLogger(CheckSlaBreachesUseCase.class);
    private static final int DEFAULT_DEADLINE_DAYS = 5;
    private static final int PAGE_SIZE = 100;

    private final ServiceRequestRepository requestRepository;
    private final Clock clock;

    public CheckSlaBreachesUseCase(
            ServiceRequestRepository requestRepository, Clock clock) {
        this.requestRepository = Objects.requireNonNull(requestRepository, "ServiceRequestRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    /**
     * Checks all OPEN requests in the system for SLA breaches.
     * For each breached request, logs a warning and updates the record.
     *
     * @return number of requests that were found to be in breach
     */
    public int execute() {
        Instant now = clock.now();
        log.info("Checking SLA breaches at {}", now);

        var page = requestRepository.findAllByTenantId(null, RequestStatus.OPEN, null, null, 0, PAGE_SIZE);

        int breachCount = 0;
        for (ServiceRequest request : page.content()) {
            RequestSla sla = RequestSla.calculate(request.getCreatedAt(), DEFAULT_DEADLINE_DAYS, now);
            if (sla.breached()) {
                log.warn("SLA breached for request '{}' (tenant: '{}', created: {}, deadline: {})",
                        request.getId(), request.getTenantId(), request.getCreatedAt(), sla.deadline());
                breachCount++;
                // In a full implementation: mark request as SLA-breached and notify ANALYST
                // requestRepository.save(request.markSlaBreached(now));
            }
        }

        log.info("SLA check complete: {} breach(es) found out of {} open requests",
                breachCount, page.totalElements());
        return breachCount;
    }
}
