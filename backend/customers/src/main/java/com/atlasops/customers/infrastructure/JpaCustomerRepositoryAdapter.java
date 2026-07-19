package com.atlasops.customers.infrastructure;

import com.atlasops.customers.domain.Address;
import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.CustomerStatus;
import com.atlasops.customers.domain.ports.CustomerRepository;
import com.atlasops.shared.domain.types.Email;
import com.atlasops.shared.domain.types.TenantId;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * JPA-based implementation of {@link CustomerRepository}. Converts between domain aggregates and
 * JPA entities. All queries enforce tenant isolation.
 */
@Component
public class JpaCustomerRepositoryAdapter implements CustomerRepository {

  private final SpringDataCustomerRepository springDataRepository;

  public JpaCustomerRepositoryAdapter(SpringDataCustomerRepository springDataRepository) {
    this.springDataRepository = springDataRepository;
  }

  @Override
  public Customer save(Customer customer) {
    CustomerJpaEntity entity = toJpaEntity(customer);
    CustomerJpaEntity saved = springDataRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public Optional<Customer> findById(String id, String tenantId) {
    return springDataRepository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
  }

  @Override
  public boolean existsByEmailAndTenantId(String email, String tenantId) {
    return springDataRepository.existsByEmailIgnoreCaseAndTenantId(email, tenantId);
  }

  @Override
  public Page<Customer> findByTenantId(String tenantId, Pageable pageable) {
    return springDataRepository
        .findByTenantIdOrderByCreatedAtDesc(tenantId, pageable)
        .map(this::toDomain);
  }

  @Override
  public Page<Customer> searchByNameOrEmail(String query, String tenantId, Pageable pageable) {
    return springDataRepository.searchByNameOrEmail(query, tenantId, pageable).map(this::toDomain);
  }

  @Override
  public Page<Customer> findByRadius(
      double latitude, double longitude, double distanceKm, String tenantId, Pageable pageable) {
    return springDataRepository
        .findByRadiusWithHaversine(latitude, longitude, distanceKm, tenantId, pageable)
        .map(this::toDomain);
  }

  private Customer toDomain(CustomerJpaEntity entity) {
    Address address = buildAddress(entity);
    return Customer.reconstitute(
        entity.getId(),
        entity.getName(),
        new Email(entity.getEmail()),
        address,
        CustomerStatus.valueOf(entity.getStatus()),
        new TenantId(entity.getTenantId()),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  private Address buildAddress(CustomerJpaEntity entity) {
    boolean hasAnyField =
        entity.getStreet() != null
            || entity.getCity() != null
            || entity.getState() != null
            || entity.getPostalCode() != null
            || entity.getCountry() != null
            || entity.getLatitude() != null
            || entity.getLongitude() != null;

    if (!hasAnyField) {
      return null;
    }

    return new Address(
        entity.getStreet(),
        entity.getCity(),
        entity.getState(),
        entity.getPostalCode(),
        entity.getCountry(),
        entity.getLatitude(),
        entity.getLongitude());
  }

  private CustomerJpaEntity toJpaEntity(Customer customer) {
    Address address = customer.getAddress();
    return new CustomerJpaEntity(
        customer.getId(),
        customer.getName(),
        customer.getEmail().getValue(),
        address != null ? address.getStreet() : null,
        address != null ? address.getCity() : null,
        address != null ? address.getState() : null,
        address != null ? address.getPostalCode() : null,
        address != null ? address.getCountry() : null,
        address != null ? address.getLatitude() : null,
        address != null ? address.getLongitude() : null,
        customer.getStatus().name(),
        customer.getTenantId().getValue(),
        customer.getCreatedAt(),
        customer.getUpdatedAt());
  }
}
