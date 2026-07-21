package com.atlasops.documents.application;

/**
 * Exception thrown when the actual file checksum does not match the declared checksum. This
 * indicates a failed upload (corrupted file or tampered data).
 */
public class ChecksumMismatchException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ChecksumMismatchException(String message) {
    super(message);
  }
}
