package org.migor.feedless.mail

interface MailGateway {

  suspend fun send(mail: OutgoingMail)
}
