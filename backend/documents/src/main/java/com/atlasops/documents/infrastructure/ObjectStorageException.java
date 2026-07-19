package com.atlasops.documents.infrastructure;

/** Runtime exception thrown when object storage operations fail. */
public class ObjectStorageException extends RuntimeException {

  public ObjectStorageException(String message, Throwable cause) {
    super(message, cause);
  }

  public ObjectStorageException(String message) {
    super(message);
  }
}
