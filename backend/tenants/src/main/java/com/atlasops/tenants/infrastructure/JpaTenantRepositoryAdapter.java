package com.atlasops.tenants.infrastructure;

import com.atlasops.tenants.domain.Tenant;
import com.atlasops.tenants.domain.TenantName;
import com.atlasops.tenants.domain.TenantStatus;
import com.atlasops.tenants.domain.ports.TenantRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * JPA-based implementation of {@link TenantRepository}. Converts between domain aggregates and JPA
 * entities.
 */
@Component
public class JpaTenantRepositoryAdapter implements TenantRepository {

  private final SpringDataTenantRepository springDataRepository;

  public JpaTenantRepositoryAdapter(SpringDataTenantRepository springDataRepository) {
    this.springDataRepository = springDataRepository;
  }

  @Override
  public Optional<Tenant> findById(String id) {
    return springDataRepository.findById(id).map(this::toDomain);
  }

  @Override
  public boolean existsByNameIgnoreCase(String name) {
    return springDataRepository.existsByNameIgnoreCase(name);
  }

  @Override
  public Tenant save(Tenant tenant) {
    TenantJpaEntity entity = toJpaEntity(tenant);
    TenantJpaEntity saved = springDataRepository.save(entity);
    return toDomain(saved);
  }

  private Tenant toDomain(TenantJpaEntity entity) {
    return Tenant.reconstitute(
        entity.getId(),
        new TenantName(entity.getName()),
        TenantStatus.valueOf(entity.getStatus()),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  private TenantJpaEntity toJpaEntity(Tenant tenant) {
    return new TenantJpaEntity(
        tenant.getId(),
        tenant.getName().getValue(),
        tenant.getStatus().name(),
        tenant.getCreatedAt(),
        tenant.getUpdatedAt());
  }
}
