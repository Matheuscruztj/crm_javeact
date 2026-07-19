package com.atlasops.notifications.infrastructure;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for NotificationJpaEntity persistence. All queries enforce user and
 * tenant isolation.
 */
@Repository
public interface SpringDataNotificationRepository
    extends JpaRepository<NotificationJpaEntity, String> {

  Page<NotificationJpaEntity> findByRecipientUserIdAndTenantIdOrderByCreatedAtDesc(
      String recipientUserId, String tenantId, Pageable pageable);

  long countByRecipientUserIdAndTenantIdAndReadFalse(String recipientUserId, String tenantId);

  @Modifying
  @Query(
      "UPDATE NotificationJpaEntity n SET n.read = true, n.updatedAt = CURRENT_TIMESTAMP "
          + "WHERE n.id IN :ids AND n.recipientUserId = :recipientUserId "
          + "AND n.tenantId = :tenantId AND n.read = false")
  int markAsReadByIdsAndRecipientUserIdAndTenantId(
      @Param("ids") List<String> ids,
      @Param("recipientUserId") String recipientUserId,
      @Param("tenantId") String tenantId);

  List<NotificationJpaEntity> findByIdInAndRecipientUserIdAndTenantId(
      List<String> ids, String recipientUserId, String tenantId);
}
