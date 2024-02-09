package org.migor.feedless.service

import org.migor.feedless.data.jpa.models.MailForwardEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.plugins.DiffDataForwaderPlugin
import org.migor.feedless.plugins.MailFormatter
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service


@Service
class MailFormatterService: MailFormatter {

  private val log = LoggerFactory.getLogger(MailFormatterService::class.simpleName)

  override fun prepareMail(
    corrId: String,
    message: MimeMessageHelper,
    webDocument: WebDocumentEntity,
    subscription: SourceSubscriptionEntity,
    params: PluginExecutionParamsInput?
  ) {
    log.info("[$corrId] prepare email")
  }

  override fun prepareWelcomeMail(
    corrId: String,
    message: MimeMessageHelper,
    subscription: SourceSubscriptionEntity,
    mailForward: MailForwardEntity
  ) {
    log.info("[$corrId] prepare welcome email")
  }

}
