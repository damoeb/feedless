package org.migor.feedless.service

import jakarta.mail.internet.MimeMessage
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.data.jpa.models.OneTimePasswordEntity
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.data.jpa.repositories.MailForwardDAO
import org.migor.feedless.plugins.MailAttachment
import org.migor.feedless.plugins.MailData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.*

@Service
@Profile("${AppProfiles.database} & ${AppProfiles.mail}")
class MailServiceImpl : MailService {
  private val log = LoggerFactory.getLogger(MailService::class.simpleName)

  @Autowired
  private lateinit var javaMailSender: JavaMailSender

  @Autowired
  private lateinit var productService: ProductService

  @Autowired
  private lateinit var mailForwardDAO: MailForwardDAO

  @Autowired
  private lateinit var templateService: TemplateService

  @Deprecated("")
  private fun send(to: String, body: Email) {
    val mailMessage = SimpleMailMessage()
    mailMessage.from = body.from
    mailMessage.setTo(to)
    mailMessage.text = body.text
    mailMessage.subject = body.subject

    javaMailSender.send(mailMessage)
  }

  override fun sendWelcomeWaitListMail(corrId: String, user: UserEntity) {
    sendWelcomeAnyMail(corrId, user, WelcomeWaitListMailTemplate(WelcomeMailParams(user.product.name)))
  }

  override fun sendWelcomePaidMail(corrId: String, user: UserEntity) {
    sendWelcomeAnyMail(corrId, user, WelcomePaidMailTemplate(WelcomeMailParams(user.product.name)))
  }

  override fun sendWelcomeFreeMail(corrId: String, user: UserEntity) {
    sendWelcomeAnyMail(corrId, user, WelcomeFreeMailTemplate(WelcomeMailParams(user.product.name)))
  }

  override fun sendAuthCode(corrId: String, user: UserEntity, otp: OneTimePasswordEntity, description: String) {
    if (StringUtils.isBlank(user.email)) {
      throw IllegalArgumentException("Email is not defined")
    }
    log.info("[$corrId] send auth mail ${user.email}")

    val from = getNoReplyAddress(user.product)
    val domain = productService.getDomain(user.product)
    val subject = "$domain: Access Code"

    val mailData = MailData()
    mailData.subject = subject
    val sdf = SimpleDateFormat("HH:mm")

    val params = AuthCodeMailParams(
      domain = domain,
      codeValidUntil = sdf.format(otp.validUntil),
      code = otp.password,
      description = description,
      corrId = corrId,
    )
    mailData.body = templateService.renderTemplate(corrId, AuthCodeMailTemplate(params))

    send(corrId, from, to = arrayOf(user.email!!), mailData)
  }

  override fun getNoReplyAddress(product: ProductName): String {
    return "no-reply@${productService.getDomain(product)}"
  }

  @Deprecated("")
  override fun send(corrId: String, mimeMessage: MimeMessage) {
    log.info("[$corrId] sending mail $mimeMessage")
    javaMailSender.send(mimeMessage)
  }

  override fun createMimeMessage(): MimeMessage {
    return javaMailSender.createMimeMessage()
  }

  override fun updateMailForwardById(mailForwardId: UUID, authorize: Boolean) {
    mailForwardDAO.findById(mailForwardId).ifPresent {
      it.authorized = authorize
      it.authorizedAt = Date()
      mailForwardDAO.save(it)
    }
  }

  override fun send(corrId: String, from: String, to: Array<String>, mailData: MailData) {
    val mimeMessage = createMimeMessage()
    val message = MimeMessageHelper(mimeMessage, true, "UTF-8")
    message.setFrom(from)
    message.setTo(to)
    message.setSubject(mailData.subject)
    message.setText(mailData.body, true)
    mailData.attachments.filterTo(ArrayList()) { it: MailAttachment -> it.inline }
      .forEach { inline -> message.addInline(inline.id, inline.resource) }
    mailData.attachments.filterTo(ArrayList()) { it: MailAttachment -> !it.inline }
      .forEach { inline -> message.addAttachment(inline.id, inline.resource) }
    javaMailSender.send(mimeMessage)
  }

  private fun <T> sendWelcomeAnyMail(corrId: String, user: UserEntity, template: FtlTemplate<T>) {
    user.email?.let { email ->
      log.info("[$corrId] send welcome mail ${user.email} using ${template.templateName}")
      val product = user.product

      val mailData = MailData()
      mailData.subject = "Welcome to ${productService.getDomain(product)}"
      val params = WelcomeMailParams(
        productName = product.name
      )
      mailData.body = templateService.renderTemplate(corrId, template)
      send(corrId, getNoReplyAddress(product), arrayOf(email), mailData)
    }
  }

}
