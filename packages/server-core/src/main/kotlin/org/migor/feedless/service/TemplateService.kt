package org.migor.feedless.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter


abstract class FtlTemplate<T>(val templateName: String) {
  abstract val params: T
}

data class WelcomeMailParams(val productName: String)
data class WelcomeFreeMailTemplate(override val params: WelcomeMailParams) :
  FtlTemplate<WelcomeMailParams>("mail-welcome-free")

data class WelcomePaidMailTemplate(override val params: WelcomeMailParams) :
  FtlTemplate<WelcomeMailParams>("mail-welcome-paid")

data class WelcomeWaitListMailTemplate(override val params: WelcomeMailParams) :
  FtlTemplate<WelcomeMailParams>("mail-welcome-wait-list")

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

class MailTrackerAuthorizedTemplate(override val params: Unit = Unit) : FtlTemplate<Unit>("page-tracker-authorized")
class WebDocumentUpdateMailTemplate(override val params: Unit = Unit) :
  FtlTemplate<Unit>("mail-web-document-update")

@Service
class TemplateService {

  private val log = LoggerFactory.getLogger(TemplateService::class.simpleName)

  @Autowired
  private lateinit var freemarkerConfigurer: FreeMarkerConfigurer

  fun <T> renderTemplate(
    corrId: String,
    template: FtlTemplate<T>,
  ): String {
    val templateName = template.templateName
    log.info("[$corrId] renderTemplate $templateName")
    return ByteArrayOutputStream().use {
      freemarkerConfigurer.configuration.getTemplate("$templateName.ftl.html")
        .process(template.params, OutputStreamWriter(it))
      String(it.toByteArray())
    }
  }
}
