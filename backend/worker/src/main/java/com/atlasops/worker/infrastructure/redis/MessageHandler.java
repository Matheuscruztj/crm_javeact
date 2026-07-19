package com.atlasops.worker.infrastructure.redis;

/** Functional interface for handling messages from Redis Streams. */
@FunctionalInterface
public interface MessageHandler {

  /**
   * Handles a message from a Redis Stream.
   *
   * @param message the stream message to process
   * @throws Exception if processing fails (will trigger retry logic)
   */
  void handle(StreamMessage message) throws Exception;
}
