package com.atlasops.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AggregateRootTest {

  static class TestEvent extends DomainEvent {
    TestEvent() {
      super();
    }
  }

  static class TestAggregate extends AggregateRoot<String> {
    TestAggregate(String id) {
      super(id);
    }

    void doSomething() {
      registerEvent(new TestEvent());
    }
  }

  @Test
  void should_haveNoDomainEvents_when_newlyCreated() {
    var aggregate = new TestAggregate("agg-1");
    assertThat(aggregate.getDomainEvents()).isEmpty();
  }

  @Test
  void should_collectDomainEvents_when_registered() {
    var aggregate = new TestAggregate("agg-1");
    aggregate.doSomething();
    aggregate.doSomething();
    assertThat(aggregate.getDomainEvents()).hasSize(2);
  }

  @Test
  void should_clearDomainEvents_when_clearCalled() {
    var aggregate = new TestAggregate("agg-1");
    aggregate.doSomething();
    assertThat(aggregate.getDomainEvents()).hasSize(1);
    aggregate.clearDomainEvents();
    assertThat(aggregate.getDomainEvents()).isEmpty();
  }

  @Test
  void should_returnUnmodifiableList_when_getDomainEvents() {
    var aggregate = new TestAggregate("agg-1");
    aggregate.doSomething();
    var events = aggregate.getDomainEvents();
    assertThatThrownBy(() -> events.add(new TestEvent()))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void should_throwException_when_nullEventRegistered() {
    var aggregate = new TestAggregate("agg-1");
    assertThatThrownBy(() -> aggregate.registerEvent(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null");
  }
}
