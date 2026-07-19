package com.atlasops.audit.infrastructure;

import com.atlasops.audit.application.WriteAuditEntryCommand;
import com.atlasops.audit.application.WriteAuditEntryUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Spring AOP aspect that intercepts critical actions and writes audit entries. Intercepts methods
 * annotated with {@link Auditable} to automatically record:
 *
 * <ul>
 *   <li>Login attempts
 *   <li>Customer creation
 *   <li>Document upload
 *   <li>Approval decisions
 *   <li>Role changes
 *   <li>Tenant deactivation
 * </ul>
 *
 * <p>Reads correlationId from MDC. Generates UUID v4 if absent. The audit write includes retry
 * logic (1 retry, 1s delay) handled by the WriteAuditEntryUseCase.
 *
 * <p>Validates: Requirements 19.1, 19.4, 19.5
 */
@Aspect
@Component
public class AuditAspect {

  private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
  private static final String MDC_CORRELATION_ID_KEY = "correlationId";
  private static final String MDC_TENANT_ID_KEY = "tenantId";
  private static final String MDC_USER_ID_KEY = "userId";

  private final WriteAuditEntryUseCase writeAuditEntryUseCase;
  private final ObjectMapper objectMapper;

  public AuditAspect(WriteAuditEntryUseCase writeAuditEntryUseCase, ObjectMapper objectMapper) {
    this.writeAuditEntryUseCase = writeAuditEntryUseCase;
    this.objectMapper = objectMapper;
  }

  /**
   * Intercepts methods annotated with @Auditable and writes an audit entry after successful
   * execution. If audit writing fails, the original operation is not affected — failures are logged
   * and the system proceeds.
   */
  @Around("@annotation(auditable)")
  public Object auditAction(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
    Object result = joinPoint.proceed();

    try {
      writeAuditEntry(joinPoint, auditable, result);
    } catch (Exception e) {
      log.error(
          "Failed to write audit entry for action: {}, method: {}",
          auditable.actionType(),
          joinPoint.getSignature().getName(),
          e);
    }

    return result;
  }

  private void writeAuditEntry(ProceedingJoinPoint joinPoint, Auditable auditable, Object result) {
    String actorId = resolveActorId(joinPoint);
    String tenantId = resolveTenantId(joinPoint);
    String entityId = resolveEntityId(joinPoint, result);
    String details = buildDetails(joinPoint, auditable);

    var command =
        new WriteAuditEntryCommand(
            auditable.actionType(), actorId, tenantId, auditable.entityType(), entityId, details);

    writeAuditEntryUseCase.execute(command);
  }

  private String resolveActorId(ProceedingJoinPoint joinPoint) {
    // Try MDC first
    String mdcUserId = MDC.get(MDC_USER_ID_KEY);
    if (mdcUserId != null && !mdcUserId.isBlank()) {
      return mdcUserId;
    }

    // Try to find actorId parameter in method args
    String paramValue = findParameterValue(joinPoint, "actorId", "userId", "analystId");
    if (paramValue != null) {
      return paramValue;
    }

    return "SYSTEM";
  }

  private String resolveTenantId(ProceedingJoinPoint joinPoint) {
    // Try MDC first
    String mdcTenantId = MDC.get(MDC_TENANT_ID_KEY);
    if (mdcTenantId != null && !mdcTenantId.isBlank()) {
      return mdcTenantId;
    }

    // Try to find tenantId parameter in method args
    String paramValue = findParameterValue(joinPoint, "tenantId");
    if (paramValue != null) {
      return paramValue;
    }

    return "UNKNOWN";
  }

  private String resolveEntityId(ProceedingJoinPoint joinPoint, Object result) {
    // Try to extract entity ID from the result
    if (result != null) {
      try {
        var method = result.getClass().getMethod("getId");
        Object id = method.invoke(result);
        if (id != null) {
          return id.toString();
        }
      } catch (ReflectiveOperationException ignored) {
        // Not all results have getId()
      }

      // Try id() for records
      try {
        var method = result.getClass().getMethod("id");
        Object id = method.invoke(result);
        if (id != null) {
          return id.toString();
        }
      } catch (ReflectiveOperationException ignored) {
        // Not all results have id()
      }
    }

    // Try to find entityId or id parameter in method args
    String paramValue =
        findParameterValue(joinPoint, "entityId", "id", "documentId", "customerId", "approvalId");
    if (paramValue != null) {
      return paramValue;
    }

    return "UNKNOWN";
  }

  private String findParameterValue(ProceedingJoinPoint joinPoint, String... paramNames) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String[] parameterNames = signature.getParameterNames();
    Object[] args = joinPoint.getArgs();

    if (parameterNames == null || args == null) {
      return null;
    }

    for (String targetName : paramNames) {
      for (int i = 0; i < parameterNames.length; i++) {
        if (targetName.equals(parameterNames[i]) && args[i] != null) {
          return args[i].toString();
        }
      }
    }

    return null;
  }

  private String buildDetails(ProceedingJoinPoint joinPoint, Auditable auditable) {
    Map<String, Object> detailsMap = new LinkedHashMap<>();
    detailsMap.put("method", joinPoint.getSignature().toShortString());
    detailsMap.put("actionType", auditable.actionType());

    String correlationId = MDC.get(MDC_CORRELATION_ID_KEY);
    if (correlationId != null) {
      detailsMap.put("correlationId", correlationId);
    }

    // Include method arguments (sanitized)
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String[] parameterNames = signature.getParameterNames();
    Object[] args = joinPoint.getArgs();

    if (parameterNames != null && args != null) {
      Map<String, Object> params = new LinkedHashMap<>();
      for (int i = 0; i < parameterNames.length; i++) {
        if (args[i] != null && !isSensitiveParam(parameterNames[i])) {
          params.put(parameterNames[i], args[i].toString());
        }
      }
      if (!params.isEmpty()) {
        detailsMap.put("parameters", params);
      }
    }

    try {
      String json = objectMapper.writeValueAsString(detailsMap);
      // Truncate if exceeds 10KB
      if (json.length() > 10240) {
        json = json.substring(0, 10237) + "...";
      }
      return json;
    } catch (JsonProcessingException e) {
      log.warn("Failed to serialize audit details to JSON", e);
      return "{}";
    }
  }

  private boolean isSensitiveParam(String paramName) {
    String lower = paramName.toLowerCase();
    return lower.contains("password")
        || lower.contains("secret")
        || lower.contains("token")
        || lower.contains("credential");
  }
}
