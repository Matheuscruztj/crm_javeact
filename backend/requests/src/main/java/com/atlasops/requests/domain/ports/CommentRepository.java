package com.atlasops.requests.domain.ports;

import com.atlasops.requests.domain.Comment;
import java.util.List;

/** Port defining persistence operations for Comment entities. */
public interface CommentRepository {

  /**
   * Persists a comment (insert).
   *
   * @param comment the comment to persist
   * @return the persisted comment
   */
  Comment save(Comment comment);

  /**
   * Finds all comments for a given request, ordered by creation time ascending.
   *
   * @param requestId the request identifier
   * @return the list of comments for the request
   */
  List<Comment> findByRequestId(String requestId);
}
