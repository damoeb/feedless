package org.migor.rss.rich.endpoint

import org.migor.rss.rich.dto.SubscriptionDto
import org.migor.rss.rich.service.SubscriptionService
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
