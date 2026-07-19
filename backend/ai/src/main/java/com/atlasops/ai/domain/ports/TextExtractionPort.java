package com.atlasops.ai.domain.ports;

import java.io.InputStream;

/**
 * Port for extracting text content from binary documents (PDF, DOCX, images, etc.).
 * Implementations should handle various file formats and return plain text.
 */
public interface TextExtractionPort {

  /**
   * Extracts text content from a document input stream.
   *
   * @param inputStream the binary content of the document
   * @param contentType the MIME type of the document (e.g., "application/pdf")
   * @param fileName the original file name (used for format detection fallback)
   * @return the extracted plain text, or empty string if no text could be extracted
   */
  TextExtractionResult extract(InputStream inputStream, String contentType, String fileName);

  /**
   * Result of text extraction.
   *
   * @param text the extracted text content
   * @param charCount number of characters extracted
   * @param success whether extraction completed without errors
   * @param errorMessage error message if extraction failed (null on success)
   */
  record TextExtractionResult(String text, int charCount, boolean success, String errorMessage) {

    public static TextExtractionResult success(String text) {
      return new TextExtractionResult(text, text != null ? text.length() : 0, true, null);
    }

    public static TextExtractionResult failure(String errorMessage) {
      return new TextExtractionResult("", 0, false, errorMessage);
    }
  }
}
