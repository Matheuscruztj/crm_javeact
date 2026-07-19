package com.atlasops.requests.infrastructure;

import com.atlasops.requests.domain.Comment;
import com.atlasops.requests.domain.ports.CommentRepository;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * JPA-based implementation of {@link CommentRepository}. Converts between domain entities and JPA
 * entities.
 */
@Component
public class JpaCommentRepositoryAdapter implements CommentRepository {

  private final SpringDataCommentRepository springDataRepository;

  public JpaCommentRepositoryAdapter(SpringDataCommentRepository springDataRepository) {
    this.springDataRepository = springDataRepository;
  }

  @Override
  public Comment save(Comment comment) {
    CommentJpaEntity entity = toJpaEntity(comment);
    CommentJpaEntity saved = springDataRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public List<Comment> findByRequestId(String requestId) {
    return springDataRepository.findByRequestIdOrderByCreatedAtAsc(requestId).stream()
        .map(this::toDomain)
        .toList();
  }

  private Comment toDomain(CommentJpaEntity entity) {
    return Comment.reconstitute(
        entity.getId(),
        entity.getText(),
        entity.getAuthorId(),
        entity.getRequestId(),
        entity.getCreatedAt());
  }

  private CommentJpaEntity toJpaEntity(Comment comment) {
    return new CommentJpaEntity(
        comment.getId(),
        comment.getText(),
        comment.getAuthorId(),
        comment.getRequestId(),
        comment.getCreatedAt());
  }
}
