package com.atlasops.requests.infrastructure;

import com.atlasops.requests.domain.RequestPriority;
import com.atlasops.requests.domain.RequestStatus;
import com.atlasops.requests.domain.ServiceRequest;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * JPA-based implementation of {@link ServiceRequestRepository}. Converts between domain aggregates
 * and JPA entities. All queries enforce tenant isolation.
 */
@Component
public class JpaServiceRequestRepositoryAdapter implements ServiceRequestRepository {

  private final SpringDataServiceRequestRepository springDataRepository;

  public JpaServiceRequestRepositoryAdapter(
      SpringDataServiceRequestRepository springDataRepository) {
    this.springDataRepository = springDataRepository;
  }

  @Override
  public Optional<ServiceRequest> findByIdAndTenantId(String id, String tenantId) {
    return springDataRepository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
  }

  @Override
  public ServiceRequestPage findAllByTenantId(
      String tenantId,
      RequestStatus status,
      RequestPriority priority,
      String customerId,
      int page,
      int size) {
    String statusFilter = status != null ? status.name() : null;
    String priorityFilter = priority != null ? priority.name() : null;

    Page<ServiceRequestJpaEntity> jpaPage =
        springDataRepository.findAllByTenantIdWithFilters(
            tenantId, statusFilter, priorityFilter, customerId, PageRequest.of(page, size));

    return new ServiceRequestPage(
        jpaPage.getContent().stream().map(this::toDomain).toList(),
        jpaPage.getNumber(),
        jpaPage.getSize(),
        jpaPage.getTotalElements(),
        jpaPage.getTotalPages());
  }

  @Override
  public ServiceRequest save(ServiceRequest request) {
    ServiceRequestJpaEntity entity = toJpaEntity(request);
    ServiceRequestJpaEntity saved = springDataRepository.save(entity);
    return toDomain(saved);
  }

  private ServiceRequest toDomain(ServiceRequestJpaEntity entity) {
    return ServiceRequest.reconstitute(
        entity.getId(),
        entity.getTitle(),
        entity.getDescription(),
        RequestStatus.valueOf(entity.getStatus()),
        RequestPriority.valueOf(entity.getPriority()),
        entity.getCustomerId(),
        entity.getAssignedAnalystId(),
        entity.getTenantId(),
        entity.getCreatedAt(),
        entity.getAssignedAt(),
        entity.getDocumentIds());
  }

  private ServiceRequestJpaEntity toJpaEntity(ServiceRequest request) {
    return new ServiceRequestJpaEntity(
        request.getId(),
        request.getTitle(),
        request.getDescription(),
        request.getStatus().name(),
        request.getPriority().name(),
        request.getCustomerId(),
        request.getAssignedAnalystId(),
        request.getTenantId(),
        request.getCreatedAt(),
        request.getAssignedAt(),
        request.getDocumentIds());
  }
}
