package org.migor.rss.rich.api

import org.migor.rss.rich.dto.FeedDto
import org.migor.rss.rich.dto.SubscriptionDto
import org.migor.rss.rich.dto.SubscriptionGroupDto
import org.migor.rss.rich.dto.UserDto
import org.migor.rss.rich.model.AccessPolicy
import org.migor.rss.rich.service.EntryService
import org.migor.rss.rich.service.SubscriptionGroupService
import org.migor.rss.rich.service.SubscriptionService
import org.migor.rss.rich.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView


@Controller
class UserEndpoint {

  @Autowired
  lateinit var userService: UserService

  @Autowired
  lateinit var entryService: EntryService

  @Autowired
  lateinit var subscriptionService: SubscriptionService

  @Autowired
  lateinit var subscriptionGroupService: SubscriptionGroupService

  @GetMapping("/api/user:{emailHash}")
  fun user(@PathVariable("emailHash") emailHash: String): ModelAndView {
    val mav = ModelAndView("user")
    val user = userService.findByEmailHash(emailHash)
    mav.addObject("user", user)
    mav.addObject("emailHash", emailHash)
    mav.addObject("name", user.name)
    mav.addObject("description", user.description)
    mav.addObject("feeds", user.feeds.filter { feed: FeedDto -> feed.accessPolicy != AccessPolicy.PUBLIC })

    val entries = entryService.findAllByUserId(user.id!!)
    mav.addObject("entries", entries)
    mav.addObject("subscriberCount", 0)

    val subscriptions = subscriptionService.findAllByOwnerId(user.id!!)
    val realGroups = subscriptionGroupService.findAllByOwnerId(user.id!!)
      .sortedBy { group: SubscriptionGroupDto -> group.order }
    realGroups.forEach { group: SubscriptionGroupDto ->
      run {
        group.subscriptions = subscriptions.filter { subscription: SubscriptionDto -> subscription.groupId.equals(group.id) }
      }
    }
    val allGroups = ArrayList<SubscriptionGroupDto>()
//    allGroups.add(SubscriptionGroupDto(null, "Public", user.id, 0, null, entries))
//    val unassignedGroup = SubscriptionGroupDto(null, "Unassigned", user.id, 1)
//    allGroups.add(unassignedGroup)

//    val unassignedSubscriptions = subscriptions.filter { subscription: SubscriptionDto -> subscription.groupId == null }
//    if (!unassignedSubscriptions.isEmpty()) {
//      unassignedGroup.subscriptions = unassignedSubscriptions
//    }
    allGroups.addAll(realGroups)

    mav.addObject("groups", allGroups)

    return mav
  }

  @GetMapping("/api/users")
  fun users(): Page<UserDto> {
    return userService.list()
  }

}
