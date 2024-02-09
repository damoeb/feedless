package org.migor.feedless.service

import jakarta.mail.internet.MimeMessage
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.data.jpa.models.OneTimePasswordEntity
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.data.jpa.repositories.MailForwardDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.util.*

data class Email(val subject: String, val text: String, val from: String)

@Service
@Profile(AppProfiles.database)
class MailService {
  private val log = LoggerFactory.getLogger(MailService::class.simpleName)

  @Autowired
  private lateinit var javaMailSender: JavaMailSender

  @Autowired
  private lateinit var productService: ProductService

  @Autowired
  private lateinit var mailForwardDAO: MailForwardDAO

  private fun send(to: String, body: Email) {
    val mailMessage = SimpleMailMessage()
    mailMessage.from = body.from
    mailMessage.setTo(to)
    mailMessage.text = body.text
    mailMessage.subject = body.subject

    javaMailSender.send(mailMessage)
  }

  fun sendWelcomeMail(corrId: String, user: UserEntity) {
    log.info("[$corrId] send welcome mail ${user.email}")
    TODO("not implemented")
  }

  fun sendAuthCode(corrId: String, user: UserEntity, otp: OneTimePasswordEntity, description: String) {
    log.info("[$corrId] send auth mail ${user.email}")
    send(user.email, createAuthCodeEmail(corrId, user, otp, description))
  }

  fun getNoReplyAddress(product: ProductName): String {
    return "no-reply@${productService.getDomain(product)}"
  }

  fun send(corrId: String, mimeMessage: MimeMessage) {
    log.info("[$corrId] sending mail $mimeMessage")
    javaMailSender.send(mimeMessage)
  }

  fun createMimeMessage(): MimeMessage {
    return javaMailSender.createMimeMessage()
  }

  private fun createAuthCodeEmail(corrId: String, data: UserEntity, otp: OneTimePasswordEntity, description: String): Email {
    val domain = productService.getDomain(data.product)
    val subject = "$domain: Access Code"
    val text = """
Hi,
you request access to $domain. Please enter the following code (valid until ${otp.validUntil} minutes).


${otp.password}


(${description}, CorrelationId: ${corrId})

""".trimIndent()
    return Email(subject, text, getNoReplyAddress(data.product))
  }

  fun updateMailForwardById(mailForwardId: UUID, authorize: Boolean) {
    mailForwardDAO.findById(mailForwardId).ifPresent {
      it.authorized = authorize
      it.authorizedAt = Date()
      mailForwardDAO.save(it)
    }
  }
}
