package org.migor.rss.rich.api

import org.migor.rss.rich.util.FeedExporter
import org.migor.rss.rich.service.SubscriptionGroupService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@Controller
class SubscriptionGroupController {

  @Autowired
  lateinit var groupService: SubscriptionGroupService

  @GetMapping("/group:{groupId}/rss", produces = ["application/rss+xml;charset=UTF-8"])
  fun rssFeed(@PathVariable("groupId") groupId: String): ResponseEntity<String> {
    return FeedExporter.toRss(groupService.findFeedByGroupId(groupId))
  }

  @GetMapping("/group:{groupId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(@PathVariable("groupId") groupId: String): ResponseEntity<String> {
    return FeedExporter.toAtom(groupService.findFeedByGroupId(groupId))
  }

  @GetMapping("/group:{groupId}/json", produces = ["application/json;charset=UTF-8"])
  fun jsonFeed(@PathVariable("groupId") groupId: String): ResponseEntity<String> {
    return FeedExporter.toJson(groupService.findFeedByGroupId(groupId))
  }

}
