package org.migor.rss.rich.endpoint

import org.migor.rss.rich.dto.FeedDiscovery
import org.migor.rss.rich.service.FeedService
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
