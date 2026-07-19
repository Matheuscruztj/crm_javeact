package com.atlasops.ai.infrastructure;

import com.atlasops.ai.domain.ports.TextExtractionPort;
import java.io.InputStream;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Apache Tika-based implementation of TextExtractionPort.
 * Supports PDF, DOCX, DOC, XLSX, PPTX, TXT, HTML, RTF, images (OCR when Tesseract is available),
 * and 1000+ other formats.
 *
 * <p>Validates: Requirements 4.5 (Real Text Extraction)
 */
@Component
public class TikaTextExtractionAdapter implements TextExtractionPort {

  private static final Logger log = LoggerFactory.getLogger(TikaTextExtractionAdapter.class);
  private static final int MAX_CONTENT_LENGTH = 10_000_000; // 10MB text limit

  private final Tika tika;

  public TikaTextExtractionAdapter() {
    this.tika = new Tika();
    this.tika.setMaxStringLength(MAX_CONTENT_LENGTH);
  }

  @Override
  public TextExtractionResult extract(InputStream inputStream, String contentType, String fileName) {
    if (inputStream == null) {
      return TextExtractionResult.failure("Input stream is null");
    }

    try {
      Metadata metadata = new Metadata();
      if (contentType != null && !contentType.isBlank()) {
        metadata.set(Metadata.CONTENT_TYPE, contentType);
      }
      if (fileName != null && !fileName.isBlank()) {
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);
      }

      String extractedText = tika.parseToString(inputStream, metadata);

      if (extractedText == null || extractedText.isBlank()) {
        log.info("No text extracted from document: {}", fileName);
        return TextExtractionResult.failure("No extractable text found in document");
      }

      // Normalize whitespace
      String normalizedText = extractedText.strip();

      log.info(
          "Successfully extracted {} characters from document: {}",
          normalizedText.length(),
          fileName);

      return TextExtractionResult.success(normalizedText);
    } catch (Exception e) {
      log.error("Failed to extract text from document {}: {}", fileName, e.getMessage());
      return TextExtractionResult.failure("Text extraction failed: " + e.getMessage());
    }
  }
}
