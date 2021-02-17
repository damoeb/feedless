package org.migor.rss.rich.endpoint

import org.migor.rss.rich.FeedExporter
import org.migor.rss.rich.dto.FeedDto
import org.migor.rss.rich.dto.SubscriptionDto
import org.migor.rss.rich.dto.SubscriptionGroupDto
import org.migor.rss.rich.service.*
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
  lateinit var feedService: FeedService

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
    mav.addObject("emailHash", emailHash)
    mav.addObject("name", user.name)
    mav.addObject("description", user.description)
    mav.addObject("feeds", user.feeds)

    mav.addObject("entries", entryService.findAllByUserId(user.id!!))
    mav.addObject("subscriberCount", 0)

    val subscriptions = subscriptionService.findAllByOwnerId(user.id!!)
    val groups = subscriptionGroupService.findAllByOwnerId(user.id!!)
    groups.forEach { group: SubscriptionGroupDto ->
      run {
        group.subscriptions = subscriptions.filter { subscription: SubscriptionDto -> subscription.groupId.equals(group.id) }
      }
    }

    val unassigned = subscriptions.filter { subscription: SubscriptionDto -> subscription.groupId == null }
    if (unassigned.isEmpty()) {
      mav.addObject("groups", groups)
    } else {
      val merge = ArrayList<SubscriptionGroupDto>()
      merge.addAll(groups)

      merge.add(SubscriptionGroupDto(null, "Unassigned", user.id, 0, unassigned))
      mav.addObject("groups", merge)
    }

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
    val feed = FeedDto("id", "name", "description", Date(), "ownerId")
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
