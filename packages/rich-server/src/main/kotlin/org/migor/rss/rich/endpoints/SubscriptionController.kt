package org.migor.rss.rich.endpoints

import org.migor.rss.rich.dtos.SubscriptionDto
import org.migor.rss.rich.models.Subscription
import org.migor.rss.rich.services.SubscriptionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class SubscriptionController {

  @Autowired
  lateinit var subscriptionService: SubscriptionService

  @GetMapping("/subscriptions")
  fun list(): Page<SubscriptionDto> {
    return subscriptionService.list()
  }

}
