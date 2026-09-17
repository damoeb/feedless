package org.migor.feedless.payment.stripe

import jakarta.validation.constraints.NotBlank
import org.migor.feedless.AppProfiles
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

/** The Stripe account this instance charges through. */
@ConfigurationProperties("stripe")
@ConditionalOnProperty("stripe.api-key")
@Validated
@Profile(AppProfiles.properties)
data class StripeProperties(
  @field:NotBlank val apiKey: String,
  @field:NotBlank val webhookSecret: String,
) {
  // the generated toString would put both secrets in every startup log
  override fun toString() = "StripeProperties(apiKey=***, webhookSecret=***)"
}
