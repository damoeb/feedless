package org.migor.feedless.report

enum class ReportSubscriptionMode(val value: String) {
  OPT_OUT("opt-out"),
  OPT_IN("opt-in");

  companion object {
    fun parse(value: String): ReportSubscriptionMode =
      entries.firstOrNull { it.value == value.trim().lowercase() }
        ?: throw IllegalArgumentException("app.report.subscription-mode must be opt-out or opt-in, was '$value'")
  }
}
