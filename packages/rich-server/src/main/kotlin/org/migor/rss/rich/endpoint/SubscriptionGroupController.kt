package org.migor.rss.rich.endpoint

import org.migor.rss.rich.service.EntryService
import org.migor.rss.rich.service.SubscriptionGroupService
import org.migor.rss.rich.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView


@Controller
class SubscriptionGroupController {

  @Autowired
  lateinit var subscriptionGroupService: SubscriptionGroupService

  @Autowired
  lateinit var userService: UserService

  @Autowired
  lateinit var entryService: EntryService

  @GetMapping("/group:{groupId}")
  fun getSubscriptionGroupDetails(@PathVariable("groupId") groupId: String): ModelAndView {
    val mav = ModelAndView("group")
    val group = subscriptionGroupService.findById(groupId)
    val user = userService.findById(group.ownerId!!)
    mav.addObject("user", user)
    mav.addObject("group", group)
    mav.addObject("entries", entryService.findLatestBySubscriptionGroupId(groupId))
    return mav
  }

}
