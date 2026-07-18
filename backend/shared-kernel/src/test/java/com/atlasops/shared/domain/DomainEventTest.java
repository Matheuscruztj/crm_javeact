package com.atlasops.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class DomainEventTest {

  static class TestEvent extends DomainEvent {
    TestEvent() {
      super();
    }

    TestEvent(Instant occurredAt, String eventId) {
      super(occurredAt, eventId);
    }
  }

  @Test
  void should_generateEventIdAndTimestamp_when_defaultConstructor() {
    var event = new TestEvent();
    assertThat(event.getEventId()).isNotNull().isNotBlank();
    assertThat(event.getOccurredAt()).isNotNull();
    assertThat(event.getOccurredAt()).isBeforeOrEqualTo(Instant.now());
  }

  @Test
  void should_useProvidedValues_when_parameterizedConstructor() {
    var now = Instant.parse("2024-01-15T10:30:00Z");
    var event = new TestEvent(now, "custom-id");
    assertThat(event.getOccurredAt()).isEqualTo(now);
    assertThat(event.getEventId()).isEqualTo("custom-id");
  }

  @Test
  void should_generateUniqueIds_when_multipleEvents() {
    var event1 = new TestEvent();
    var event2 = new TestEvent();
    assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
  }
}
