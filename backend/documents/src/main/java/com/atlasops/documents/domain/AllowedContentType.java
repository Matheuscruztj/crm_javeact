package com.atlasops.documents.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enum representing the allowed content types for document uploads. Each value maps to a standard
 * MIME type.
 */
public enum AllowedContentType {
  PDF("application/pdf"),
  DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
  PNG("image/png"),
  JPEG("image/jpeg");

  private final String mimeType;

  AllowedContentType(String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * Returns the MIME type string for this content type.
   *
   * @return the MIME type (e.g., "application/pdf")
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Resolves an AllowedContentType from a MIME type string.
   *
   * @param mimeType the MIME type to look up
   * @return an Optional containing the matching AllowedContentType, or empty if not supported
   */
  public static Optional<AllowedContentType> fromMimeType(String mimeType) {
    if (mimeType == null || mimeType.isBlank()) {
      return Optional.empty();
    }
    return Arrays.stream(values())
        .filter(type -> type.mimeType.equalsIgnoreCase(mimeType.trim()))
        .findFirst();
  }

  /**
   * Checks if the given MIME type is in the allowed list.
   *
   * @param mimeType the MIME type to check
   * @return true if the MIME type is supported
   */
  public static boolean isSupported(String mimeType) {
    return fromMimeType(mimeType).isPresent();
  }

  /**
   * Returns a comma-separated list of all supported MIME types.
   *
   * @return human-readable list of supported content types
   */
  public static String supportedMimeTypes() {
    return String.join(", ", Arrays.stream(values()).map(AllowedContentType::getMimeType).toList());
  }
}
