package org.migor.rss.rich.api

import org.migor.rss.rich.service.HttpService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController

@RestController
class HttpProxyEndpoint {

  private val log = LoggerFactory.getLogger(HttpProxyEndpoint::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

//  @GetMapping("/api/http/get")
//  fun get(@RequestParam("url") url: String, @RequestParam("token") token: String): FeedDiscovery {
//
//  }

//  @PostMapping("/api/http/join")
//  fun join(@RequestParam("token") token: String): FeedDiscovery {
//    httpService.joinProxyRing(token);
//  }
}
