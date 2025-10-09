package org.migor.feedless.mail

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.template.AuthCodeMailParams
import org.migor.feedless.template.MailTemplateAuthCode
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
@Profile("${AppProfiles.mail} & ${AppLayer.service}")
class NativeMailService : MailService {
  private val log = LoggerFactory.getLogger(MailService::class.simpleName)

  @Autowired
  private lateinit var javaMailSender: JavaMailSender

  @Autowired
  private lateinit var templateService: TemplateService

  override suspend fun sendAuthCode(user: User, otp: OneTimePassword, description: String) {
    val corrId = "corrId"
    if (StringUtils.isBlank(user.email)) {
      throw IllegalArgumentException("Email is not defined")
    }
    log.info("send auth mail ${user.email}")

    val domain = ""
    val subject = "$domain: Access Code"
    val dtf = DateTimeFormatter.ofPattern("HH:mm")

    val params = AuthCodeMailParams(
      domain = domain,
      codeValidUntil = otp.validUntil.format(dtf),
      code = otp.password,
      description = description,
      corrId = corrId,
    )

    send(
      from = "no-reply@localhost",
      to = arrayOf(user.email),
      text = templateService.renderTemplate(MailTemplateAuthCode(params)),
      subject = subject,
    )
  }

  override suspend fun send(email: Email) {

  }

  private suspend fun send(from: String, to: Array<String>, text: String, subject: String) {
    val mailMessage = SimpleMailMessage()
    mailMessage.from = from
    mailMessage.setTo(*to)
    mailMessage.text = text
    mailMessage.subject = subject

    javaMailSender.send(mailMessage)
  }

//  override suspend fun send(from: String, to: Array<String>, mailData: MailData) {
//    val mimeMessage = createMimeMessage()
//    val message = MimeMessageHelper(mimeMessage, true, "UTF-8")
//    message.setFrom(from)
//    message.setTo(to)
//    message.setSubject(mailData.subject)
//    message.setText(mailData.body, true)
//    mailData.attachments.filterTo(ArrayList()) { it: MailAttachment -> it.inline }
//      .forEach { inline -> message.addInline(inline.id, inline.resource) }
//    mailData.attachments.filterTo(ArrayList()) { it: MailAttachment -> !it.inline }
//      .forEach { inline -> message.addAttachment(inline.id, inline.resource) }
//    javaMailSender.send(mimeMessage)
//  }

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
