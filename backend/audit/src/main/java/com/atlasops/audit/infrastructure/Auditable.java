package com.atlasops.audit.infrastructure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as auditable. The AuditAspect will intercept methods annotated with this
 * annotation and automatically create audit entries.
 *
 * <p>Critical actions that should be annotated: login, create customer, upload document, approval
 * decision, role change, tenant deactivation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

  /**
   * The action type to record in the audit entry.
   *
   * @return action type string (e.g., "LOGIN", "CREATE_CUSTOMER")
   */
  String actionType();

  /**
   * The entity type affected by this action.
   *
   * @return entity type string (e.g., "USER", "CUSTOMER", "DOCUMENT")
   */
  String entityType();
}
