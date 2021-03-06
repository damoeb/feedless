package org.migor.rss.rich.api

import org.migor.rss.rich.service.SubscriptionGroupService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@Controller
class SubscriptionGroupEndpoint {

  @Autowired
  lateinit var subscriptionGroupService: SubscriptionGroupService

  @GetMapping("/api/group:{groupId}")
  fun getSubscriptionGroupDetails(@PathVariable("groupId") groupId: String): Map<String, Any> {
    return subscriptionGroupService.getSubscriptionGroupDetails(groupId)
  }

}
