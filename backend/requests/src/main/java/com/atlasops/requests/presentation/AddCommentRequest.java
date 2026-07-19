package com.atlasops.requests.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for adding a comment to a service request.
 *
 * @param text the comment text (1-2000 characters)
 * @param authorId the author's user identifier
 */
public record AddCommentRequest(
    @NotBlank(message = "Comment text must not be blank")
        @Size(max = 2000, message = "Comment text must not exceed 2000 characters")
        String text,
    @NotBlank(message = "Author id must not be blank") String authorId) {}
