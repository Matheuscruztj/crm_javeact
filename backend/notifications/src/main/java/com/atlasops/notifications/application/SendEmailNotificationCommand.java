package com.atlasops.notifications.application;

/**
 * Command to send an email notification.
 *
 * @param to the recipient email address
 * @param subject the email subject line
 * @param body the email body content
 * @param tenantName the name of the tenant for branding context
 */
public record SendEmailNotificationCommand(
    String to, String subject, String body, String tenantName) {}
