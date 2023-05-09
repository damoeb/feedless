package org.migor.feedless.mail

interface MailService {
  fun send(mail: String, subject: String, text: String)
}
