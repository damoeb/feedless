package org.migor.rich.rss.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service


@Service
@Profile("mail", "auth")
class MailService {
  private val log = LoggerFactory.getLogger(MailService::class.simpleName)

  @Autowired(required = false)
  private lateinit var javaMailSender: JavaMailSender

//  @Value("\${app.mail.sender:#{null}}")
//  private lateinit var sender: String
//
//  fun sendSimpleMail(to: String, subject: String, text: String) {
//    val mailMessage = SimpleMailMessage()
//    mailMessage.setFrom(sender)
//    mailMessage.setTo(to)
//    mailMessage.setText(text)
//    mailMessage.setSubject(subject)
//    javaMailSender.send(mailMessage)
//  }

}
