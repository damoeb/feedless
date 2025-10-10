package org.migor.feedless.mail.gateway

import jakarta.mail.internet.MimeMessage
import org.migor.feedless.mail.MailGateway
import org.migor.feedless.mail.MailGatewayProperties
import org.migor.feedless.mail.MailService
import org.migor.feedless.mail.OutgoingMail
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper


class NativeMailGateway(
  private val javaMailSender: JavaMailSender,
  private val mailGatewayProperties: MailGatewayProperties
) : MailGateway {
  private val log = LoggerFactory.getLogger(MailService::class.simpleName)

  override suspend fun send(mail: OutgoingMail) {
    val message: MimeMessage = javaMailSender.createMimeMessage()
    val helper = MimeMessageHelper(message, true)

    helper.setFrom(mailGatewayProperties.from)
    helper.setTo(mail.to.toTypedArray())
    helper.setSubject(mail.subject)
    helper.setText(mail.htmlContent, true)

    javaMailSender.send(message)
  }
}
