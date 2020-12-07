package org.migor.rss.rich.endpoint

import org.migor.rss.rich.FeedExporter
import org.migor.rss.rich.dto.EntryDto
import org.migor.rss.rich.dto.FeedDto
import org.migor.rss.rich.dto.SubscriptionDto
import org.migor.rss.rich.service.SubscriptionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@RestController
class SubscriptionController {

  @Autowired
  lateinit var subscriptionService: SubscriptionService

  @GetMapping("/subscriptions")
  fun list(): Page<SubscriptionDto> {
    return subscriptionService.list()
  }

//  @GetMapping("/subscriptions/{id}")
//  fun subscription(@PathVariable("id") subscriptionId: String): SubscriptionDto {
//    return subscriptionService.subscription(subscriptionId)
//  }

  @GetMapping("/subscriptions/{id}/feed")
  fun feed(@PathVariable("id") subscriptionId: String): FeedDto {
    return subscriptionService.feed(subscriptionId)
  }

  @GetMapping("/subscriptions/{id}/feed/rss")
  fun rssFeed(@PathVariable("id") subscriptionId: String): ResponseEntity<String> {
    return FeedExporter.toRss(subscriptionService.feed(subscriptionId))
  }

  @GetMapping("/subscriptions/{id}/feed/atom")
  fun atomFeed(@PathVariable("id") subscriptionId: String): ResponseEntity<String> {
    return FeedExporter.toAtom(subscriptionService.feed(subscriptionId))
  }

  @GetMapping("/subscriptions/{id}/feed/json")
  fun jsonFeed(@PathVariable("id") subscriptionId: String): ResponseEntity<String> {
    return FeedExporter.toJson(subscriptionService.feed(subscriptionId))
  }

  @GetMapping("/subscriptions/{id}/entries")
  fun entries(@PathVariable("id") subscriptionId: String): Page<EntryDto> {
    return subscriptionService.entries(subscriptionId)
  }

}
