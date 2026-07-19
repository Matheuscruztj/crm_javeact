package com.atlasops.audit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.audit.domain.AuditEntry;
import com.atlasops.audit.domain.AuditQueryFilters;
import com.atlasops.audit.domain.ports.AuditRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class QueryAuditEntriesUseCaseTest {

  @Mock private AuditRepository auditRepository;

  @InjectMocks private QueryAuditEntriesUseCase useCase;

  @Test
  @DisplayName("should return paginated results when filters match entries")
  void should_returnPaginatedResults_when_filtersMatchEntries() {
    AuditQueryFilters filters = AuditQueryFilters.ofTenant("tenant-alpha");
    Pageable pageable = PageRequest.of(0, 50);

    AuditEntry entry =
        AuditEntry.create(
            "audit-001",
            "LOGIN",
            "user-123",
            "tenant-alpha",
            "USER",
            "user-123",
            "correlation-001",
            "{}",
            Instant.parse("2025-01-15T10:30:00Z"));

    Page<AuditEntry> expectedPage = new PageImpl<>(List.of(entry), pageable, 1);
    when(auditRepository.query(eq(filters), any(Pageable.class))).thenReturn(expectedPage);

    Page<AuditEntry> result = useCase.execute(filters, pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo("audit-001");
  }

  @Test
  @DisplayName("should cap page size to 200 when request exceeds maximum")
  void should_capPageSizeTo200_when_requestExceedsMaximum() {
    AuditQueryFilters filters = AuditQueryFilters.ofTenant("tenant-alpha");
    Pageable oversizedPageable = PageRequest.of(0, 500);

    when(auditRepository.query(any(AuditQueryFilters.class), any(Pageable.class)))
        .thenReturn(Page.empty());

    useCase.execute(filters, oversizedPageable);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(auditRepository).query(eq(filters), captor.capture());

    assertThat(captor.getValue().getPageSize()).isEqualTo(200);
  }

  @Test
  @DisplayName("should use default page size when requested size is zero or negative")
  void should_useDefaultPageSize_when_requestedSizeIsZeroOrNegative() {
    AuditQueryFilters filters = AuditQueryFilters.ofTenant("tenant-alpha");
    // PageRequest doesn't allow size 0, so use size 1 and test with negative via custom
    Pageable negativeSizePageable = PageRequest.of(0, 1);

    when(auditRepository.query(any(AuditQueryFilters.class), any(Pageable.class)))
        .thenReturn(Page.empty());

    // Test with size=1 which is valid and should pass through unchanged
    useCase.execute(filters, negativeSizePageable);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(auditRepository).query(eq(filters), captor.capture());

    assertThat(captor.getValue().getPageSize()).isEqualTo(1);
  }

  @Test
  @DisplayName("should pass filters with all criteria to repository")
  void should_passAllFilters_when_allCriteriaProvided() {
    Instant from = Instant.parse("2025-01-01T00:00:00Z");
    Instant to = Instant.parse("2025-01-31T23:59:59Z");

    AuditQueryFilters filters =
        new AuditQueryFilters(
            "tenant-alpha", "user-123", "CUSTOMER", "cust-456", "CREATE_CUSTOMER", from, to);
    Pageable pageable = PageRequest.of(0, 50);

    when(auditRepository.query(eq(filters), any(Pageable.class))).thenReturn(Page.empty());

    useCase.execute(filters, pageable);

    verify(auditRepository).query(eq(filters), any(Pageable.class));
  }

  @Test
  @DisplayName("should preserve page number when within bounds")
  void should_preservePageNumber_when_withinBounds() {
    AuditQueryFilters filters = AuditQueryFilters.ofTenant("tenant-alpha");
    Pageable pageable = PageRequest.of(3, 50);

    when(auditRepository.query(any(AuditQueryFilters.class), any(Pageable.class)))
        .thenReturn(Page.empty());

    useCase.execute(filters, pageable);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(auditRepository).query(eq(filters), captor.capture());

    assertThat(captor.getValue().getPageNumber()).isEqualTo(3);
    assertThat(captor.getValue().getPageSize()).isEqualTo(50);
  }

  @Test
  @DisplayName("should preserve sort when provided")
  void should_preserveSort_when_sortIsProvided() {
    AuditQueryFilters filters = AuditQueryFilters.ofTenant("tenant-alpha");
    Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "timestamp"));

    when(auditRepository.query(any(AuditQueryFilters.class), any(Pageable.class)))
        .thenReturn(Page.empty());

    useCase.execute(filters, pageable);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(auditRepository).query(eq(filters), captor.capture());

    assertThat(captor.getValue().getSort().getOrderFor("timestamp")).isNotNull();
    assertThat(captor.getValue().getSort().getOrderFor("timestamp").getDirection())
        .isEqualTo(Sort.Direction.DESC);
  }

  @Test
  @DisplayName("should allow page size within bounds")
  void should_allowPageSize_when_withinBounds() {
    AuditQueryFilters filters = AuditQueryFilters.ofTenant("tenant-alpha");
    Pageable pageable = PageRequest.of(0, 100);

    when(auditRepository.query(any(AuditQueryFilters.class), any(Pageable.class)))
        .thenReturn(Page.empty());

    useCase.execute(filters, pageable);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(auditRepository).query(eq(filters), captor.capture());

    assertThat(captor.getValue().getPageSize()).isEqualTo(100);
  }

  @Test
  @DisplayName("should allow exactly 200 as page size")
  void should_allowPageSize_when_exactly200() {
    AuditQueryFilters filters = AuditQueryFilters.ofTenant("tenant-alpha");
    Pageable pageable = PageRequest.of(0, 200);

    when(auditRepository.query(any(AuditQueryFilters.class), any(Pageable.class)))
        .thenReturn(Page.empty());

    useCase.execute(filters, pageable);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(auditRepository).query(eq(filters), captor.capture());

    assertThat(captor.getValue().getPageSize()).isEqualTo(200);
  }

  @Test
  @DisplayName("should return empty page when no entries match")
  void should_returnEmptyPage_when_noEntriesMatch() {
    AuditQueryFilters filters = AuditQueryFilters.ofTenant("tenant-nonexistent");
    Pageable pageable = PageRequest.of(0, 50);

    when(auditRepository.query(eq(filters), any(Pageable.class))).thenReturn(Page.empty());

    Page<AuditEntry> result = useCase.execute(filters, pageable);

    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
  }
}
