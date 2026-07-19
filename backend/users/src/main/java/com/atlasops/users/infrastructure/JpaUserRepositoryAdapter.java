package com.atlasops.users.infrastructure;

import com.atlasops.users.domain.User;
import com.atlasops.users.domain.UserRole;
import com.atlasops.users.domain.UserStatus;
import com.atlasops.users.domain.ports.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter implementing the UserRepository domain port. Provides case-insensitive email queries
 * filtered by tenant.
 */
@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

  private final UserSpringDataRepository springDataRepository;

  public JpaUserRepositoryAdapter(UserSpringDataRepository springDataRepository) {
    this.springDataRepository = springDataRepository;
  }

  @Override
  public Optional<User> findById(String id) {
    return springDataRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<User> findByEmailAndTenantId(String email, String tenantId) {
    return springDataRepository
        .findByEmailIgnoreCaseAndTenantId(email, tenantId)
        .map(this::toDomain);
  }

  @Override
  public boolean existsByEmailAndTenantId(String email, String tenantId) {
    return springDataRepository.existsByEmailIgnoreCaseAndTenantId(email, tenantId);
  }

  @Override
  public User save(User user) {
    UserJpaEntity entity = toEntity(user);
    UserJpaEntity saved = springDataRepository.save(entity);
    return toDomain(saved);
  }

  private User toDomain(UserJpaEntity entity) {
    return User.reconstitute(
        entity.getId(),
        entity.getEmail(),
        entity.getName(),
        entity.getPasswordHash(),
        UserRole.valueOf(entity.getRole()),
        entity.getTenantId(),
        UserStatus.valueOf(entity.getStatus()),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  private UserJpaEntity toEntity(User user) {
    return new UserJpaEntity(
        user.getId(),
        user.getTenantId(),
        user.getEmail(),
        user.getName(),
        user.getPasswordHash(),
        user.getRole().name(),
        user.getStatus().name(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}
