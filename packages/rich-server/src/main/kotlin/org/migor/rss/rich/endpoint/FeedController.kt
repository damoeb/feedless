package org.migor.rss.rich.endpoint

import org.migor.rss.rich.dto.FeedDiscovery
import org.migor.rss.rich.service.EntryService
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.service.SourceService
import org.migor.rss.rich.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView


@Controller
class FeedController {

  @Autowired
  lateinit var userService: UserService;

  @Autowired
  lateinit var feedService: FeedService;

  @Autowired
  lateinit var sourceService: SourceService;

  @Autowired
  lateinit var entryService: EntryService;

  @GetMapping("/")
  fun index(): ModelAndView {
    val mav = ModelAndView("index")
    return mav
  }

  @GetMapping("/feeds/discover")
  fun discoverFeeds(@RequestParam("url") url: String): FeedDiscovery? {
    return this.sourceService.discover(url)
  }

  @GetMapping("/feed:{feedId}/subscribe")
  fun subscribeToFeed(@PathVariable("feedId") feedId: String): ModelAndView {

    val feed = feedService.findById(feedId)
    val user = userService.findById(feed.ownerId!!)

    val mav = ModelAndView("subscribe")
    mav.addObject("isPrivate", "private".equals(feedId))
    mav.addObject("feedName", "Markus Network")
    mav.addObject("feedUrl", "foo/bar")
    return mav
  }

  @GetMapping("/subscription:{subscriptionId}")
  fun listEntriesForSubscription(@PathVariable("subscriptionId") subscriptionId: String): ModelAndView {
    val mav = ModelAndView("feed")
    val source = sourceService.findBySubscription(subscriptionId)
    mav.addObject("description", source.description)
    mav.addObject("feedName", source.title)
    mav.addObject("entriesPage", entryService.findAllBySubscriptionId(subscriptionId))
//    mav.addObject("lastUpdatedAt", source.lastUpdatedAt)
//    mav.addObject("subscriberCount", 0)
    return mav
  }

//  @GetMapping("/f:{sourceId}")
//  fun globalFeed(@PathVariable("sourceId") sourceId: String): ModelAndView {
//    val mav = ModelAndView("feed")
//    val source = sourceService.findById(sourceId)
//    mav.addObject("feedName", source.title)
//    mav.addObject("description", source.description)
//    mav.addObject("entriesPage", entryService.entriesForSource(sourceId))
////    mav.addObject("lastUpdatedAt", source.lastUpdatedAt)
////    mav.addObject("subscriberCount", 0)
//    return mav
//  }

//  @GetMapping("/{ownerEmailHash}/feed/public/{subscriptionId}")
//  fun getPublicFeed(@PathVariable("ownerEmailHash") ownerEmailHash: String,
//                    @PathVariable("subscriptionId") subscriptionId: String): FeedDiscovery? {
////    -> return the feed, every url contains the subscriptionId
//    TODO()
//  }

  @RequestMapping("/user:{ownerEmailHash}/feed/comments/{entryId}/{subscriptionId}",
    method = [RequestMethod.GET],
    produces = [MediaType.TEXT_HTML_VALUE]
  )
  fun comments(@PathVariable("ownerEmailHash") ownerEmailHash: String,
               @PathVariable("entryId") entryId: String,
               @PathVariable("subscriptionId") subscriptionId: String): FeedDiscovery? {
    TODO()
  }

  @GetMapping("/user:{ownerEmailHash}/feed/like/{entryId}/{subscriptionId}")
  fun like(@PathVariable("ownerEmailHash") ownerEmailHash: String,
           @PathVariable("entryId") entryId: String,
           @PathVariable("subscriptionId") subscriptionId: String,
           @RequestParam("commentId", required = false) commentId: String): FeedDiscovery? {
    TODO()
  }

  @RequestMapping("/user:{ownerEmailHash}/feed/read/{entryId}",
    method = [RequestMethod.GET],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun read(@PathVariable("ownerEmailHash") ownerEmailHash: String,
           @PathVariable("entryId") entryId: String): FeedDiscovery? {
    TODO()
  }
}
