package com.atlasops.boot.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.DocumentStatus;
import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.shared.domain.events.CustomerDeletionRequestedEvent;
import com.atlasops.shared.domain.events.DocumentDeletionRequestedEvent;
import com.atlasops.shared.domain.ports.DistributedLockPort;
import com.atlasops.shared.domain.ports.DistributedLockPort.LockHandle;
import com.atlasops.shared.domain.ports.EventPublisher;
import com.atlasops.shared.domain.types.TenantId;
import com.atlasops.documents.domain.AllowedContentType;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CrossStoreDeletionServiceTest {

  private EventPublisher eventPublisher;
  private DistributedLockPort lockPort;
  private DocumentRepository documentRepository;
  private CrossStoreDeletionService service;

  @BeforeEach
  void setUp() {
    eventPublisher = mock(EventPublisher.class);
    lockPort = mock(DistributedLockPort.class);
    documentRepository = mock(DocumentRepository.class);
    service = new CrossStoreDeletionService(eventPublisher, lockPort, documentRepository);
  }

  @Test
  void should_publishDocumentDeletionEventAndReleaseLock_whenDocumentHasNoLegalHold() {
    when(lockPort.tryAcquire(any(), any())).thenReturn(Optional.of(new LockHandle("key", "owner", 1L)));
    Document document =
        Document.reconstitute(
            "doc-1",
            new TenantId("tenant-1"),
            "request-1",
            "invoice.pdf",
            AllowedContentType.PDF,
            100L,
            "checksum",
            null,
            DocumentStatus.UPLOADED,
            null,
            Instant.parse("2026-07-27T10:00:00Z"),
            Instant.parse("2026-07-27T10:00:00Z"));
    when(documentRepository.findById("doc-1", "tenant-1")).thenReturn(Optional.of(document));

    service.deleteDocument("doc-1", "tenant-1");

    ArgumentCaptor<DocumentDeletionRequestedEvent> captor =
        ArgumentCaptor.forClass(DocumentDeletionRequestedEvent.class);
    verify(eventPublisher).publish(captor.capture());
    verify(lockPort).release(any());
  }

  @Test
  void should_throwWhenDocumentHasLegalHold() {
    when(lockPort.tryAcquire(any(), any())).thenReturn(Optional.of(new LockHandle("key", "owner", 1L)));
    Document document =
        Document.reconstitute(
            "doc-1",
            new TenantId("tenant-1"),
            "request-1",
            "invoice.pdf",
            AllowedContentType.PDF,
            100L,
            "checksum",
            null,
            DocumentStatus.UPLOADED,
            null,
            Instant.parse("2026-07-27T10:00:00Z"),
            Instant.parse("2026-07-27T10:00:00Z"),
            true,
            Instant.parse("2026-07-27T09:00:00Z"));
    when(documentRepository.findById("doc-1", "tenant-1")).thenReturn(Optional.of(document));

    assertThatThrownBy(() -> service.deleteDocument("doc-1", "tenant-1"))
        .isInstanceOf(IllegalStateException.class);

    verify(lockPort).release(any());
  }

  @Test
  void should_publishCustomerDeletionEvent_whenLockIsAcquired() {
    when(lockPort.tryAcquire(any(), any())).thenReturn(Optional.of(new LockHandle("key", "owner", 1L)));

    service.deleteCustomer("customer-1", "tenant-1");

    ArgumentCaptor<CustomerDeletionRequestedEvent> captor =
        ArgumentCaptor.forClass(CustomerDeletionRequestedEvent.class);
    verify(eventPublisher).publish(captor.capture());
    verify(lockPort).release(any());
  }
}
