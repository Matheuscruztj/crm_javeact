package com.atlasops.requests.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for CommentJpaEntity persistence. */
@Repository
public interface SpringDataCommentRepository extends JpaRepository<CommentJpaEntity, String> {

  /**
   * Finds all comments for a given request, ordered by creation time ascending.
   *
   * @param requestId the request identifier
   * @return list of comments ordered by createdAt ascending
   */
  List<CommentJpaEntity> findByRequestIdOrderByCreatedAtAsc(String requestId);
}
