package org.migor.rich.rss.mail

interface MailService {
  fun send(mail: String, subject: String, text: String)
}
