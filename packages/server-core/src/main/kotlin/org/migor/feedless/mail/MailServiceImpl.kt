package org.migor.feedless.mail

import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.pipeline.plugins.MailAttachment
import org.migor.feedless.pipeline.plugins.MailData
import org.migor.feedless.plan.ProductService
import org.migor.feedless.user.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
@Profile("${AppProfiles.mail} & ${AppLayer.service}")
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

  override suspend fun sendWelcomeWaitListMail(user: UserEntity) {
//    sendWelcomeAnyMail(corrId, user, WelcomeWaitListMailTemplate(WelcomeMailParams(user.subscription!!.product!!.partOf!!.name)))
  }

  override suspend fun sendWelcomePaidMail(user: UserEntity) {
//    sendWelcomeAnyMail(corrId, user, WelcomePaidMailTemplate(WelcomeMailParams(user.subscription!!.product!!.partOf!!.name)))
  }

  override suspend fun sendWelcomeFreeMail(user: UserEntity) {
//    sendWelcomeAnyMail(corrId, user, WelcomeFreeMailTemplate(WelcomeMailParams(user.subscription!!.product!!.partOf!!.name)))
  }

  override suspend fun sendAuthCode(user: UserEntity, otp: OneTimePasswordEntity, description: String) {
//    if (StringUtils.isBlank(user.email)) {
//      throw IllegalArgumentException("Email is not defined")
//    }
//    log.info("[$corrId] send auth mail ${user.email}")
//
//    val from = getNoReplyAddress(user.subscription!!.product!!.partOf!!)
//    val domain = productService.getDomain(user.subscription!!.product!!.partOf!!)
//    val subject = "$domain: Access Code"
//
//    val mailData = MailData()
//    mailData.subject = subject
//    val sdf = SimpleDateFormat("HH:mm")
//
//    val params = AuthCodeMailParams(
//      domain = domain,
//      codeValidUntil = sdf.format(otp.validUntil),
//      code = otp.password,
//      description = description,
//      corrId = corrId,
//    )
//    mailData.body = templateService.renderTemplate(corrId, AuthCodeMailTemplate(params))
//
//    send(corrId, from, to = arrayOf(user.email), mailData)
  }

  override suspend fun getNoReplyAddress(product: Vertical): String {
    return "no-reply@${productService.getDomain(product)}"
  }

  @Deprecated("")
  override suspend fun send(mimeMessage: MimeMessage) {
    log.debug("sending mail $mimeMessage")
    javaMailSender.send(mimeMessage)
  }

  override suspend fun createMimeMessage(): MimeMessage {
    return javaMailSender.createMimeMessage()
  }

  override suspend fun updateMailForwardById(mailForwardId: UUID, authorize: Boolean) {
    withContext(Dispatchers.IO) {
      mailForwardDAO.findById(mailForwardId).orElseThrow()?.let {
        it.authorized = authorize
        it.authorizedAt = LocalDateTime.now()
        mailForwardDAO.save(it)
      }
    }
  }

  override suspend fun send(from: String, to: Array<String>, mailData: MailData) {
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

//  private fun <T> sendWelcomeAnyMail(corrId: String, user: UserEntity, template: FtlTemplate<T>) {
//    log.info("[$corrId] send welcome mail ${user.email} using ${template.templateName}")
//    val product = user.subscription!!.product!!.partOf!!
//
//    val mailData = MailData()
//    mailData.subject = "Welcome to ${productService.getDomain(product)}"
////      val params = WelcomeMailParams(
////        productName = product.name
////      )
//    mailData.body = templateService.renderTemplate(corrId, template)
//    send(corrId, getNoReplyAddress(product), arrayOf(user.email), mailData)
//  }

}
