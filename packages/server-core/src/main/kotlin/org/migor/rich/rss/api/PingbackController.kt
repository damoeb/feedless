package org.migor.rich.rss.api

import org.migor.rich.rss.pingback.PingbackService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@Profile("rich")
class PingbackController {

  @Autowired
  lateinit var pingbackService: PingbackService

  @GetMapping("/pingback.ping")
  fun pingback(
    @RequestParam("sourceURI") sourceURI: String,
    @RequestParam("targetURI") targetURI: String,
  ): ResponseEntity<String> {
    return pingbackService.pingback(sourceURI, targetURI)
  }

}
