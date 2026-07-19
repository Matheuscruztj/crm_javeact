package com.atlasops.users.application;

/**
 * Command for deactivating a user.
 *
 * @param userId the user to deactivate
 * @param requestingUserId the user performing the deactivation (to prevent self-deactivation)
 */
public record DeactivateUserCommand(String userId, String requestingUserId) {}
