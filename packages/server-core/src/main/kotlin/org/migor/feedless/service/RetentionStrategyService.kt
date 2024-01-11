package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.database)
class RetentionStrategyService {

  private val log = LoggerFactory.getLogger(RetentionStrategyService::class.simpleName)

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

//  fun applyRetentionStrategy(corrId: String, feed: NativeFeedEntity) {
//    feed.retentionSize?.let {
//      val retentionSize = it.coerceAtLeast(2)
//      log.info("apply retention strategy size=$retentionSize")
//      webDocumentDAO.deleteAllByStreamIdWithSkip(feed.streamId, retentionSize)
//    }
//  }
}
