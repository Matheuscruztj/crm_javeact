package com.atlasops.requests.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRequestStatusHistoryRepository
    extends JpaRepository<RequestStatusHistoryJpaEntity, String> {

  List<RequestStatusHistoryJpaEntity> findByRequestIdAndTenantIdOrderByOccurredAtAsc(
      String requestId, String tenantId);
}
