package org.migor.feedless.template

abstract class FreemarkerTemplate<T>(val templateName: String) {
  abstract val params: T
}

data class ReportCreatedParams(
  val language: String,
  val deactivationLink: String,
  val abuseLink: String,
  val reportName: String,
  val cronExpression: String,
  val nextScheduledAt: String,
)

data class MailTemplateReportCreated(override val params: ReportCreatedParams) :
  FreemarkerTemplate<ReportCreatedParams>("mail-report-created")

data class ReportConfirmRequestParams(
  val language: String,
  val confirmationLink: String,
)

data class MailTemplateReportConfirmRequest(override val params: ReportConfirmRequestParams) :
  FreemarkerTemplate<ReportConfirmRequestParams>("mail-report-confirm-request")

data class AuthCodeMailParams(
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

class PageTemplateReportAbuse(override val params: Unit = Unit) :
  FreemarkerTemplate<Unit>("page-report-abuse")


interface TemplateService {
  fun <T> renderTemplate(template: FreemarkerTemplate<T>): String

  /**
   * Renders [template] in a variant: looks up `<template>-<variant>` first,
   * falling back to `<template>` if missing. A separate overload rather than
   * a default parameter, so existing callers and their test doubles stay
   * unchanged.
   */
  fun <T> renderTemplate(template: FreemarkerTemplate<T>, variant: TemplateVariant?): String =
    renderTemplate(template)
}
