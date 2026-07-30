package com.atlasops.boot.config;

import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;

class LocalMailConfigTest {

  private final LocalMailConfig config = new LocalMailConfig();

  @Test
  void should_notSendEmails_when_localMailSenderIsUsed() {
    var sender = config.javaMailSender();
    MimeMessage mimeMessage = new MimeMessage((Session) null);
    SimpleMailMessage simpleMessage = new SimpleMailMessage();
    simpleMessage.setTo("user@example.com");

    assertThatCode(() -> sender.send(mimeMessage)).doesNotThrowAnyException();
    assertThatCode(() -> sender.send(simpleMessage)).doesNotThrowAnyException();
    assertThatCode(() -> sender.send(new MimeMessage[] {mimeMessage})).doesNotThrowAnyException();
    assertThatCode(() -> sender.send(new SimpleMailMessage[] {simpleMessage}))
        .doesNotThrowAnyException();
    assertThatCode(() -> sender.send(message -> {})).doesNotThrowAnyException();
    assertThatCode(() -> sender.send(new org.springframework.mail.javamail.MimeMessagePreparator[] {message -> {}}))
        .doesNotThrowAnyException();
  }
}
