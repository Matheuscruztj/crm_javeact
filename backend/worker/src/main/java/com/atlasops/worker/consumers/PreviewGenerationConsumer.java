package com.atlasops.worker.consumers;

import com.atlasops.worker.infrastructure.redis.MessageHandler;
import com.atlasops.worker.infrastructure.redis.StreamMessage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Consumer for generating PDF preview images. Generates PNG preview of the first page (max 600x800
 * pixels) for PDF documents.
 *
 * <p>Validates: Requirements 11.6, 11.8
 */
@Component
public class PreviewGenerationConsumer implements MessageHandler {

  private static final Logger log = LoggerFactory.getLogger(PreviewGenerationConsumer.class);
  private static final String STREAM_KEY = "documents.uploaded";
  private static final int MAX_WIDTH = 600;
  private static final int MAX_HEIGHT = 800;
  private static final float DPI = 72f;

  private final S3Client s3Client;
  private final String bucketName;

  public PreviewGenerationConsumer(
      S3Client s3Client,
      @Value("${atlasops.storage.bucket:atlasops-documents}") String bucketName) {
    this.s3Client = s3Client;
    this.bucketName = bucketName;
  }

  public String getStreamKey() {
    return STREAM_KEY;
  }

  @Override
  public void handle(StreamMessage message) throws Exception {
    String documentId = message.getRequired("documentId");
    String tenantId = message.getRequired("tenantId");
    String storagePath = message.getRequired("storagePath");
    String contentType = message.get("contentType");

    // Only process PDF documents
    if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
      log.debug("Skipping preview generation for non-PDF document {}", documentId);
      return;
    }

    try {
      generateAndStorePreview(documentId, tenantId, storagePath);
    } catch (Exception e) {
      // Non-blocking: log and continue
      log.warn("Failed to generate preview for document {}: {}", documentId, e.getMessage());
    }
  }

  private void generateAndStorePreview(String documentId, String tenantId, String storagePath)
      throws Exception {

    log.info("Generating preview for PDF document {}", documentId);

    // Download PDF from S3
    GetObjectRequest getRequest =
        GetObjectRequest.builder().bucket(bucketName).key(storagePath).build();

    byte[] previewBytes;
    try (InputStream inputStream = s3Client.getObject(getRequest);
        PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {

      if (document.getNumberOfPages() == 0) {
        log.warn("Document {} has no pages, skipping preview generation", documentId);
        return;
      }

      PDFRenderer renderer = new PDFRenderer(document);
      BufferedImage image = renderer.renderImageWithDPI(0, DPI);

      // Scale if necessary
      BufferedImage scaledImage = scaleImage(image);

      // Convert to PNG bytes
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        ImageIO.write(scaledImage, "png", baos);
        previewBytes = baos.toByteArray();
      }
    }

    // Store preview in S3
    LocalDate now = LocalDate.now();
    String previewPath =
        String.format(
            "%s/%d/%02d/%s/preview.png", tenantId, now.getYear(), now.getMonthValue(), documentId);

    PutObjectRequest putRequest =
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(previewPath)
            .contentType("image/png")
            .build();

    s3Client.putObject(putRequest, RequestBody.fromBytes(previewBytes));

    log.info("Preview generated and stored for document {} at {}", documentId, previewPath);
  }

  private BufferedImage scaleImage(BufferedImage original) {
    int width = original.getWidth();
    int height = original.getHeight();

    if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
      return original;
    }

    double scaleX = (double) MAX_WIDTH / width;
    double scaleY = (double) MAX_HEIGHT / height;
    double scale = Math.min(scaleX, scaleY);

    int newWidth = (int) (width * scale);
    int newHeight = (int) (height * scale);

    BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
    scaled
        .getGraphics()
        .drawImage(
            original.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH),
            0,
            0,
            null);

    return scaled;
  }
}
