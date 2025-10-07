package org.migor.feedless.mail

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.mail} & ${AppLayer.service}")
@ConditionalOnMissingBean(MailService::class)
class NativeMailService : MailService {
  private val log = LoggerFactory.getLogger(MailService::class.simpleName)

  @Autowired
  private lateinit var javaMailSender: JavaMailSender

  @Autowired
  private lateinit var templateService: TemplateService

  private fun send(to: String, body: Email) {
    val mailMessage = SimpleMailMessage()
    mailMessage.from = body.from
    mailMessage.setTo(to)
    mailMessage.text = body.text
    mailMessage.subject = body.subject

    javaMailSender.send(mailMessage)
  }

//  override suspend fun sendWelcomeWaitListMail(user: UserEntity) {
////    sendWelcomeAnyMail(corrId, user, WelcomeWaitListMailTemplate(WelcomeMailParams(user.subscription!!.product!!.partOf!!.name)))
//  }
//
//  override suspend fun sendWelcomePaidMail(user: UserEntity) {
////    sendWelcomeAnyMail(corrId, user, WelcomePaidMailTemplate(WelcomeMailParams(user.subscription!!.product!!.partOf!!.name)))
//  }
//
//  override suspend fun sendWelcomeFreeMail(user: UserEntity) {
////    sendWelcomeAnyMail(corrId, user, WelcomeFreeMailTemplate(WelcomeMailParams(user.subscription!!.product!!.partOf!!.name)))
//  }

  override suspend fun sendAuthCode(user: User, otp: OneTimePassword, description: String) {
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
    return "no-reply@feedless.org"
  }

  override suspend fun send(email: Email) {
    log.debug("sending mail $email")
//  todo  javaMailSender.send(mimeMessage)
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
