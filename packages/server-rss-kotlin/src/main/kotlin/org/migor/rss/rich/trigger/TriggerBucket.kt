package org.migor.rss.rich.trigger

import org.migor.rss.rich.database.repository.BucketRepository
import org.migor.rss.rich.database.repository.SubscriptionRepository
import org.migor.rss.rich.harvest.BucketHarvester
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Service
class TriggerBucket internal constructor() {

  private val log = LoggerFactory.getLogger(TriggerBucket::class.simpleName)

  @Autowired
  lateinit var bucketRepository: BucketRepository

  @Autowired
  lateinit var bucketHarvester: BucketHarvester

  @Scheduled(fixedDelay = 20345, initialDelay = 20000)
  @Transactional(readOnly = true)
  fun fillBuckets() {
    bucketRepository.findDueToBuckets(Date())
      .forEach { bucket -> bucketHarvester.harvestBucket(bucket) }
  }

  //  @PostMapping("/triggers/update/feed/{feedId}", produces = ["application/json;charset=UTF-8"])
//  fun triggerUpdate(@PathVariable("feedId") feedId: String) {
//    return streamService.triggerUpdate(streamId);
//  }

}

