package org.migor.rss.rich.endpoint

import org.migor.rss.rich.FeedExporter
import org.migor.rss.rich.service.EntryService
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.service.SubscriptionService
import org.migor.rss.rich.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView


@Controller
class SubscriptionController {

  @Autowired
  lateinit var subscriptionService: SubscriptionService

  @Autowired
  lateinit var userService: UserService

  @Autowired
  lateinit var entryService: EntryService

  @Autowired
  lateinit var feedService: FeedService

  @GetMapping("/subscription:{subscriptionId}/rss", produces = ["application/rss+xml;charset=UTF-8"])
  fun rssFeed(@PathVariable("subscriptionId") subscriptionId: String): ResponseEntity<String> {
    return FeedExporter.toRss(feedService.findBySubscriptionId(subscriptionId))
  }

  @GetMapping("/subscription:{subscriptionId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(@PathVariable("subscriptionId") subscriptionId: String): ResponseEntity<String> {
    return FeedExporter.toAtom(feedService.findBySubscriptionId(subscriptionId))
  }

  @GetMapping("/subscription:{subscriptionId}/json", produces = ["application/json;charset=UTF-8"])
  fun jsonFeed(@PathVariable("subscriptionId") subscriptionId: String): ResponseEntity<String> {
    return FeedExporter.toJson(feedService.findBySubscriptionId(subscriptionId))
  }

  @GetMapping("/subscription:{subscriptionId}")
  fun getSubscriptionDetails(@PathVariable("subscriptionId") subscriptionId: String): ModelAndView {
    val mav = ModelAndView("subscription")
    val subscription = subscriptionService.findById(subscriptionId)
    val user = userService.findById(subscription.ownerId!!)
    mav.addObject("subscription", subscription)
    mav.addObject("user", user)
    mav.addObject("entries", entryService.findLatestBySubscriptionId(subscriptionId))
    return mav
  }

}
