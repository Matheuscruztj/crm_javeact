package com.atlasops.documents.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlasops.documents.application.ChecksumMismatchException;
import com.atlasops.documents.application.ConfirmUploadCommand;
import com.atlasops.documents.application.ConfirmUploadUseCase;
import com.atlasops.documents.application.InitiateUploadCommand;
import com.atlasops.documents.application.InitiateUploadResult;
import com.atlasops.documents.application.InitiateUploadUseCase;
import com.atlasops.documents.application.RegisterDocumentMetadataCommand;
import com.atlasops.documents.application.RegisterDocumentMetadataUseCase;
import com.atlasops.documents.domain.AllowedContentType;
import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.DocumentStatus;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * MockMvc tests for DocumentController.
 *
 * <p>Validates: Requirements 9.1, 10.1, 10.2
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentController")
class DocumentControllerTest {

  private static final String TENANT_ID = "tenant-001";
  private static final String DOCUMENT_ID = "doc-001";
  private static final String CORRELATION_ID = "corr-001";
  private static final String FILENAME = "report.pdf";
  private static final String CONTENT_TYPE = "application/pdf";
  private static final long FILE_SIZE = 1024L;
  private static final String CHECKSUM =
      "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
  private static final String STORAGE_PATH = "tenant-001/2025/01/doc-001/report.pdf";
  private static final String UPLOAD_URL = "https://minio.local/presigned-upload-url";
  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:00:00Z");

  private MockMvc mockMvc;

  @Mock private RegisterDocumentMetadataUseCase registerDocumentMetadataUseCase;

  @Mock private InitiateUploadUseCase initiateUploadUseCase;

  @Mock private ConfirmUploadUseCase confirmUploadUseCase;

