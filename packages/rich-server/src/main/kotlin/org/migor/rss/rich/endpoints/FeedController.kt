package org.migor.rss.rich.endpoints

import org.migor.rss.rich.dtos.FeedDiscovery
import org.migor.rss.rich.services.FeedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class FeedController {

  @Autowired
  lateinit var feedService: FeedService;

  @GetMapping("/feeds/discover")
  fun discoverFeeds(@RequestParam("url") url: String): FeedDiscovery? {
    return this.feedService.discover(url)
  }

}
