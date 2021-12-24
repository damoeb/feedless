package org.migor.rss.rich.pingback

import org.migor.rss.rich.database.repository.FeedRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class PingbackService {

  private val log = LoggerFactory.getLogger(PingbackService::class.simpleName)

  @Autowired
  lateinit var feedRepository: FeedRepository

  fun pingback(sourceURI: String, targetURI: String): ResponseEntity<String> {
//    The server MAY attempt to fetch the source URI to verify that the source does indeed link to the target.
//    The server MAY check its own data to ensure that the target exists and is a valid entry.
//    The server MAY check that the pingback has not already been registered.
//    The server MAY record the pingback.
    return ResponseEntity.ok("ok")
  }

}
