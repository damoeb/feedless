package org.migor.feedless.mail

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter


abstract class FtlTemplate<T>(val templateName: String) {
  abstract val params: T
}

data class WelcomeMailParams(val productName: String)

data class WelcomePaidMailTemplate(override val params: WelcomeMailParams) :
  FtlTemplate<WelcomeMailParams>("mail-welcome-paid")

data class AuthCodeMailParams(
  val domain: String,
  val codeValidUntil: String,
  val code: String,
  val description: String,
  val corrId: String
)

data class AuthCodeMailTemplate(override val params: AuthCodeMailParams) :
  FtlTemplate<AuthCodeMailParams>("mail-auth-code")

data class VisualDiffChangeDetectedParams(
  val trackerTitle: String,
  val website: String,
  val inlineImages: String
)

data class VisualDiffChangeDetectedMailTemplate(override val params: VisualDiffChangeDetectedParams) :
  FtlTemplate<VisualDiffChangeDetectedParams>("mail-visual-diff-change-detected")

data class VisualDiffWelcomeParams(
  val trackerTitle: String,
  val website: String,
  val trackerInfo: String,
  val activateTrackerMailsUrl: String,
  val info: String
)

data class VisualDiffWelcomeMailTemplate(override val params: VisualDiffWelcomeParams) :
  FtlTemplate<VisualDiffWelcomeParams>("mail-visual-diff-welcome")

class ChangeTrackerAuthorizedTemplate(override val params: Unit = Unit) : FtlTemplate<Unit>("page-tracker-authorized")

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.mail} & ${AppLayer.service}")
class TemplateService(
  private val freemarkerConfigurer: FreeMarkerConfigurer
) {

  private val log = LoggerFactory.getLogger(TemplateService::class.simpleName)

  fun <T> renderTemplate(
    template: FtlTemplate<T>,
  ): String {
    val templateName = template.templateName
    log.debug("renderTemplate $templateName")
    return ByteArrayOutputStream().use {
      freemarkerConfigurer.configuration.getTemplate("$templateName.ftl.html")
        .process(template.params, OutputStreamWriter(it))
      String(it.toByteArray())
    }
  }
}
