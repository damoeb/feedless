package org.migor.rss.rich.endpoint

import org.migor.rss.rich.service.SubscriptionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView


@Controller
class SubscriptionController {

  @Autowired
  lateinit var subscriptionService: SubscriptionService

//  @GetMapping("/subscriptions")
//  fun list(): Page<SourceDto> {
//    return subscriptionService.list()
//  }

//  @GetMapping("/subscriptions/{id}")
//  fun subscription(@PathVariable("id") subscriptionId: String): SubscriptionDto {
//    return subscriptionService.subscription(subscriptionId)
//  }

//  @GetMapping("/subscriptions/{id}/feed")
//  fun feed(@PathVariable("id") subscriptionId: String): FeedDto {
//    return subscriptionService.feed(subscriptionId)
//  }

//  @GetMapping("/subscriptions/{id}/feed/rss", produces = ["application/rss+xml;charset=UTF-8"])
//  fun rssFeed(@PathVariable("id") subscriptionId: String): ResponseEntity<String> {
//    return FeedExporter.toRss(subscriptionService.feed(subscriptionId))
//  }
//
//  @GetMapping("/subscriptions/{id}/feed/atom", produces = ["application/atom+xml;charset=UTF-8"])
//  fun atomFeed(@PathVariable("id") subscriptionId: String): ResponseEntity<String> {
//    return FeedExporter.toAtom(subscriptionService.feed(subscriptionId))
//  }
//
//  @GetMapping("/subscriptions/{id}/feed/json", produces = ["application/json;charset=UTF-8"])
//  fun jsonFeed(@PathVariable("id") subscriptionId: String): ResponseEntity<String> {
//    return FeedExporter.toJson(subscriptionService.feed(subscriptionId))
//  }

  @GetMapping("/subscription:/{subscriptionId}")
  fun getSubscriptionDetails(@PathVariable("subscriptionId") subscriptionId: String): ModelAndView {
    val mav = ModelAndView("subscription")
    val subscription = subscriptionService.findById(subscriptionId)
    return mav
  }

}
