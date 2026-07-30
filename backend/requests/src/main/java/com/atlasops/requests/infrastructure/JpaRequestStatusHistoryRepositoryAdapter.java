package com.atlasops.requests.infrastructure;

import com.atlasops.requests.domain.RequestStatus;
import com.atlasops.requests.domain.RequestStatusHistory;
import com.atlasops.requests.domain.ports.RequestStatusHistoryRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Component;

/** JPA-backed adapter for request status history persistence. */
@Component
public class JpaRequestStatusHistoryRepositoryAdapter implements RequestStatusHistoryRepository {

  private final SpringDataRequestStatusHistoryRepository historyRepository;
  private final SpringDataServiceRequestRepository serviceRequestRepository;

  public JpaRequestStatusHistoryRepositoryAdapter(
      SpringDataRequestStatusHistoryRepository historyRepository,
      SpringDataServiceRequestRepository serviceRequestRepository) {
    this.historyRepository = historyRepository;
    this.serviceRequestRepository = serviceRequestRepository;
  }

  @Override
  public void save(RequestStatusHistory history) {
    String tenantId =
        serviceRequestRepository
            .findById(history.getRequestId())
            .map(ServiceRequestJpaEntity::getTenantId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Request with id '" + history.getRequestId() + "' not found"));

    historyRepository.save(toJpaEntity(history, tenantId));
  }

  @Override
  public java.util.List<RequestStatusHistory> findByRequestId(String requestId, String tenantId) {
    return historyRepository.findByRequestIdAndTenantIdOrderByOccurredAtAsc(requestId, tenantId)
        .stream()
        .map(this::toDomain)
        .toList();
  }

  private RequestStatusHistoryJpaEntity toJpaEntity(
      RequestStatusHistory history, String tenantId) {
    return new RequestStatusHistoryJpaEntity(
        history.getId(),
        history.getRequestId(),
        tenantId,
        history.getFromStatus() != null ? history.getFromStatus().name() : null,
        history.getToStatus().name(),
        history.getReason(),
        history.getActorId(),
        history.getOccurredAt());
  }

  private RequestStatusHistory toDomain(RequestStatusHistoryJpaEntity entity) {
    return RequestStatusHistory.reconstitute(
        entity.getId(),
        entity.getRequestId(),
        entity.getFromStatus() != null ? RequestStatus.valueOf(entity.getFromStatus()) : null,
        RequestStatus.valueOf(entity.getToStatus()),
        entity.getReason(),
        entity.getActorId(),
        entity.getOccurredAt());
  }
}
