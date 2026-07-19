package com.atlasops.approvals.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for rejecting an approval.
 *
 * @param reason the rejection reason (10-1000 characters)
 */
public record RejectApprovalRequest(
    @NotBlank(message = "Rejection reason must not be blank")
        @Size(
            min = 10,
            max = 1000,
            message = "Rejection reason must be between 10 and 1000 characters")
        String reason) {}
