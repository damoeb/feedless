package org.migor.feedless.service

import freemarker.template.Template
import org.migor.feedless.data.jpa.models.MailForwardEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.plugins.MailData
import org.migor.feedless.plugins.MailProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter


@Service
class MailProviderService : MailProvider {

  private val log = LoggerFactory.getLogger(MailProviderService::class.simpleName)

  @Autowired
  private lateinit var freemarkerConfigurer: FreeMarkerConfigurer

  override fun provideWebDocumentMail(
    corrId: String,
    webDocument: WebDocumentEntity,
    subscription: SourceSubscriptionEntity,
    params: PluginExecutionParamsInput
  ): MailData {
    log.info("[$corrId] prepare email")

    val data = mutableMapOf<String, Any>()

    val mailData = MailData()
    mailData.subject = "Change"
    mailData.body = renderTemplate(corrId, "", data)
    return mailData
  }

  override fun provideWelcomeMail(
    corrId: String,
    subscription: SourceSubscriptionEntity,
    mailForward: MailForwardEntity
  ): MailData {
    log.info("[$corrId] prepare welcome email")
    val data = mutableMapOf<String, Any>()
    val mailData = MailData()
    mailData.subject = "Change"
    mailData.body = renderTemplate(corrId, "", data)
    return mailData
  }

  fun renderTemplate(
    corrId: String,
    templateName: String,
    data: Map<String, Any>
  ): String {
    log.info("[$corrId] renderTemplate $templateName")

    val template: Template = freemarkerConfigurer.configuration.getTemplate(templateName)
    return ByteArrayOutputStream().use {
      template.process(data, OutputStreamWriter(it))
      String(it.toByteArray())
    }
  }
}
