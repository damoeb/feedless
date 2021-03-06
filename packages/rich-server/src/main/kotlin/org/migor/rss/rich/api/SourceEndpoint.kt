package org.migor.rss.rich.api

import org.migor.rss.rich.service.SourceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@Controller
class SourceEndpoint {

  @Autowired
  lateinit var sourceService: SourceService

  @GetMapping("/api/source:{sourceId}")
  fun getSourceDetails(@PathVariable("sourceId") sourceId: String): Map<String, Any> {
    return sourceService.getSourceDetails(sourceId)
  }

}
