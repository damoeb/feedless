package org.migor.feedless.mail.gateway

import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.model.message.Message
import org.migor.feedless.mail.MailGateway
import org.migor.feedless.mail.MailGatewayProperties
import org.migor.feedless.mail.OutgoingMail


// see https://github.com/mailgun/mailgun-java
class MailGunGateway(
  private val mailgunMessagesApi: MailgunMessagesApi,
  private val mailGatewayProperties: MailGatewayProperties
) : MailGateway {

  override suspend fun send(mail: OutgoingMail) {
    val message: Message = Message.builder()
      .from(mailGatewayProperties.from)
      .to(mail.to)
      .subject(mail.subject)
      .html(mail.htmlContent)
      .build()

    val response = mailgunMessagesApi.sendMessage(mailGatewayProperties.domain, message)
    response.message
  }

}
