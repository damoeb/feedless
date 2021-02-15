package org.migor.rss.rich.endpoint

import org.migor.rss.rich.service.SubscriptionGroupService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller


@Controller
class SubscriptionGroupController {

  @Autowired
  lateinit var subscriptionGroupService: SubscriptionGroupService

//  @GetMapping("/subgroup:/{groupId}")
//  fun getSubscriptionDetails(@PathVariable("groupId") groupId: String): ModelAndView {
////    val mav = ModelAndView("subscription")
////    val subscription = subscriptionService.findById(subscriptionId)
//    return mav
//  }

}
