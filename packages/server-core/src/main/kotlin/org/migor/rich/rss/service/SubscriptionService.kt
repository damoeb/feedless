package org.migor.rich.rss.service

import org.migor.rich.rss.database.models.SubscriptionEntity
import org.migor.rich.rss.database.models.UserEntity
import org.migor.rich.rss.database.repositories.BucketDAO
import org.migor.rich.rss.database.repositories.NativeFeedDAO
import org.migor.rich.rss.database.repositories.SubscriptionDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class SubscriptionService {

  private val log = LoggerFactory.getLogger(SubscriptionService::class.simpleName)

  @Autowired
  lateinit var subscriptionDAO: SubscriptionDAO

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var feedDAO: NativeFeedDAO

  fun subscribeToBucket(bucketId: UUID, digest: Boolean, notify: Boolean, filter: String?, user: UserEntity): SubscriptionEntity {
    val subscription = SubscriptionEntity()
    subscription.bucket = bucketDAO.findById(bucketId).orElseThrow()
    subscription.user = user
    subscription.filter = filter
    return subscriptionDAO.save(subscription)
  }

  fun subscribeToFeed(feedId: UUID, digest: Boolean, notify: Boolean, filter: String?, user: UserEntity): SubscriptionEntity {
    val subscription = SubscriptionEntity()
    subscription.feed = feedDAO.findById(feedId).orElseThrow()
    subscription.user = user
    subscription.filter = filter
    return subscriptionDAO.save(subscription)
  }


}
