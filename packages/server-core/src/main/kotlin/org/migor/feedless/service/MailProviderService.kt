package org.migor.feedless.service

import org.migor.feedless.data.jpa.models.MailForwardEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.plugins.MailData
import org.migor.feedless.plugins.MailProvider
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

  override fun provideWebDocumentMail(
    corrId: String,
    webDocument: WebDocumentEntity,
    subscription: SourceSubscriptionEntity,
    params: PluginExecutionParamsInput
  ): MailData {
    log.info("[$corrId] prepare email")

    val mailData = MailData()
    mailData.subject = "Change"
    mailData.body = templateService.renderTemplate(corrId, WebDocumentUpdateMailTemplate())
    return mailData
  }

  override fun provideWelcomeMail(
    corrId: String,
    subscription: SourceSubscriptionEntity,
    mailForward: MailForwardEntity
  ): MailData {
    log.info("[$corrId] prepare welcome email")
    val mailData = MailData()
    mailData.subject = "Welcome to ${productService.getDomain(subscription.product)}"
    val params = WelcomeMailParams(
      productName = subscription.product.name
    )
    mailData.body = templateService.renderTemplate(corrId, WelcomeFreeMailTemplate(params))
    return mailData
  }
}
