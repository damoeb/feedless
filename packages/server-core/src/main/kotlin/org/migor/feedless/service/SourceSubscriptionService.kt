package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.api.auth.CurrentUser
import org.migor.feedless.api.graphql.DtoResolver.fromDTO
import org.migor.feedless.data.jpa.models.BucketEntity
import org.migor.feedless.data.jpa.models.ScrapeSourceEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.StreamEntity
import org.migor.feedless.data.jpa.repositories.BucketDAO
import org.migor.feedless.data.jpa.repositories.ScrapeSourceDAO
import org.migor.feedless.data.jpa.repositories.SourceSubscriptionDAO
import org.migor.feedless.data.jpa.repositories.StreamDAO
import org.migor.feedless.generated.types.BucketCreateInput
import org.migor.feedless.generated.types.ScrapeRequestInput
import org.migor.feedless.generated.types.ScrapeSourceCreateOrConnectInput
import org.migor.feedless.generated.types.SourceSubscription
import org.migor.feedless.generated.types.SourceSubscriptionCreateInput
import org.migor.feedless.generated.types.SourceSubscriptionsCreateInput
import org.migor.feedless.util.GenericFeedUtil.fromDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.database)
class SourceSubscriptionService {

  private val log = LoggerFactory.getLogger(SourceSubscriptionService::class.simpleName)

  @Autowired
  lateinit var scrapeSourceDAO: ScrapeSourceDAO

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var streamDAO: StreamDAO

  @Autowired
  lateinit var sourceSubscriptionDAO: SourceSubscriptionDAO

  @Autowired
  lateinit var currentUser: CurrentUser

  @Autowired
  lateinit var defaultsService: DefaultsService

  @Transactional
  fun create(data: SourceSubscriptionsCreateInput): List<SourceSubscription> {
    log.info("create sub")
    return data.subscriptions.map { createSubscription(it).toDto() }
  }

  private fun createSubscription(subInput: SourceSubscriptionCreateInput): SourceSubscriptionEntity {
    val sub = SourceSubscriptionEntity()

    sub.sources = subInput.sources.map { resolveSource(it, sub) }.toMutableList()
    sub.bucketId = resolveBucket(subInput.sinkOptions.bucket).id
    sub.ownerId = currentUser.user().id
    sub.schedulerExpression = subInput.sourceOptions.refreshCron
    sub.retentionMaxItems = defaultsService.retentionMaxItems(subInput.sinkOptions.bucket.retention.maxItems)
    sub.retentionMaxAgeDays = subInput.sinkOptions.bucket.retention.maxAgeDays

    return sourceSubscriptionDAO.save(sub)
  }

  private fun resolveBucket(bucketInput: BucketCreateInput): BucketEntity {
    val stream = StreamEntity()
    streamDAO.save(stream)

    val bucket = BucketEntity()
    bucket.title = bucketInput.title
    bucket.streamId = stream.id
    bucket.description = bucketInput.description
    bucket.visibility = fromDTO(bucketInput.visibility)
    bucket.ownerId = currentUser.user().id
    return bucketDAO.save(bucket)
  }

  private fun resolveSource(sourceInput: ScrapeSourceCreateOrConnectInput, sub: SourceSubscriptionEntity): ScrapeSourceEntity {
    return sourceInput.create?.let {
      createScrapeSource(it, sub)
    } ?: sourceInput.connect?.let {
      scrapeSourceDAO.findById(UUID.fromString(it.id)).orElseThrow()
    } ?: throw RuntimeException("")
  }

  private fun createScrapeSource(scrapeRequest: ScrapeRequestInput, sub: SourceSubscriptionEntity): ScrapeSourceEntity {
    val entity = ScrapeSourceEntity()
    entity.scrapeRequest = fromDto(scrapeRequest)
    entity.subscriptionId = sub.id
    return scrapeSourceDAO.save(entity)
  }

}

private fun SourceSubscriptionEntity.toDto(): SourceSubscription {
  return SourceSubscription.newBuilder()
    .id(this.id.toString())
    .build()
}
