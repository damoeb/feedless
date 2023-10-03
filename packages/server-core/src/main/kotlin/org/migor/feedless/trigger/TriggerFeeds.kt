package org.migor.feedless.trigger

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.NativeFeedStatus
import org.migor.feedless.data.jpa.models.NativeFeedEntity
import org.migor.feedless.data.jpa.repositories.NativeFeedDAO
import org.migor.feedless.harvest.FeedHarvester
import org.migor.feedless.util.CryptUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.database)
class TriggerFeeds internal constructor() {

  @Autowired
  lateinit var feedRepository: NativeFeedDAO

  @Autowired
  lateinit var feedHarvester: FeedHarvester

//  @Scheduled(fixedDelay = 1234, initialDelay = 20000)
  @Transactional(readOnly = true)
  fun fetchFeeds() {
    val excludedStates = arrayOf(NativeFeedStatus.DISABLED, NativeFeedStatus.NOT_FOUND, NativeFeedStatus.SERVICE_UNAVAILABLE)
    val pageable = PageRequest.ofSize(10)
    val corrId = CryptUtil.newCorrId()
    feedRepository.findSomeDueToFeeds(Date(), excludedStates, pageable)
      .forEach { feed: NativeFeedEntity ->
        feedHarvester.harvestFeed(corrId, feed)
      }
  }
}
