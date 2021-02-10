package org.migor.rss.rich.endpoint

import org.migor.rss.rich.dto.FeedDiscovery
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
  lateinit var sourceService: SourceService;

  @GetMapping("/")
  fun index(): ModelAndView {
    val mav = ModelAndView("index")
    return mav
  }

  @GetMapping("/feeds/discover")
  fun discoverFeeds(@RequestParam("url") url: String): FeedDiscovery? {
    return this.sourceService.discover(url)
  }

  @GetMapping("/u:{ownerEmailHash}/feeds/{feedName}/subscribe")
  fun subscribeToFeed(@PathVariable("ownerEmailHash") ownerEmailHash: String,
                      @PathVariable("feedName") feedName: String): ModelAndView {

    val user = userService.findByEmailHash(ownerEmailHash)

    val mav = ModelAndView("subscribe")
    mav.addObject("isPrivate", "private".equals(feedName))
    mav.addObject("ownerEmailHash", ownerEmailHash)
    mav.addObject("feedName", "Markus Network")
    mav.addObject("feedUrl", "foo/bar")
    return mav
  }

  @GetMapping("/u:{ownerEmailHash}/feeds/{feedName}")
  fun showUsersFeed(@PathVariable("ownerEmailHash") ownerEmailHash: String,
                    @PathVariable("feedName") feedName: String): ModelAndView {
    val mav = ModelAndView("feed")
    val user = userService.findByEmailHash(ownerEmailHash)
    mav.addObject("description", "${user.name}s $feedName feed")
    mav.addObject("feedName", feedName)
//    mav.addObject("lastUpdatedAt", source.lastUpdatedAt)
//    mav.addObject("subscriberCount", 0)
    return mav
  }

  @GetMapping("/f:{feedId}")
  fun globalFeed(@PathVariable("feedId") feedId: String): ModelAndView {
    val mav = ModelAndView("feed")
    val source = sourceService.findById(feedId)
    mav.addObject("feedName", source.title)
    mav.addObject("description", source.description)
//    mav.addObject("lastUpdatedAt", source.lastUpdatedAt)
//    mav.addObject("subscriberCount", 0)
    return mav
  }

  @GetMapping("/u:{ownerEmailHash}/feed/{feedName}/{subscriptionId}")
  fun getAtomFeed(@PathVariable("ownerEmailHash") ownerEmailHash: String,
                  @PathVariable("feedName") feedName: String,
                  @PathVariable("subscriptionId") subscriptionId: String): FeedDiscovery? {
//    -> return the feed, every url contains the subscriptionId
    TODO()
  }

//  @GetMapping("/{ownerEmailHash}/feed/public/{subscriptionId}")
//  fun getPublicFeed(@PathVariable("ownerEmailHash") ownerEmailHash: String,
//                    @PathVariable("subscriptionId") subscriptionId: String): FeedDiscovery? {
////    -> return the feed, every url contains the subscriptionId
//    TODO()
//  }

  @RequestMapping("/u:{ownerEmailHash}/feed/comments/{entryId}/{subscriptionId}",
    method = [RequestMethod.GET],
    produces = [MediaType.TEXT_HTML_VALUE]
  )
  fun comments(@PathVariable("ownerEmailHash") ownerEmailHash: String,
               @PathVariable("entryId") entryId: String,
               @PathVariable("subscriptionId") subscriptionId: String): FeedDiscovery? {
    TODO()
  }

  @GetMapping("/u:{ownerEmailHash}/feed/like/{entryId}/{subscriptionId}")
  fun like(@PathVariable("ownerEmailHash") ownerEmailHash: String,
           @PathVariable("entryId") entryId: String,
           @PathVariable("subscriptionId") subscriptionId: String,
           @RequestParam("commentId", required = false) commentId: String): FeedDiscovery? {
    TODO()
  }

  @RequestMapping("/u:{ownerEmailHash}/feed/read/{entryId}",
    method = [RequestMethod.GET],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun read(@PathVariable("ownerEmailHash") ownerEmailHash: String,
           @PathVariable("entryId") entryId: String): FeedDiscovery? {
    TODO()
  }
}
