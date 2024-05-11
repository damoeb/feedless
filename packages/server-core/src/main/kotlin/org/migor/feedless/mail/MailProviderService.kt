package org.migor.feedless.mail

import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.pipeline.plugins.MailData
import org.migor.feedless.pipeline.plugins.MailProvider
import org.migor.feedless.plan.ProductService
import org.migor.feedless.repository.RepositoryEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class MailProviderService : MailProvider {

  private val log = LoggerFactory.getLogger(MailProviderService::class.simpleName)

  @Autowired
  private lateinit var templateService: TemplateService

  @Autowired
  private lateinit var productService: ProductService

  override fun provideDocumentMail(
    corrId: String,
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput
  ): MailData {
    log.info("[$corrId] prepare email")

    val mailData = MailData()
    mailData.subject = "Change"
    mailData.body = templateService.renderTemplate(corrId, DocumentUpdateMailTemplate())
    return mailData
  }

  override fun provideWelcomeMail(
      corrId: String,
      repository: RepositoryEntity,
      mailForward: MailForwardEntity
  ): MailData {
    log.info("[$corrId] prepare welcome email")
    val mailData = MailData()
    mailData.subject = "Welcome to ${productService.getDomain(repository.product)}"
    val params = WelcomeMailParams(
      productName = repository.product.name
    )
    mailData.body = templateService.renderTemplate(corrId, WelcomeFreeMailTemplate(params))
    return mailData
  }
}