  @InjectMocks private DocumentController documentController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(documentController)
            .setControllerAdvice(new TestExceptionHandler())
            .build();
  }

  // --- POST /api/v1/documents (register metadata) ---

  @Test
  void should_return201WithDocument_when_validMetadataRegistered() throws Exception {
    Document document = createPendingDocument();
    when(registerDocumentMetadataUseCase.execute(any(RegisterDocumentMetadataCommand.class)))
        .thenReturn(document);

    mockMvc
        .perform(
            post("/api/v1/documents")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRegisterRequestJson()))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/documents/" + DOCUMENT_ID))
        .andExpect(jsonPath("$.id").value(DOCUMENT_ID))
        .andExpect(jsonPath("$.tenantId").value(TENANT_ID))
        .andExpect(jsonPath("$.filename").value(FILENAME))
        .andExpect(jsonPath("$.contentType").value(CONTENT_TYPE))
        .andExpect(jsonPath("$.fileSize").value(FILE_SIZE))
        .andExpect(jsonPath("$.checksum").value(CHECKSUM))
        .andExpect(jsonPath("$.status").value("PENDING_UPLOAD"));
  }

  @Test
  void should_return400_when_filenameIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/documents")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "filename": "",
                                  "contentType": "application/pdf",
                                  "fileSize": 1024,
                                  "checksum": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
                                }
                                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return400_when_contentTypeIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/documents")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "filename": "report.pdf",
                                  "contentType": "",
                                  "fileSize": 1024,
                                  "checksum": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
                                }
                                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return400_when_checksumInvalid() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/documents")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "filename": "report.pdf",
                                  "contentType": "application/pdf",
                                  "fileSize": 1024,
                                  "checksum": "short"
                                }
                                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return400_when_unsupportedContentType() throws Exception {
    when(registerDocumentMetadataUseCase.execute(any(RegisterDocumentMetadataCommand.class)))
        .thenThrow(
            new IllegalArgumentException(
                "Unsupported content type: text/plain. Supported types: "
                    + AllowedContentType.supportedMimeTypes()));

    mockMvc
        .perform(
            post("/api/v1/documents")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "filename": "readme.txt",
                                  "contentType": "text/plain",
                                  "fileSize": 1024,
                                  "checksum": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
                                }
                                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return400_when_fileSizeNotPositive() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/documents")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "filename": "report.pdf",
                                  "contentType": "application/pdf",
                                  "fileSize": 0,
                                  "checksum": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
                                }
                                """))
        .andExpect(status().isBadRequest());
  }

  // --- POST /api/v1/documents/{id}/upload-url ---

  @Test
  void should_return200WithUploadUrl_when_validDocumentId() throws Exception {
    var result = new InitiateUploadResult(UPLOAD_URL, STORAGE_PATH);
    when(initiateUploadUseCase.execute(any(InitiateUploadCommand.class))).thenReturn(result);

    mockMvc
        .perform(
            post("/api/v1/documents/" + DOCUMENT_ID + "/upload-url")
                .header("X-Tenant-ID", TENANT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uploadUrl").value(UPLOAD_URL))
        .andExpect(jsonPath("$.storagePath").value(STORAGE_PATH));
  }

  @Test
  void should_return404_when_documentNotFoundForUploadUrl() throws Exception {
    when(initiateUploadUseCase.execute(any(InitiateUploadCommand.class)))
        .thenThrow(new ResourceNotFoundException("Document not found: nonexistent"));

    mockMvc
        .perform(post("/api/v1/documents/nonexistent/upload-url").header("X-Tenant-ID", TENANT_ID))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_return422_when_documentNotInPendingUploadStatus() throws Exception {
    when(initiateUploadUseCase.execute(any(InitiateUploadCommand.class)))
        .thenThrow(
            new BusinessRuleViolationException(
                "Document must be in PENDING_UPLOAD status to initiate upload. "
                    + "Current status: UPLOADED"));

    mockMvc
        .perform(
            post("/api/v1/documents/" + DOCUMENT_ID + "/upload-url")
                .header("X-Tenant-ID", TENANT_ID))
        .andExpect(status().isUnprocessableEntity());
  }

  // --- POST /api/v1/documents/{id}/confirm-upload ---

  @Test
  void should_return200WithUpdatedDocument_when_checksumMatches() throws Exception {
    Document uploaded = createUploadedDocument();
    when(confirmUploadUseCase.execute(any(ConfirmUploadCommand.class))).thenReturn(uploaded);

    mockMvc
        .perform(
            post("/api/v1/documents/" + DOCUMENT_ID + "/confirm-upload")
                .header("X-Tenant-ID", TENANT_ID)
                .header("X-Correlation-ID", CORRELATION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"storagePath": "tenant-001/2025/01/doc-001/report.pdf"}
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(DOCUMENT_ID))
        .andExpect(jsonPath("$.status").value("UPLOADED"))
        .andExpect(jsonPath("$.storagePath").value(STORAGE_PATH));
  }

  @Test
  void should_return200_when_correlationIdHeaderMissing() throws Exception {
    Document uploaded = createUploadedDocument();
    when(confirmUploadUseCase.execute(any(ConfirmUploadCommand.class))).thenReturn(uploaded);

    mockMvc
        .perform(
            post("/api/v1/documents/" + DOCUMENT_ID + "/confirm-upload")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"storagePath": "tenant-001/2025/01/doc-001/report.pdf"}
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(DOCUMENT_ID))
        .andExpect(jsonPath("$.status").value("UPLOADED"));
  }

  @Test
  void should_return422_when_checksumMismatch() throws Exception {
    when(confirmUploadUseCase.execute(any(ConfirmUploadCommand.class)))
        .thenThrow(
            new ChecksumMismatchException("Checksum mismatch: declared=abc123, actual=def456"));

    mockMvc
        .perform(
            post("/api/v1/documents/" + DOCUMENT_ID + "/confirm-upload")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"storagePath": "tenant-001/2025/01/doc-001/report.pdf"}
                                """))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void should_return404_when_documentNotFoundForConfirmUpload() throws Exception {
    when(confirmUploadUseCase.execute(any(ConfirmUploadCommand.class)))
        .thenThrow(new ResourceNotFoundException("Document not found: nonexistent"));

    mockMvc
        .perform(
            post("/api/v1/documents/nonexistent/confirm-upload")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"storagePath": "some/path"}
                                """))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_return400_when_storagePathIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/documents/" + DOCUMENT_ID + "/confirm-upload")
                .header("X-Tenant-ID", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"storagePath": ""}
                                """))
        .andExpect(status().isBadRequest());
  }

  // --- Helper methods ---

  private Document createPendingDocument() {
    return Document.reconstitute(
        DOCUMENT_ID,
        new TenantId(TENANT_ID),
        null,
        FILENAME,
        AllowedContentType.PDF,
        FILE_SIZE,
        CHECKSUM,
        null,
        DocumentStatus.PENDING_UPLOAD,
        null,
        FIXED_NOW,
        FIXED_NOW);
  }

  private Document createUploadedDocument() {
    return Document.reconstitute(
        DOCUMENT_ID,
        new TenantId(TENANT_ID),
        null,
        FILENAME,
        AllowedContentType.PDF,
        FILE_SIZE,
        CHECKSUM,
        STORAGE_PATH,
        DocumentStatus.UPLOADED,
        null,
        FIXED_NOW,
        FIXED_NOW);
  }

  private String validRegisterRequestJson() {
    return """
                {
                  "filename": "report.pdf",
                  "contentType": "application/pdf",
                  "fileSize": 1024,
                  "checksum": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
                }
                """;
  }

  /**
   * Minimal exception handler for standalone MockMvc tests, mirroring the GlobalExceptionHandler in
   * app-boot.
   */
  @RestControllerAdvice
  static class TestExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(java.util.Map.of("status", 404, "detail", ex.getMessage()));
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<Object> handleBusinessRule(BusinessRuleViolationException ex) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
          .body(java.util.Map.of("status", 422, "detail", ex.getMessage()));
    }

    @ExceptionHandler(ChecksumMismatchException.class)
    public ResponseEntity<Object> handleChecksumMismatch(ChecksumMismatchException ex) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
          .body(
              java.util.Map.of(
                  "status", 422, "code", "CHECKSUM_MISMATCH", "detail", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleBadRequest(IllegalArgumentException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(java.util.Map.of("status", 400, "detail", ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(
        org.springframework.web.bind.MethodArgumentNotValidException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(java.util.Map.of("status", 400, "detail", "Validation failed"));
    }
  }
}
