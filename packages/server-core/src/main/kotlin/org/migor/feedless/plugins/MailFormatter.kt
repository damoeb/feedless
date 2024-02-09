package org.migor.feedless.plugins

import org.migor.feedless.data.jpa.models.MailForwardEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.springframework.mail.javamail.MimeMessageHelper

interface MailFormatter {

  fun prepareMail(
    corrId: String,
    message: MimeMessageHelper,
    webDocument: WebDocumentEntity,
    subscription: SourceSubscriptionEntity,
    params: PluginExecutionParamsInput?
  )

  fun prepareWelcomeMail(
    corrId: String,
    message: MimeMessageHelper,
    subscription: SourceSubscriptionEntity,
    mailForward: MailForwardEntity,
  )

}
