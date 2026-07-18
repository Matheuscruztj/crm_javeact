package com.atlasops.shared.domain.ports;

import java.time.Instant;

/**
 * Port for obtaining the current time. Allows deterministic testing by injecting a fixed or
 * controlled clock implementation.
 */
public interface Clock {

  Instant now();
}
