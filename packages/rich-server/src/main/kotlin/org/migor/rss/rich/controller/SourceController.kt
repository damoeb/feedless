package org.migor.rss.rich.controller

import org.migor.rss.rich.FeedExporter
import org.migor.rss.rich.service.EntryService
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.service.SourceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView


@Controller
class SourceController {

  @Autowired
  lateinit var sourceService: SourceService

  @Autowired
  lateinit var entryService: EntryService

  @Autowired
  lateinit var feedService: FeedService

  @GetMapping("/source:{sourceId}/rss", produces = ["application/rss+xml;charset=UTF-8"])
  fun rssFeed(@PathVariable("sourceId") sourceId: String): ResponseEntity<String> {
    return FeedExporter.toRss(feedService.findBySourceId(sourceId))
  }

  @GetMapping("/source:{sourceId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(@PathVariable("sourceId") sourceId: String): ResponseEntity<String> {
    return FeedExporter.toAtom(feedService.findBySourceId(sourceId))
  }

  @GetMapping("/source:{sourceId}/json", produces = ["application/json;charset=UTF-8"])
  fun jsonFeed(@PathVariable("sourceId") sourceId: String): ResponseEntity<String> {
    return FeedExporter.toJson(feedService.findBySourceId(sourceId))
  }

  @GetMapping("/source:{sourceId}")
  fun getSourceDetails(@PathVariable("sourceId") sourceId: String): ModelAndView {
    val mav = ModelAndView("source")
    val source = sourceService.findById(sourceId)
    mav.addObject("source", source)
    mav.addObject("entries", entryService.findLatestBySourceId(sourceId))
    return mav
  }

}
