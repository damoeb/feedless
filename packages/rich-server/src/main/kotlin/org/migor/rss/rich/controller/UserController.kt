package org.migor.rss.rich.controller

import org.migor.rss.rich.FeedExporter
import org.migor.rss.rich.dto.FeedDto
import org.migor.rss.rich.dto.SubscriptionDto
import org.migor.rss.rich.dto.SubscriptionGroupDto
import org.migor.rss.rich.model.AccessPolicy
import org.migor.rss.rich.service.EntryService
import org.migor.rss.rich.service.SubscriptionGroupService
import org.migor.rss.rich.service.SubscriptionService
import org.migor.rss.rich.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList


@Controller
class UserController {

  @Autowired
  lateinit var userService: UserService

  @Autowired
  lateinit var entryService: EntryService

  @Autowired
  lateinit var subscriptionService: SubscriptionService

  @Autowired
  lateinit var subscriptionGroupService: SubscriptionGroupService

  @GetMapping("/user:{emailHash}")
  fun user(@PathVariable("emailHash") emailHash: String): ModelAndView {
    val mav = ModelAndView("user")
    val user = userService.findByEmailHash(emailHash)
    mav.addObject("user", user)
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

  @GetMapping("/user:{ownerEmailHash}/atom")
  fun atomFeed(@PathVariable("ownerEmailHash") ownerEmailHash: String): ResponseEntity<String> {
    return FeedExporter.toAtom(getEntries(ownerEmailHash))
  }

  @GetMapping("/user:{ownerEmailHash}/rss")
  fun rssFeed(@PathVariable("ownerEmailHash") ownerEmailHash: String): ResponseEntity<String> {
    return FeedExporter.toRss(getEntries(ownerEmailHash))
  }

  @GetMapping("/user:{ownerEmailHash}/json")
  fun jsonFeed(@PathVariable("ownerEmailHash") ownerEmailHash: String): ResponseEntity<String> {
    return FeedExporter.toJson(getEntries(ownerEmailHash))
  }

  private fun getEntries(ownerEmailHash: String): FeedDto {
    val user = userService.findByEmailHash(ownerEmailHash)

    val entries = entryService.findAllByUserId(user.id!!)
    val feed = FeedDto("id", "name", "description", Date(), "ownerId", AccessPolicy.PROTECTED)
    feed.entries = entries
    return feed
  }

  @GetMapping("/u")
  fun users(): ModelAndView {
    val mav = ModelAndView("users")
    val users = userService.list()
    mav.addObject("users", users.get().collect(Collectors.toList()))
    mav.addObject("page", users.number)
    mav.addObject("isFirst", users.isFirst)
    mav.addObject("isLast", users.isLast)
    return mav
  }

}
