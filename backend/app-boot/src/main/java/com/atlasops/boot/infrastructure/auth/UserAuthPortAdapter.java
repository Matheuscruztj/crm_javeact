package com.atlasops.boot.infrastructure.auth;

import com.atlasops.auth.domain.ports.AuthUserPort;
import com.atlasops.users.domain.User;
import com.atlasops.users.domain.ports.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adapter that exposes the users module as the auth module's lookup port.
 *
 * <p>This keeps authentication isolated from persistence details while allowing login and token
 * refresh flows to resolve active users from the shared user repository.
 */
@Component
public class UserAuthPortAdapter implements AuthUserPort {

  private final UserRepository userRepository;

  public UserAuthPortAdapter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<AuthUserData> findByEmailAndTenantId(String email, String tenantId) {
    return userRepository.findByEmailAndTenantId(email, tenantId).map(this::toAuthUserData);
  }

  private AuthUserData toAuthUserData(User user) {
    return new AuthUserData(
        user.getId(),
        user.getEmail(),
        user.getPasswordHash(),
        user.getRole().name(),
        user.getTenantId(),
        user.isActive());
  }
}
