package com.atlasops.notifications.presentation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request body for marking notifications as read.
 *
 * @param ids the list of notification IDs to mark as read (1-100 IDs)
 */
public record MarkNotificationsReadRequest(
    @NotEmpty(message = "IDs list must not be empty")
        @Size(max = 100, message = "Cannot mark more than 100 notifications at once")
        List<String> ids) {}
