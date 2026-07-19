package com.atlasops.search.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.atlasops.search.domain.ports.SearchIndexUpdatePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IndexEntityUseCaseTest {

  private static final String ENTITY_TYPE = "CUSTOMER";
  private static final String ENTITY_ID = "cust-001";
  private static final String CONTENT = "Alpha Corporation alpha@corp.com";
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private SearchIndexUpdatePort searchIndexUpdatePort;

  private IndexEntityUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new IndexEntityUseCase(searchIndexUpdatePort);
  }

  @Nested
  class Execute {

    @Test
    void should_delegateToPort_when_commandIsValid() {
      IndexEntityCommand command =
          new IndexEntityCommand(ENTITY_TYPE, ENTITY_ID, CONTENT, TENANT_ID);

      useCase.execute(command);

      verify(searchIndexUpdatePort).indexEntity(ENTITY_TYPE, ENTITY_ID, CONTENT, TENANT_ID);
    }

    @Test
    void should_throwException_when_commandIsNull() {
      assertThatThrownBy(() -> useCase.execute(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("Command must not be null");
    }

    @Test
    void should_indexWithDifferentEntityTypes_when_requestEntity() {
      IndexEntityCommand command =
          new IndexEntityCommand("REQUEST", "req-001", "Support ticket title", TENANT_ID);

      useCase.execute(command);

      verify(searchIndexUpdatePort)
          .indexEntity("REQUEST", "req-001", "Support ticket title", TENANT_ID);
    }

    @Test
    void should_indexWithDifferentEntityTypes_when_documentEntity() {
      IndexEntityCommand command =
          new IndexEntityCommand("DOCUMENT", "doc-001", "Report.pdf extracted text", TENANT_ID);

      useCase.execute(command);

      verify(searchIndexUpdatePort)
          .indexEntity("DOCUMENT", "doc-001", "Report.pdf extracted text", TENANT_ID);
    }
  }

  @Nested
  class Remove {

    @Test
    void should_delegateRemoveToPort_when_validParameters() {
      useCase.remove(ENTITY_TYPE, ENTITY_ID, TENANT_ID);

      verify(searchIndexUpdatePort).removeEntity(ENTITY_TYPE, ENTITY_ID, TENANT_ID);
    }

    @Test
    void should_throwException_when_entityTypeIsNull() {
      assertThatThrownBy(() -> useCase.remove(null, ENTITY_ID, TENANT_ID))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("EntityType must not be null");
    }

    @Test
    void should_throwException_when_entityIdIsNull() {
      assertThatThrownBy(() -> useCase.remove(ENTITY_TYPE, null, TENANT_ID))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("EntityId must not be null");
    }

    @Test
    void should_throwException_when_tenantIdIsNull() {
      assertThatThrownBy(() -> useCase.remove(ENTITY_TYPE, ENTITY_ID, null))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("TenantId must not be null");
    }
  }

  @Nested
  class CommandValidation {

    @Test
    void should_throwException_when_entityTypeIsBlank() {
      assertThatThrownBy(() -> new IndexEntityCommand(" ", ENTITY_ID, CONTENT, TENANT_ID))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("EntityType must not be blank");
    }

    @Test
    void should_throwException_when_entityIdIsBlank() {
      assertThatThrownBy(() -> new IndexEntityCommand(ENTITY_TYPE, " ", CONTENT, TENANT_ID))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("EntityId must not be blank");
    }

    @Test
    void should_throwException_when_tenantIdIsBlank() {
      assertThatThrownBy(() -> new IndexEntityCommand(ENTITY_TYPE, ENTITY_ID, CONTENT, " "))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("TenantId must not be blank");
    }

    @Test
    void should_throwException_when_entityTypeIsNullInCommand() {
      assertThatThrownBy(() -> new IndexEntityCommand(null, ENTITY_ID, CONTENT, TENANT_ID))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("EntityType must not be null");
    }

    @Test
    void should_throwException_when_contentIsNullInCommand() {
      assertThatThrownBy(() -> new IndexEntityCommand(ENTITY_TYPE, ENTITY_ID, null, TENANT_ID))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("Content must not be null");
    }

    @Test
    void should_allowEmptyContent_when_contentIsEmptyString() {
      // Empty content is valid (e.g., image documents without extracted text)
      IndexEntityCommand command = new IndexEntityCommand(ENTITY_TYPE, ENTITY_ID, "", TENANT_ID);

      useCase.execute(command);

      verify(searchIndexUpdatePort).indexEntity(ENTITY_TYPE, ENTITY_ID, "", TENANT_ID);
    }
  }
}
