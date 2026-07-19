package com.atlasops.ai.domain;

import java.util.Objects;

/** Immutable key-value pair for document analysis extracted fields. */
public record KeyValuePair(String key, String value) {

  public KeyValuePair {
    Objects.requireNonNull(key, "key must not be null");
    if (key.isBlank()) {
      throw new IllegalArgumentException("key must not be blank");
    }
    Objects.requireNonNull(value, "value must not be null");
  }
}
