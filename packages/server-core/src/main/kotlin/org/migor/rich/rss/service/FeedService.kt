package org.migor.rich.rss.service

import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.NativeFeedStatus
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import org.migor.rich.rss.data.jpa.models.NativeFeedEntity
import org.migor.rich.rss.data.jpa.repositories.NativeFeedDAO
import org.migor.rich.rss.generated.types.NativeFeedsWhereInput
import org.migor.rich.rss.generated.types.Selectors
import org.migor.rich.rss.harvest.HarvestResponse
import org.migor.rich.rss.harvest.feedparser.FeedBodyParser
import org.migor.rich.rss.harvest.feedparser.JsonFeedParser
import org.migor.rich.rss.harvest.feedparser.NullFeedParser
import org.migor.rich.rss.harvest.feedparser.XmlFeedParser
import org.migor.rich.rss.util.CryptUtil
import org.migor.rich.rss.util.FeedUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
class FeedService {

  private val log = LoggerFactory.getLogger(FeedService::class.simpleName)

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var nativeFeedDAO: NativeFeedDAO

  @Autowired
  lateinit var opsService: OpsService

  @Autowired
  lateinit var httpService: HttpService

  private val feedBodyParsers: Array<FeedBodyParser> = arrayOf(
    XmlFeedParser(),
    JsonFeedParser(),
    NullFeedParser()
  )

  init {
    feedBodyParsers.sortByDescending { feedBodyParser -> feedBodyParser.priority() }
    log.debug(
      "Using bodyParsers ${
        feedBodyParsers.joinToString(", ") { contentStrategy -> "$contentStrategy priority: ${contentStrategy.priority()}" }
      }"
    )
  }

  fun parseFeedFromUrl(corrId: String, url: String): RichFeed {
    log.info("[$corrId] parseFeedFromUrl $url")
    httpService.guardedHttpResource(
      url,
      200,
      listOf("text/", "application/xml", "application/json", "application/rss", "application/atom", "application/rdf")
    )
    val request = httpService.prepareGet(url)
//    authHeader?.let {
//      request.setHeader("Authorization", it)
//    }
    val branchedCorrId = CryptUtil.newCorrId(parentCorrId = corrId)
    log.info("[$branchedCorrId] GET $url")
    val response = httpService.executeRequest(branchedCorrId, request, 200)
    return this.parseFeed(corrId, HarvestResponse(url, response))
  }

  fun parseFeed(corrId: String, response: HarvestResponse): RichFeed {
    log.debug("[$corrId] Parsing feed")
    val (feedType, _) = FeedUtil.detectFeedTypeForResponse(
      response.response
    )
    log.debug("[$corrId] Parse feedType=$feedType")
    val bodyParser = feedBodyParsers.first { bodyParser ->
      bodyParser.canProcess(feedType)
    }
    return runCatching {
      bodyParser.process(corrId, response)
    }.onFailure {
      log.info("[${corrId}] bodyParser ${bodyParser::class.simpleName} failed with ${it.message}")
    }.getOrThrow()
  }

  fun updateLastUpdatedAt(corrId: String, feed: NativeFeedEntity) {
    log.debug("[$corrId] Updating lastUpdatedAt for feed=${feed.id}")
    nativeFeedDAO.updateLastUpdatedAt(feed.id, Date())
  }

  fun updateLastChangedAt(corrId: String, feed: NativeFeedEntity) {
    log.debug("[$corrId] Updating lastChangedAt for feed=${feed.id}")
    nativeFeedDAO.updateLastChangedAt(feed.id, Date())
  }

  fun updateNextHarvestDateAfterError(corrId: String, feed: NativeFeedEntity, e: Throwable) {
    // todo mag externalize nextHarvest interval

    feed.failedAttemptCount += 1
    val nextHarvestAt = if (feed.failedAttemptCount >= 5) {
      log.info("[$corrId] Critical errorCount reached, quasi-stopping harvesting, retrying every 7 days")
      Date.from(Date().toInstant().plus(Duration.of(7, ChronoUnit.DAYS)))
    } else {
      log.info("[$corrId] Erroneous feed with errorCount ${feed.failedAttemptCount}")
      Date.from(Date().toInstant().plus(Duration.of((10 * (feed.failedAttemptCount + 1)).toLong(), ChronoUnit.MINUTES)))
    }

    log.info("[$corrId] Rescheduling failed harvest ${feed.feedUrl} to $nextHarvestAt")
    feed.nextHarvestAt = nextHarvestAt

    // todo mag push ops notificationService.createOpsNotificationForUser(corrId, feed, e)

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
    val harvestIntervalMinutes = Optional.ofNullable(feed.harvestIntervalMinutes).orElse(10) // todo use feature restriction based on user
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
  fun findByFeedId(feedId: String, page: Int): RichFeed {
    val id = UUID.fromString(feedId)
    val feed = nativeFeedDAO.findById(id).orElseThrow()

    val items = articleService.findByStreamId(feed.streamId!!, page, ArticleType.feed, ReleaseStatus.released)

    val richFeed = RichFeed()
    richFeed.id = "feed:${feedId}"
    richFeed.description = feed.description
    richFeed.title = feed.title
    richFeed.imageUrl = feed.imageUrl
    richFeed.iconUrl = feed.iconUrl
    richFeed.items = items
    richFeed.language = feed.lang
    richFeed.websiteUrl = feed.websiteUrl
    richFeed.feedUrl = "${propertyService.publicUrl}/feed:$feedId"
    richFeed.publishedAt = Optional.ofNullable(items.maxOfOrNull { it.publishedAt }).orElse(Date())

    return richFeed
  }

  fun changeStatus(
    corrId: String,
    feed: NativeFeedEntity,
    status: NativeFeedStatus,
    ex: Exception
  ) {
    if (status !== NativeFeedStatus.OK) {
      opsService.createOpsMessage(corrId, feed, status, ex)
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
    return nativeFeedDAO.findAllMatching(pageable)
  }

  fun updateMeta(feed: NativeFeedEntity, richFeed: RichFeed) {
    feed.title = richFeed.title
    feed.description = richFeed.description
    feed.imageUrl = richFeed.imageUrl
    feed.iconUrl = richFeed.iconUrl
    nativeFeedDAO.save(feed)
  }

  fun toHash(selectors: Selectors): String {
    val sha1 = MessageDigest.getInstance("SHA1")
    val input = selectors.linkXPath + selectors.dateXPath + selectors.contextXPath + selectors.extendContext
    return BigInteger(1, sha1.digest(input.toByteArray())).toString(16).padStart(32, '0')
  }

}
