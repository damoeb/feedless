package org.migor.feedless.pipeline.plugins

import jakarta.mail.util.ByteArrayDataSource
import org.migor.feedless.data.jpa.models.DocumentEntity
import org.migor.feedless.data.jpa.models.RepositoryEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.mail.MailForwardEntity

data class MailAttachment(val id: String, val resource: ByteArrayDataSource, val inline: Boolean = false)

class MailData {
  lateinit var body: String
  lateinit var subject: String
  val attachments: MutableList<MailAttachment> = mutableListOf()
}

interface MailProvider {

  fun provideDocumentMail(
    corrId: String,
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput
  ): MailData

  fun provideWelcomeMail(
    corrId: String,
    repository: RepositoryEntity,
    mailForward: MailForwardEntity,
  ): MailData

}
