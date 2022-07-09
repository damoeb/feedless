package org.migor.rich.rss.service

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("database")
class GraphService {

  private val log = LoggerFactory.getLogger(GraphService::class.simpleName)

  fun link(fromUrl: String, toUrl: String) {
    // todo mag implement
  }
}
