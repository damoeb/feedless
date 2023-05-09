package org.migor.feedless.mail

import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.mail)
class MailServiceImpl: MailService {
  private val log = LoggerFactory.getLogger(MailServiceImpl::class.simpleName)

  @Autowired
  private lateinit var javaMailSender: JavaMailSender

  override fun send(mail: String, subject: String, text: String) {
    log.info("send mail $mail")
    val mailMessage = SimpleMailMessage()
    mailMessage.from = "admin@feedless.com"
    mailMessage.setTo(mail)
    mailMessage.text = text
    mailMessage.subject = subject
    javaMailSender.send(mailMessage)
  }

//  @Value("\${app.mail.sender:#{null}}")
//  private lateinit var sender: String

}
