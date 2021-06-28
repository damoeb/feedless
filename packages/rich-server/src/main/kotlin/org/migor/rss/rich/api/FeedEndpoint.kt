package org.migor.rss.rich.api

import org.migor.rss.rich.dto.FeedDiscovery
import org.migor.rss.rich.service.SourceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class FeedEndpoint {

  @Autowired
  lateinit var sourceService: SourceService

  @GetMapping("/api/feeds/discover")
  fun discoverFeeds(@RequestParam("url") url: String): FeedDiscovery? {
    return this.sourceService.discover(url)
  }

//  @GetMapping("/feed:{feedId}/subscribe")
//  fun subscribeToFeed(@PathVariable("feedId") feedId: String): ModelAndView {
//
//    val feed = feedService.findById(feedId)
//
//    val mav = ModelAndView("subscribe")
//    mav.addObject("feedName", "Markus Network")
//    mav.addObject("feedUrl", "foo/bar")
//    return mav
//  }

}
