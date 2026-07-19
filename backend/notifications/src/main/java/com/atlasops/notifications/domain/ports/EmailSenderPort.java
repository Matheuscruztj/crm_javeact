package com.atlasops.notifications.domain.ports;

import com.atlasops.notifications.domain.EmailNotification;

/**
 * Port defining the contract for email delivery. Implementations handle the actual sending of
 * emails via SMTP or other transports.
 */
public interface EmailSenderPort {

  /**
   * Sends an email notification.
   *
   * @param emailNotification the email notification containing recipient, subject, body, and tenant
   *     name
   */
  void sendEmail(EmailNotification emailNotification);
}
