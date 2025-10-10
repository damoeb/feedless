package org.migor.feedless.mail

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.template.AuthCodeMailParams
import org.migor.feedless.template.MailTemplateAuthCode
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.User
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
@Profile(AppProfiles.mail)
class MailServiceImpl(
  private val templateService: TemplateService,
  private val mailGateway: MailGateway
) : MailService {
  private val log = LoggerFactory.getLogger(MailService::class.simpleName)

  override suspend fun sendAuthCode(user: User, otp: OneTimePassword, description: String) {
    val corrId = "corrId"
    if (StringUtils.isBlank(user.email)) {
      throw IllegalArgumentException("Email is not defined")
    }
    log.info("send auth mail ${user.email}")

    val subject = "Confirm your login with this code"
    val dtf = DateTimeFormatter.ofPattern("HH:mm")

    val params = AuthCodeMailParams(
      codeValidUntil = otp.validUntil.format(dtf),
      code = otp.password,
      description = description,
      corrId = corrId,
    )

    mailGateway.send(
      OutgoingMail(
        from = "no-reply@localhost",
        to = listOf(user.email),
        htmlContent = templateService.renderTemplate(MailTemplateAuthCode(params)),
        subject = subject,
      )
    )
  }
}
