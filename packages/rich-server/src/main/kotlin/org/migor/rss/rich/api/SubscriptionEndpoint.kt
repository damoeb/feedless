package org.migor.rss.rich.api

import org.migor.rss.rich.service.SubscriptionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@Controller
class SubscriptionEndpoint {

  @Autowired
  lateinit var subscriptionService: SubscriptionService

  @GetMapping("/api/subscription:{subscriptionId}")
  fun getSubscriptionDetails(@PathVariable("subscriptionId") subscriptionId: String): Map<String, Any> {
    return subscriptionService.getSubscriptionDetails(subscriptionId)
  }

}
