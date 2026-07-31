package com.atlasops.boot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import jakarta.mail.internet.MimeMessage;

/**
 * Provides a no-op mail sender for local Docker runs.
 *
 * <p>This keeps the notifications module wired while avoiding a hard dependency on MailHog or any
 * other SMTP server in the local compose stack.
 */
@Configuration
@Profile("local")
public class LocalMailConfig {

  @Bean
  JavaMailSenderImpl javaMailSender() {
    return new NoOpJavaMailSender();
  }

  private static final class NoOpJavaMailSender extends JavaMailSenderImpl {

    private static final Logger log = LoggerFactory.getLogger(NoOpJavaMailSender.class);

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
      log.info("Skipping SMTP send in local profile");
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
      log.info("Skipping SMTP send of {} message(s) in local profile", mimeMessages.length);
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
      log.info("Skipping SMTP preparator send in local profile");
    }

    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
      log.info(
          "Skipping SMTP preparator send of {} message(s) in local profile",
          mimeMessagePreparators.length);
    }

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
      log.info(
          "Skipping SMTP send to {} in local profile",
          (Object) simpleMessage.getTo());
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
      log.info("Skipping SMTP send of {} simple message(s) in local profile", simpleMessages.length);
    }
  }
}
