package org.migor.feedless.service

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.auth.CurrentUser
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.api.graphql.DtoResolver.fromDTO
import org.migor.feedless.config.CacheNames
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.BucketEntity
import org.migor.feedless.data.jpa.models.ScrapeSourceEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.StreamEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.BucketDAO
import org.migor.feedless.data.jpa.repositories.ScrapeSourceDAO
import org.migor.feedless.data.jpa.repositories.SourceSubscriptionDAO
import org.migor.feedless.data.jpa.repositories.StreamDAO
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.generated.types.BucketCreateInput
import org.migor.feedless.generated.types.ScrapeRequestInput
import org.migor.feedless.generated.types.ScrapeSourceCreateOrConnectInput
import org.migor.feedless.generated.types.SourceSubscription
import org.migor.feedless.generated.types.SourceSubscriptionCreateInput
import org.migor.feedless.generated.types.SourceSubscriptionsCreateInput
import org.migor.feedless.util.GenericFeedUtil.fromDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
  lateinit var sourceSubscriptionDAO: SourceSubscriptionDAO

  @Autowired
  lateinit var currentUser: CurrentUser

  @Autowired
  lateinit var defaultsService: DefaultsService

  @Autowired
  lateinit var webDocumentService: WebDocumentService

  @Autowired
  lateinit var propertyService: PropertyService

  @Transactional
  fun create(data: SourceSubscriptionsCreateInput): List<SourceSubscription> {
    log.info("create sub")
    return data.subscriptions.map { createSubscription(it).toDto() }
  }

  private fun createSubscription(subInput: SourceSubscriptionCreateInput): SourceSubscriptionEntity {
    val sub = SourceSubscriptionEntity()

    sub.title = subInput.sinkOptions.bucket.title
    sub.description = subInput.sinkOptions.bucket.description
    sub.visibility = fromDTO(subInput.sinkOptions.bucket.visibility)
    sub.sources = subInput.sources.map { resolveSource(it, sub) }.toMutableList()
    sub.ownerId = currentUser.user().id
    sub.schedulerExpression = subInput.sourceOptions.refreshCron
    sub.retentionMaxItems = defaultsService.retentionMaxItems(subInput.sinkOptions.bucket.retention.maxItems)
    sub.retentionMaxAgeDays = subInput.sinkOptions.bucket.retention.maxAgeDays

    return sourceSubscriptionDAO.save(sub)
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

  @Cacheable(value = [CacheNames.FEED_RESPONSE], key = "\"bucket/\" + #subscriptionId")
  @Transactional(readOnly = true)
  fun getFeedBySubscriptionId(subscriptionId: String, page: Int): RichFeed {
    val id = UUID.fromString(subscriptionId)
    val bucket = sourceSubscriptionDAO.findById(id).orElseThrow {IllegalArgumentException("subscription not found")}
    val items = webDocumentService.findBySubscriptionId(id, page, ReleaseStatus.released)
      .map { it.toRichArticle() }

    val richFeed = RichFeed()
    richFeed.id = "bucket:${subscriptionId}"
    richFeed.title = bucket.title
    richFeed.description = bucket.description
    richFeed.websiteUrl = "${propertyService.apiGatewayUrl}/bucket:$subscriptionId"
    richFeed.publishedAt = items.maxOfOrNull { it.publishedAt } ?: Date()
    richFeed.items = items
    richFeed.imageUrl = null
    richFeed.expired = false
    richFeed.selfPage = page
    richFeed.feedUrl = "${propertyService.apiGatewayUrl}/stream/bucket/${subscriptionId}/atom"

    return richFeed
  }

  fun findAll(offset: Int, pageSize: Int): List<SourceSubscriptionEntity> {
    val pageable = PageRequest.of(offset, pageSize.coerceAtMost(10), Sort.by(Sort.Direction.DESC, StandardJpaFields.createdAt))
    return (currentUser.userId()
      ?.let { sourceSubscriptionDAO.findAllByOwnerId(it, pageable) }
      ?: emptyList())
  }

  fun findById(id: String): SourceSubscriptionEntity {
    val sub = sourceSubscriptionDAO.findById(UUID.fromString(id)).orElseThrow { RuntimeException("not found") }
    return if (sub.visibility === EntityVisibility.isPublic) {
      sub
    } else {
      if (sub.ownerId == currentUser.userId()) {
        sub
      } else {
        throw RuntimeException("unauthorized")
      }
    }
  }

}

private fun SourceSubscriptionEntity.toDto(): SourceSubscription {
  return SourceSubscription.newBuilder()
    .id(this.id.toString())
    .build()
}

private fun WebDocumentEntity.toRichArticle(): RichArticle {
  val richArticle = RichArticle()
  richArticle.id = this.id.toString()
  richArticle.title = StringUtils.trimToEmpty(this.contentTitle)
  richArticle.url = this.url
//          tags = getTags(content),
  this.attachments?.let {
    richArticle.attachments = it.media.map {
      run {
        val a = JsonAttachment()
        a.url = it.url
        a.type = it.format!!
//                a.size = it.size
        a.duration = it.duration
        a
      }
    }
  }
  richArticle.contentText = StringUtils.trimToEmpty(this.contentText)
  richArticle.contentRaw = this.contentRaw
  richArticle.contentRawMime = this.contentRawMime
  richArticle.publishedAt = this.releasedAt
  richArticle.startingAt = this.startingAt
  richArticle.imageUrl = this.imageUrl
  return richArticle

}
