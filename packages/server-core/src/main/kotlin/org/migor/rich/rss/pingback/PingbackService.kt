package org.migor.rich.rss.pingback

import org.migor.rich.rss.database.repositories.NativeFeedDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
@Profile("database2")
class PingbackService {

  private val log = LoggerFactory.getLogger(PingbackService::class.simpleName)

  @Autowired
  lateinit var feedRepository: NativeFeedDAO

  fun pingback(sourceURI: String, targetURI: String): ResponseEntity<String> {
//    The server MAY attempt to fetch the source URI to verify that the source does indeed link to the target.
//    The server MAY check its own data to ensure that the target exists and is a valid entry.
//    The server MAY check that the pingback has not already been registered.
//    The server MAY record the pingback.
    return ResponseEntity.ok("ok")
  }

}
