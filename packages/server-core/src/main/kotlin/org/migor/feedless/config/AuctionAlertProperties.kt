package org.migor.feedless.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auction-alert")
class AuctionAlertProperties {
  var stripe: Stripe = Stripe()
  var checkout: Checkout = Checkout()

  class Stripe {
    var priceId: String = ""
  }

  class Checkout {
    var trialDays: Long = 30
    var successUrl: String = ""
    var cancelUrl: String = ""
  }
}
