package org.migor.feedless.plugins

import jakarta.mail.util.ByteArrayDataSource
import org.migor.feedless.data.jpa.models.MailForwardEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput

data class MailAttachment(val id: String, val resource: ByteArrayDataSource, val inline: Boolean = false)

class MailData {
  lateinit var body: String
  lateinit var subject: String
  val attachments: MutableList<MailAttachment> = mutableListOf()
}

interface MailProvider {

  fun provideWebDocumentMail(
    corrId: String,
    webDocument: WebDocumentEntity,
    subscription: SourceSubscriptionEntity,
    params: PluginExecutionParamsInput
  ): MailData

  fun provideWelcomeMail(
    corrId: String,
    subscription: SourceSubscriptionEntity,
    mailForward: MailForwardEntity,
  ): MailData

}
