package com.atlasops.shared.domain.ports;

/**
 * Port for generating unique identifiers. Implementations may use UUID, ULID, or other strategies.
 */
public interface IdGenerator {

  String generate();
}
