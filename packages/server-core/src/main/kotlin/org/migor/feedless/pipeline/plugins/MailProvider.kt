package org.migor.feedless.pipeline.plugins

import jakarta.mail.util.ByteArrayDataSource
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.repository.RepositoryEntity

data class MailAttachment(val id: String, val resource: ByteArrayDataSource, val inline: Boolean = false)

class MailData {
  lateinit var body: String
  lateinit var subject: String
  val attachments: MutableList<MailAttachment> = mutableListOf()
}

interface MailProvider {

  fun provideDocumentMail(
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput
  ): MailData

}
