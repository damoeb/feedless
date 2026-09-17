package org.migor.feedless.report

import jakarta.validation.constraints.NotBlank
import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

/** How new report subscriptions start, and who their mails come from. */
@ConfigurationProperties("app.report")
@Validated
@Profile(AppProfiles.properties)
data class ReportProperties(
  val subscriptionMode: ReportSubscriptionMode,
  @field:NotBlank val sender: String,
)
