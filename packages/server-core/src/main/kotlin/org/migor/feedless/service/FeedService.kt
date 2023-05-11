package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.api.auth.CurrentUser
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.config.CacheNames
import org.migor.feedless.data.jpa.enums.ArticleType
import org.migor.feedless.data.jpa.enums.NativeFeedStatus
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.NativeFeedEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.ArticleDAO
import org.migor.feedless.data.jpa.repositories.NativeFeedDAO
import org.migor.feedless.feed.parser.FeedBodyParser
import org.migor.feedless.feed.parser.JsonFeedParser
import org.migor.feedless.feed.parser.NullFeedParser
import org.migor.feedless.feed.parser.XmlFeedParser
import org.migor.feedless.generated.types.NativeFeedsWhereInput
import org.migor.feedless.generated.types.Selectors
import org.migor.feedless.harvest.HarvestResponse
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.FeedUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Profile(AppProfiles.database)
class FeedService {

  private val log = LoggerFactory.getLogger(FeedService::class.simpleName)

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var articleDAO: ArticleDAO

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var nativeFeedDAO: NativeFeedDAO

  @Autowired
  lateinit var currentUser: CurrentUser

  @Autowired
  lateinit var opsService: OpsService

  @Autowired
  lateinit var httpService: HttpService

  fun updateNextHarvestDateAfterError(corrId: String, feed: NativeFeedEntity, e: Throwable) {
    // todo mag externalize nextHarvest interval
    log.info("[$corrId] handling ${e.message}")

    feed.failedAttemptCount += 1
    val nextHarvestAt = if (feed.failedAttemptCount >= 5) {
      log.info("[$corrId] Critical errorCount reached, quasi-stopping harvesting, retrying every 7 days")
      Date.from(Date().toInstant().plus(Duration.of(7, ChronoUnit.DAYS)))
    } else {
      log.info("[$corrId] Erroneous feed with errorCount ${feed.failedAttemptCount}")
      Date.from(Date().toInstant().plus(Duration.of((10 * (feed.failedAttemptCount + 1)).toLong(), ChronoUnit.MINUTES)))
    }

    log.info("[$corrId] Rescheduling failed harvest ${feed.id} to $nextHarvestAt")
    feed.nextHarvestAt = nextHarvestAt

    opsService.createOpsMessage(corrId, feed, e)
    feed.status = NativeFeedStatus.DEFECTIVE

    nativeFeedDAO.save(feed)
  }

  companion object {
    fun absUrl(baseUrl: String, relativeUrl: String): String {
      return try {
        URL(URL(baseUrl), relativeUrl).toURI().toString()
      } catch (e: Exception) {
        relativeUrl
      }
    }
  }

  fun updateNextHarvestDate(corrId: String, feed: NativeFeedEntity, hasNewEntries: Boolean) {
    val harvestIntervalMinutes = feed.harvestIntervalMinutes ?: 10 // todo use feature restriction based on user
    val harvestInterval = if (hasNewEntries) {
      (harvestIntervalMinutes * 0.5).coerceAtLeast(10.0)
    } else {
      (harvestIntervalMinutes * 4).coerceAtMost(700) // twice a day
    }
    val nextHarvestAt = Date.from(Date().toInstant().plus(Duration.of(harvestInterval.toLong(), ChronoUnit.MINUTES)))
    log.debug("[$corrId] Scheduling next harvest for ${feed.feedUrl} to $nextHarvestAt")

    nativeFeedDAO.updateNextHarvestAtAndHarvestInterval(feed.id, nextHarvestAt, harvestInterval.toInt())
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  fun applyRetentionStrategy(corrId: String, feed: NativeFeedEntity) {
    // todo mag implement
//    feed.retentionSize?.let { articleRefRepository.applyRetentionStrategyOfSize(feed.streamId, it) }
  }

  @Cacheable(value = [CacheNames.FEED_RESPONSE], key = "\"feed/\" + #feedId")
  fun findByFeedId(feedId: String, page: Int): RichFeed {
    val id = UUID.fromString(feedId)
    val feed = nativeFeedDAO.findById(id).orElseThrow {IllegalArgumentException("nativeFeed not found")}

    val items = articleService.findByStreamId(feed.streamId, page, ArticleType.feed, ReleaseStatus.released)

    val richFeed = RichFeed()
    richFeed.id = "feed:${feedId}"
    richFeed.description = feed.description
    richFeed.title = feed.title
    richFeed.imageUrl = feed.imageUrl
    richFeed.iconUrl = feed.iconUrl
    richFeed.items = items
    richFeed.language = feed.lang
    richFeed.websiteUrl = feed.websiteUrl
    richFeed.feedUrl = "${propertyService.apiGatewayUrl}/feed:$feedId"
    richFeed.publishedAt = items.maxOfOrNull { it.publishedAt } ?: Date()

    return richFeed
  }

  @Cacheable(value = [CacheNames.FEED_RESPONSE], key = "\"stream/\" + #streamId")
  fun findByStreamId(streamId: String, page: Int): RichFeed {
    val id = UUID.fromString(streamId)
    val items = articleService.findByStreamId(id, page, ArticleType.feed, ReleaseStatus.released)

    val richFeed = RichFeed()
    richFeed.id = "stream:${streamId}"
    richFeed.description = "Notifications"
    richFeed.title = "Notifications"
    richFeed.items = items
    richFeed.feedUrl = "${propertyService.apiGatewayUrl}/stream:$streamId"
    richFeed.publishedAt = items.maxOfOrNull { it.publishedAt } ?: Date()

    return richFeed
  }

  fun changeStatus(
    corrId: String,
    feed: NativeFeedEntity,
    status: NativeFeedStatus,
    ex: Throwable
  ) {
    log.info("[$corrId] status change ${feed.id} ${feed.status} -> ${status}")
    if (status !== NativeFeedStatus.OK) {
      opsService.createOpsMessage(corrId, feed, ex)
    }
    nativeFeedDAO.setStatus(feed.id, status)
  }

  fun findNativeById(id: UUID): Optional<NativeFeedEntity> {
    return nativeFeedDAO.findById(id)
  }

  fun findAllByFeedUrl(feedUrl: String, pageable: PageRequest): List<NativeFeedEntity> {
    return nativeFeedDAO.findAllByFeedUrl(feedUrl, pageable)
  }

  fun findAllByFilter(where: NativeFeedsWhereInput, pageable: PageRequest): List<NativeFeedEntity> {
    // todo where not used
    return nativeFeedDAO.findAllByOwnerId(currentUser.userId()!!, pageable)
  }

  fun updateMeta(feed: NativeFeedEntity, richFeed: RichFeed) {
    feed.title = richFeed.title
    feed.description = richFeed.description
    feed.imageUrl = richFeed.imageUrl
    feed.iconUrl = richFeed.iconUrl
    nativeFeedDAO.save(feed)
  }

  fun existsByContentInFeed(webDocument: WebDocumentEntity, feed: NativeFeedEntity): Boolean {
    return articleDAO.existsByWebDocumentIdAndStreamId(webDocument.id, feed.streamId)
  }

}
