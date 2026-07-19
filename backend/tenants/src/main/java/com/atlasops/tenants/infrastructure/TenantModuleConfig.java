package com.atlasops.tenants.infrastructure;

import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import com.atlasops.tenants.application.CreateTenantUseCase;
import com.atlasops.tenants.application.DeactivateTenantUseCase;
import com.atlasops.tenants.application.GetTenantUseCase;
import com.atlasops.tenants.domain.ports.TenantRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring configuration for wiring the Tenants module use cases as beans. */
@Configuration
public class TenantModuleConfig {

  @Bean
  public CreateTenantUseCase createTenantUseCase(
      TenantRepository tenantRepository, IdGenerator idGenerator, Clock clock) {
    return new CreateTenantUseCase(tenantRepository, idGenerator, clock);
  }

  @Bean
  public DeactivateTenantUseCase deactivateTenantUseCase(
      TenantRepository tenantRepository, Clock clock) {
    return new DeactivateTenantUseCase(tenantRepository, clock);
  }

  @Bean
  public GetTenantUseCase getTenantUseCase(TenantRepository tenantRepository) {
    return new GetTenantUseCase(tenantRepository);
  }
}
