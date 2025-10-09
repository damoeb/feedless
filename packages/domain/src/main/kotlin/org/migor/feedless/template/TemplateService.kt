package org.migor.feedless.template

abstract class FreemarkerTemplate<T>(val templateName: String) {
  abstract val params: T
}

data class WelcomeMailParams(val productName: String)

data class MailTemplateWelcomePaid(override val params: WelcomeMailParams) :
  FreemarkerTemplate<WelcomeMailParams>("mail-welcome-paid")

data class AuthCodeMailParams(
  val domain: String,
  val codeValidUntil: String,
  val code: String,
  val description: String,
  val corrId: String
)

data class MailTemplateAuthCode(override val params: AuthCodeMailParams) :
  FreemarkerTemplate<AuthCodeMailParams>("mail-auth-code")

data class VisualDiffChangeDetectedParams(
  val trackerTitle: String,
  val website: String,
  val inlineImages: String
)

data class MailTemplateVisualDiffChange(override val params: VisualDiffChangeDetectedParams) :
  FreemarkerTemplate<VisualDiffChangeDetectedParams>("mail-visual-diff-change-detected")

data class VisualDiffWelcomeParams(
  val trackerTitle: String,
  val website: String,
  val trackerInfo: String,
  val activateTrackerMailsUrl: String,
  val info: String
)

data class MailTemplateVisualDiffWelcome(override val params: VisualDiffWelcomeParams) :
  FreemarkerTemplate<VisualDiffWelcomeParams>("mail-visual-diff-welcome")

class MailTemplateChangeTrackerAuthorized(override val params: Unit = Unit) :
  FreemarkerTemplate<Unit>("page-tracker-authorized")


interface TemplateService {
  fun <T> renderTemplate(template: FreemarkerTemplate<T>): String
}
