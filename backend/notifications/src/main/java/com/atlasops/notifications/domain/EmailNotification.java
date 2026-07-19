package com.atlasops.notifications.domain;

import java.util.Objects;

/**
 * Value object representing an email notification to be sent. Contains all information needed to
 * compose and send an email message.
 *
 * @param to the recipient email address
 * @param subject the email subject line
 * @param body the email body content
 * @param tenantName the name of the tenant for branding context
 */
public record EmailNotification(String to, String subject, String body, String tenantName) {

  public EmailNotification {
    Objects.requireNonNull(to, "Email 'to' must not be null");
    Objects.requireNonNull(subject, "Email 'subject' must not be null");
    Objects.requireNonNull(body, "Email 'body' must not be null");
    Objects.requireNonNull(tenantName, "Email 'tenantName' must not be null");

    if (to.isBlank()) {
      throw new IllegalArgumentException("Email 'to' must not be blank");
    }
    if (subject.isBlank()) {
      throw new IllegalArgumentException("Email 'subject' must not be blank");
    }
    if (body.isBlank()) {
      throw new IllegalArgumentException("Email 'body' must not be blank");
    }
    if (tenantName.isBlank()) {
      throw new IllegalArgumentException("Email 'tenantName' must not be blank");
    }
  }
}
