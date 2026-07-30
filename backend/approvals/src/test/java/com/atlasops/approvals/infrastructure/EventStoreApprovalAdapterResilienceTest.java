package com.atlasops.approvals.infrastructure;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventStoreApprovalAdapterResilienceTest {

  @Test
  @DisplayName("should_ignoreEvent_when_eventStoreAdapterIsInvoked")
  void should_ignoreEvent_when_eventStoreAdapterIsInvoked() {
    EventStoreApprovalAdapter adapter = new EventStoreApprovalAdapter();

    assertThatCode(() -> adapter.appendEvent("approval-001", new Object()))
        .doesNotThrowAnyException();
  }
}
