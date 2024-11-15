package org.migor.feedless.mail

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.pipeline.plugins.MailData
import org.migor.feedless.pipeline.plugins.MailProvider
import org.migor.feedless.repository.RepositoryEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service


@Service
@Profile("${AppProfiles.mail} & ${AppLayer.service}")
class MailProviderService(
  private val templateService: TemplateService
) : MailProvider {

  private val log = LoggerFactory.getLogger(MailProviderService::class.simpleName)

  override fun provideDocumentMail(
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput
  ): MailData {
    log.debug("prepare email")

    val mailData = MailData()
    mailData.subject = "Change"
    mailData.body = templateService.renderTemplate(DocumentUpdateMailTemplate())
    return mailData
  }
}
