package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.repositories.BucketDAO
import org.migor.rich.rss.data.jpa.repositories.NativeFeedDAO
import org.migor.rich.rss.data.jpa.repositories.SubscriptionDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class SubscriptionService {

  private val log = LoggerFactory.getLogger(SubscriptionService::class.simpleName)

  @Autowired
  lateinit var subscriptionDAO: SubscriptionDAO

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var feedDAO: NativeFeedDAO

//  fun subscribeToBucket(
//    bucketId: UUID,
//    digest: Boolean,
//    notify: Boolean,
//    filter: String?,
//    user: UserEntity
//  ): SubscriptionEntity {
//    val subscription = SubscriptionEntity()
//    subscription.bucket = bucketDAO.findById(bucketId).orElseThrow()
//    subscription.user = user
//    subscription.filter = filter
//    return subscriptionDAO.save(subscription)
//  }
//
//  fun subscribeToFeed(
//    feedId: UUID,
//    digest: Boolean,
//    notify: Boolean,
//    filter: String?,
//    user: UserEntity
//  ): SubscriptionEntity {
//    val subscription = SubscriptionEntity()
//    subscription.feedId = feedDAO.findById(feedId).orElseThrow().id
//    subscription.userId = user.id
//    subscription.filter = filter
//    return subscriptionDAO.save(subscription)
//  }


}
